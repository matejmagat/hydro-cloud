package hr.fer.hydro.auth.google2fa;

import hr.fer.hydro.auth.auth.dto.AuthResponse;
import hr.fer.hydro.auth.auth.service.AuthService;
import hr.fer.hydro.auth.google2fa.api.rest.Google2FAApi;
import hr.fer.hydro.auth.google2fa.dto.QRCode;
import hr.fer.hydro.auth.google2fa.dto.Status2FA;
import hr.fer.hydro.auth.google2fa.dto.Verify2FAReq;
import hr.fer.hydro.auth.google2fa.dto.Verify2FAResult;
import hr.fer.hydro.auth.google2fa.service.GoogleAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class Google2FAController implements Google2FAApi {
    private final AuthService authService;
    private final GoogleAuthService googleAuthService;

    @Override
    public void disable2FA() {
        authService.disable2FA();
    }

    @Override
    public ResponseEntity<QRCode> activate2FA() {
        return ResponseEntity.ok(googleAuthService.activate2FA());
    }

    @Override
    public ResponseEntity<Verify2FAResult> activate2FA(Verify2FAReq verify2FAReq) {
        return ResponseEntity.ok(googleAuthService.verify2FA(verify2FAReq));
    }

    @Override
    public ResponseEntity<AuthResponse> loginVerify2FA(Verify2FAReq verify2FAReq) {
        return ResponseEntity.ok(authService.loginVerify2FA(verify2FAReq));
    }

    @Override
    public ResponseEntity<Status2FA> get2FAStatus() {
        return ResponseEntity.ok(googleAuthService.get2FAStatus());
    }
}
