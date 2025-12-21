package hr.fer.hydro.service.google.auth;

import hr.fer.hydro.api.google2fa.QRCode;
import hr.fer.hydro.api.google2fa.Verify2FAResult;
import hr.fer.hydro.api.google2fa.Verify2FAReq;

public interface GoogleAuthService {
    QRCode activate2FA();
    Verify2FAResult verify2FA(final Verify2FAReq verify2FAReq);
}
