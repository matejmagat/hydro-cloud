package hr.fer.hydro.auth.google2fa.api.rest;

import hr.fer.hydro.auth.auth.dto.AuthResponse;
import hr.fer.hydro.auth.google2fa.dto.QRCode;
import hr.fer.hydro.auth.google2fa.dto.Status2FA;
import hr.fer.hydro.auth.google2fa.dto.Verify2FAReq;
import hr.fer.hydro.auth.google2fa.dto.Verify2FAResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@Tag(name = "Google 2FA", description = "Dvofaktorska autentifikacija putem Google Authenticatora")
@RequestMapping(value = "/google-2fa")
public interface Google2FAApi {

    @PutMapping(value = "/disable")
    void disable2FA();

    @PostMapping(value = "/activate", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<QRCode> activate2FA();

    @PostMapping(value = "/verify", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Verify2FAResult> activate2FA(@RequestBody Verify2FAReq verify2FAReq);

    @PostMapping(value = "/login/verify", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<AuthResponse> loginVerify2FA(@RequestBody Verify2FAReq verify2FAReq);

    @GetMapping(value = "/status", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Status2FA> get2FAStatus();
}
