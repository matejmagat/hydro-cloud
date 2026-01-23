package hr.fer.hydro.auth.google2fa.service.impl;

import com.warrenstrange.googleauth.ICredentialRepository;
import hr.fer.hydro.auth.mapper.AuthMapper;
import hr.fer.hydro.auth.persistence.entities.User2FAEntity;
import hr.fer.hydro.auth.persistence.entities.UserEntity;
import hr.fer.hydro.auth.persistence.repositories.User2FADao;
import hr.fer.hydro.auth.persistence.repositories.User2FAScratchCodeDao;
import hr.fer.hydro.auth.persistence.repositories.UserDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleAuthCredentialRepository implements ICredentialRepository {
    private final AuthMapper authMapper;
    private final UserDao userDao;
    private final User2FADao user2FADao;
    private final User2FAScratchCodeDao user2FAScratchCodeDao;

    @Override
    public String getSecretKey(final String username) {
        final UserEntity user = findUserByUsername(username);
        final User2FAEntity user2FAEntity = user2FADao.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "2FA not initiated"));

        if (user2FAEntity.getConfirmed()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "2FA already confirmed");
        }
        return user2FADao.findByUser(user).orElseThrow(
                () -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "User doesn't have 2FA set up!"
                )
        ).getSecret();
    }

    @Override
    public void saveUserCredentials(final String username, final String secretKey, final int validationCode, List<Integer> scratchCode) {
        final UserEntity user = findUserByUsername(username);
        user2FADao.save(authMapper.toUser2FAEntity(user, secretKey, validationCode));
        user2FAScratchCodeDao.saveAllAndFlush(
                scratchCode.stream()
                        .map(code -> authMapper.toUser2FAScratchCodeEntity(user, code))
                        .toList()
        );
    }

    private UserEntity findUserByUsername(final String username) {
        return userDao.findByUsername(username).orElseThrow(
                () -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                )
        );
    }
}
