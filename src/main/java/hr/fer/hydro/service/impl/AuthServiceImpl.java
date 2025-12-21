package hr.fer.hydro.service.impl;


import hr.fer.hydro.api.auth.AuthResponse;
import hr.fer.hydro.api.auth.LoginReq;
import hr.fer.hydro.api.auth.SignUpReq;
import hr.fer.hydro.config.core.UserLocalThread;
import hr.fer.hydro.db.User2FADao;
import hr.fer.hydro.db.User2FAScratchCodeDao;
import hr.fer.hydro.db.UserDao;
import hr.fer.hydro.db.entity.User2FAEntity;
import hr.fer.hydro.db.entity.UserEntity;
import hr.fer.hydro.mapper.AuthMapper;
import hr.fer.hydro.service.AuthService;
import hr.fer.hydro.service.JwtService;
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

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginReq loginReq) {
        log.debug("Login attempt for username: {}", loginReq.username());

        final UserEntity user = userDao.findByUsername(loginReq.username())
                .orElseThrow(() -> {
                    log.warn("Login failed: User not found - {}", loginReq.username());
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
                });

        if (!passwordEncoder.matches(loginReq.password(), user.getPassword())) {
            log.warn("Login failed: Invalid password for user - {}", loginReq.username());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        log.info("User logged in successfully: {}", loginReq.username());
        return generateAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse signUp(SignUpReq signUpReq) {
        log.debug("Sign up attempt for username: {}", signUpReq.username());

        validateUserDoesNotExist(signUpReq);

        final UserEntity newUser = authMapper.toUserEntity(signUpReq);
        newUser.setPassword(passwordEncoder.encode(signUpReq.password()));

        final UserEntity savedUser = userDao.save(newUser);
        log.info("New user registered successfully: {}", savedUser.getUsername());

        return generateAuthResponseWithAccessAndRefreshToken(savedUser, false);
    }

    @Override
    @Transactional
    public void disable2FA() {
        final UserEntity user = findUserById(UserLocalThread.getUserId());
        user.setIs2FAEnabled(Boolean.FALSE);
        user2FADao.deleteAllByUser(user);
        user2FAScratchCodeDao.deleteAllByUser(user);
    }

    private UserEntity findUserById(final Integer userId) {
        return userDao.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("User not found with userId = " + userId)
        );
    }

    private void validateUserDoesNotExist(SignUpReq signUpReq) {
        if (userDao.existsByUsername(signUpReq.username())) {
            log.warn("Sign up failed: Username already exists - {}", signUpReq.username());
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Username already exists"
            );
        }

        if (userDao.existsByEmail(signUpReq.email())) {
            log.warn("Sign up failed: Email already exists - {}", signUpReq.email());
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email already exists"
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

        return generateAuthResponseWithAccessAndRefreshToken(user, Boolean.FALSE);
    }

    private AuthResponse generateAuthResponseWithAccessAndRefreshToken(final UserEntity user, final boolean is2FAEnabled) {
        return generateAuthResponseWithAccessAndRefreshToken(user, is2FAEnabled, true);
    }

    private AuthResponse generateAuthResponseWithAccessAndRefreshToken(final UserEntity user, final boolean is2FAEnabled, final Boolean generateNewRefreshToken) {
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