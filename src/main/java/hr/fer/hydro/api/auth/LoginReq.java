package hr.fer.hydro.api.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginReq(
        @NotBlank(message = "Username is required")
        String username,
        @NotBlank(message = "Password is required")
        String password
) {
}
