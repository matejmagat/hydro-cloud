package hr.fer.hydro.auth.auth.service.impl;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import hr.fer.hydro.auth.auth.dto.AuthResponse;
import hr.fer.hydro.auth.auth.dto.LoginReq;
import hr.fer.hydro.auth.auth.dto.RegisterReq;
import hr.fer.hydro.auth.auth.service.AuthService;
import hr.fer.hydro.auth.auth.service.JwtService;
import hr.fer.hydro.auth.google2fa.dto.Verify2FAReq;
import hr.fer.hydro.auth.mapper.AuthMapper;
import hr.fer.hydro.auth.persistence.entities.User2FAEntity;
import hr.fer.hydro.auth.persistence.entities.UserEntity;
import hr.fer.hydro.auth.persistence.repositories.User2FADao;
import hr.fer.hydro.auth.persistence.repositories.User2FAScratchCodeDao;
import hr.fer.hydro.auth.persistence.repositories.UserDao;
import hr.fer.hydro.config.core.UserCoreLocalThread;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserDao userDao;
    private final User2FADao user2FADao;
    private final User2FAScratchCodeDao user2FAScratchCodeDao;

    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;
    private final GoogleAuthenticator googleAuthenticator;

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginReq loginReq) {
        final UserEntity user = userDao.findByUsername(loginReq.username())
                .orElseThrow(() -> {
                    log.warn("Login failed: User not found - {}", loginReq.username());
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
                });

        if (!passwordEncoder.matches(loginReq.password(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }
        return generateAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse signUp(RegisterReq signUpReq) {
        validateUserDoesNotExist(signUpReq);

        final UserEntity newUser = authMapper.toUserEntity(signUpReq);
        newUser.setPassword(passwordEncoder.encode(signUpReq.password()));

        return generateAuthResponseWithAccessToken(userDao.save(newUser), false);
    }

    @Override
    @Transactional
    public void disable2FA() {
        final UserEntity user = findUserById(UserCoreLocalThread.getUserId());
        user.setIs2FAEnabled(Boolean.FALSE);
        user2FADao.deleteAllByUser(user);
        user2FAScratchCodeDao.deleteAllByUser(user);
    }

    @Override
    @Transactional
    public AuthResponse loginVerify2FA(final Verify2FAReq verify2FAReq) {
        final UserEntity user = userDao.findById(UserCoreLocalThread.getUserId()).orElseThrow();
        final User2FAEntity user2FAEntity = user2FADao.findByUser(user).orElseThrow();
        if (googleAuthenticator.authorize(user2FAEntity.getSecret(), verify2FAReq.code())) {
            return generateAuthResponseWithAccessToken(user, Boolean.TRUE);
        }
        return new AuthResponse(null, null, Boolean.TRUE);
    }

    private UserEntity findUserById(final Integer userId) {
        return userDao.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("User not found with userId = " + userId)
        );
    }

    private void validateUserDoesNotExist(RegisterReq signUpReq) {
        if (userDao.existsByEmail(signUpReq.email())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email already exists"
            );
        }

        if (userDao.existsByUsername(signUpReq.username())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Username already exists"
            );
        }

    }

    private AuthResponse generateAuthResponse(final UserEntity user) {
        if (user.getIs2FAEnabled()) {
            final User2FAEntity user2FA = user2FADao.findByUser(user).orElseThrow();
            if (user2FA.getConfirmed()) {
                final UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
                return AuthResponse.builder()
                        .accessToken(null)
                        .pendingToken(jwtService.generatePendingToken(userDetails))
                        .is2FAEnabled(Boolean.TRUE)
                        .build();

            }
        }

        return generateAuthResponseWithAccessToken(user, Boolean.FALSE);
    }

    private AuthResponse generateAuthResponseWithAccessToken(final UserEntity user, final boolean is2FAEnabled) {
        final HashMap<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());

        final UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        final String accessToken = jwtService.generateAccessToken(claims, userDetails);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .pendingToken(null)
                .is2FAEnabled(is2FAEnabled)
                .build();
    }
}