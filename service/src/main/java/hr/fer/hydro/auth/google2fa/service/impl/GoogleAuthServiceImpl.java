package hr.fer.hydro.auth.google2fa.service.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import hr.fer.hydro.auth.google2fa.dto.QRCode;
import hr.fer.hydro.auth.google2fa.dto.Verify2FAReq;
import hr.fer.hydro.auth.google2fa.dto.Verify2FAResult;
import hr.fer.hydro.auth.google2fa.service.GoogleAuthService;
import hr.fer.hydro.auth.persistence.entities.User2FAScratchCodeEntity;
import hr.fer.hydro.auth.persistence.entities.UserEntity;
import hr.fer.hydro.auth.persistence.repositories.User2FADao;
import hr.fer.hydro.auth.persistence.repositories.User2FAScratchCodeDao;
import hr.fer.hydro.auth.persistence.repositories.UserDao;
import hr.fer.hydro.config.core.UserCoreLocalThread;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleAuthServiceImpl implements GoogleAuthService {
    private static final String ISSUER = "CHAINLINK";

    private final GoogleAuthenticator googleAuthenticator;
    private final UserDao userDao;
    private final User2FADao user2FADao;
    private final User2FAScratchCodeDao user2FAScratchCodeDao;

    @Override
    @Transactional
    public QRCode activate2FA() {
        final UserEntity user = userDao.findById(UserCoreLocalThread.getUserId()).orElseThrow(() -> new EntityNotFoundException("User not found by id = {}" + UserCoreLocalThread.getUserId()));
        try {
            final GoogleAuthenticatorKey key = googleAuthenticator.createCredentials(user.getUsername());
            final String secret = key.getKey();
            final String url = GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL(
                    ISSUER,
                    user.getUsername(),
                    new GoogleAuthenticatorKey.Builder(secret).build());
            return generateQRBase64(url);
        } catch (final Exception e) {
            log.error("Error while activating token: ", e);
            return new QRCode(null);
        }
    }

    @Override
    @Transactional
    public Verify2FAResult verify2FA(final Verify2FAReq verify2FAReq) {
        final UserEntity user = userDao.findById(UserCoreLocalThread.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        final String secretKey = googleAuthenticator.getCredentialRepository().getSecretKey(user.getUsername());
        if (verify2FAReq.code() != null && googleAuthenticator.authorize(secretKey, verify2FAReq.code())) {
            user2FADao.findByUser(user).ifPresent(user2FAEntity -> user2FAEntity.setConfirmed(Boolean.TRUE));
            user.setIs2FAEnabled(Boolean.TRUE);
            return new Verify2FAResult(
                    user2FAScratchCodeDao.findAllByUser(user).stream().map(User2FAScratchCodeEntity::getCode).toList()
            );
        }
        return new Verify2FAResult(Collections.emptyList());
    }

    private static QRCode generateQRBase64(final String qrCodeText) throws Exception {
        final QRCodeWriter qrCodeWriter = new QRCodeWriter();
        final Map<EncodeHintType, Object> hintMap = new HashMap<>();
        hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        final BitMatrix bitMatrix = qrCodeWriter.encode(qrCodeText, BarcodeFormat.QR_CODE, 200, 200, hintMap);
        final BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", baos);
        return new QRCode(Base64.getEncoder().encodeToString(baos.toByteArray()));
    }
}
