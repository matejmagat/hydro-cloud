package hr.fer.hydro.api.google2fa;

import java.util.List;

public record Verify2FAResult(
        List<Integer> scratchCodes
) {
}
