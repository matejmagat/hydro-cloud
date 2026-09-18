package hr.fer.hydro.stations.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.NonNull;

import java.time.OffsetDateTime;

public record StationResponseDto(
        @NonNull
        Long stationId,

        @NotBlank(message = "Station name is required.")
        String stationName,

        double longitude,

        double latitude,

        @NonNull
        OffsetDateTime activeFrom,

        @NonNull
        OffsetDateTime activeTo
) {
};
