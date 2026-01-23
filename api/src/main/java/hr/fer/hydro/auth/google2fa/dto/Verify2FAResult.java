package hr.fer.hydro.auth.google2fa.dto;

import java.util.List;

public record Verify2FAResult(
        List<Integer> scratchCodes
) {
}
