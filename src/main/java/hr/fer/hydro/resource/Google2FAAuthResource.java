package hr.fer.hydro.resource;

import hr.fer.hydro.api.auth.AuthResponse;
import hr.fer.hydro.api.google2fa.QRCode;
import hr.fer.hydro.api.google2fa.Verify2FAResult;
import hr.fer.hydro.api.google2fa.Verify2FAReq;
import hr.fer.hydro.service.AuthService;
import hr.fer.hydro.service.google.auth.GoogleAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/google-2fa", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class Google2FAAuthResource {
    private final AuthService authService;
    private final GoogleAuthService googleAuthService;

    @PutMapping("/:disable")
    public void disable2FA() {
        authService.disable2FA();
    }

    @PostMapping("/:activate")
    public ResponseEntity<QRCode> activate2FA() {
        return ResponseEntity.ok(googleAuthService.activate2FA());
    }

    @PostMapping("/:verify")
    public ResponseEntity<Verify2FAResult> activate2FA(@RequestBody Verify2FAReq verify2FAReq) {
        return ResponseEntity.ok(googleAuthService.verify2FA(verify2FAReq));
    }

    @PostMapping("/login/:verify")
    public ResponseEntity<AuthResponse> loginVerify2FA(@RequestBody Verify2FAReq verify2FAReq) {
        return ResponseEntity.ok(authService.loginVerify2FA(verify2FAReq));
    }
}
