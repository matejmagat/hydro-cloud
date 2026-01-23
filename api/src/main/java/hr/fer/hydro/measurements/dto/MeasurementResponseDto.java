package hr.fer.hydro.measurements.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.NonNull;

import java.time.OffsetDateTime;

public record MeasurementResponseDto(
        @NonNull
        Long measurementId,

        @NonNull
        Long stationId,

        @NotBlank(message = "Station name is required.")
        String station,

        @NonNull
        Long typeId,

        @NotBlank(message = "Station name is required.")
        String type,

        @NonNull
        String unit,

        @NonNull
        Double value,

        @NonNull
        OffsetDateTime measuredAt
) {
}
