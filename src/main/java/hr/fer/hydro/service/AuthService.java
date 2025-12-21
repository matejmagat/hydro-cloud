package hr.fer.hydro.service;

import hr.fer.hydro.api.auth.AuthResponse;
import hr.fer.hydro.api.auth.LoginReq;
import hr.fer.hydro.api.auth.SignUpReq;

public interface AuthService {
    AuthResponse login(LoginReq loginReq);
    AuthResponse signUp(SignUpReq signUpReq);
    void disable2FA();
}
