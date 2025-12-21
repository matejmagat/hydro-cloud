package hr.fer.hydro.api.auth;

import lombok.Builder;

@Builder
public record AuthResponse(
        String accessToken,
        String pendingToken,
        boolean is2FAEnabled
) {
}
