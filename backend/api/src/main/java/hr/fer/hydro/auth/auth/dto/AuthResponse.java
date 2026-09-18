package hr.fer.hydro.auth.auth.dto;

import lombok.Builder;

@Builder
public record AuthResponse(
        String accessToken,

        String pendingToken,

        boolean is2FAEnabled
) {
}
