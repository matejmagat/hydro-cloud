package hr.fer.hydro.auth.auth.service;


import hr.fer.hydro.auth.auth.dto.AuthResponse;
import hr.fer.hydro.auth.auth.dto.LoginReq;
import hr.fer.hydro.auth.auth.dto.RegisterReq;
import hr.fer.hydro.auth.google2fa.dto.Verify2FAReq;

public interface AuthService {
    AuthResponse login(LoginReq loginReq);

    AuthResponse signUp(RegisterReq signUpReq);

    void disable2FA();

    AuthResponse loginVerify2FA(Verify2FAReq verify2FAReq);
}
