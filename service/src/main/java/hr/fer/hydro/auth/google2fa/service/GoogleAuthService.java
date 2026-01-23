package hr.fer.hydro.auth.google2fa.service;

import hr.fer.hydro.auth.google2fa.dto.QRCode;
import hr.fer.hydro.auth.google2fa.dto.Status2FA;
import hr.fer.hydro.auth.google2fa.dto.Verify2FAReq;
import hr.fer.hydro.auth.google2fa.dto.Verify2FAResult;

public interface GoogleAuthService {
    QRCode activate2FA();

    Verify2FAResult verify2FA(final Verify2FAReq verify2FAReq);

    Status2FA get2FAStatus();
}
