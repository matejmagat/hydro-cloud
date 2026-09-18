package hr.fer.hydro.measurements.dto;

import lombok.NonNull;

import java.time.LocalDateTime;

public record MeasurementBulkRequestDto(
        @NonNull
        Long stationId,

        @NonNull
        Long typeId,

        @NonNull
        Double value,

        @NonNull
        LocalDateTime measuredAt
) {
}
