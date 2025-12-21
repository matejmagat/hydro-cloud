package hr.fer.hydro.service.google.auth;

import hr.fer.hydro.api.google2fa.QRCode;
import hr.fer.hydro.api.google2fa.Verify2FA;
import hr.fer.hydro.api.google2fa.Verify2FAReq;

public interface GoogleAuthService {
    QRCode activate2FA();
    Verify2FA verify2FA(final Verify2FAReq verify2FAReq);
}
