package hr.fer.hydro.auth.auth.api.rest;

import hr.fer.hydro.auth.auth.dto.AuthResponse;
import hr.fer.hydro.auth.auth.dto.LoginReq;
import hr.fer.hydro.auth.auth.dto.RegisterReq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@CrossOrigin
@Tag(name = "Authentication", description = "Operacije za prijavu i registraciju korisnika")
@RequestMapping(value = "/auth")
public interface AuthApi {

    @Operation(summary = "Prijava korisnika", description = "Vraća JWT token ako su podaci ispravni")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Uspješna prijava.",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Neispravan zahtjev.", content = @Content)
    })
    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<AuthResponse> login(final @Valid @RequestBody LoginReq loginReq);

    @Operation(summary = "Registracija novog korisnika")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Uspješno stvoren novi korisnik.",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Neispravan zahtjev.", content = @Content)
    })
    @PostMapping(value = "/sign-up", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<AuthResponse> signUp(final @Valid @RequestBody RegisterReq signUpReq);
}
