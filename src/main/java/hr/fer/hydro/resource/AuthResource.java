package hr.fer.hydro.resource;

import hr.fer.hydro.api.auth.AuthResponse;
import hr.fer.hydro.api.auth.LoginReq;
import hr.fer.hydro.api.auth.RegisterReq;
import hr.fer.hydro.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class AuthResource {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(final @Valid @RequestBody LoginReq loginReq) {
        return ResponseEntity.ok(authService.login(loginReq));
    }

    @PostMapping("/sign-up")
    public ResponseEntity<AuthResponse> signUp(final @Valid @RequestBody RegisterReq signUpReq) {
        return ResponseEntity.ok(authService.signUp(signUpReq));
    }
}
