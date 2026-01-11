package hr.fer.hydro.auth.auth;

import hr.fer.hydro.auth.auth.api.rest.AuthApi;
import hr.fer.hydro.auth.auth.dto.AuthResponse;
import hr.fer.hydro.auth.auth.dto.LoginReq;
import hr.fer.hydro.auth.auth.dto.RegisterReq;
import hr.fer.hydro.auth.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final AuthService authService;

    @Override
    public ResponseEntity<AuthResponse> login(LoginReq loginReq) {
        return ResponseEntity.ok(authService.login(loginReq));
    }

    @Override
    public ResponseEntity<AuthResponse> signUp(RegisterReq signUpReq) {
        return ResponseEntity.ok(authService.signUp(signUpReq));
    }
}
