package hr.fer.hydro.stations.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.NonNull;

public record StationRequestDto(
        @NotBlank(message = "Station name is required.")
        String stationName,

        @NonNull
        Long longitude,

        @NonNull
        Long latitude
) {
}
