package hr.fer.hydro.measurements.dto;

import lombok.NonNull;

public record MeasurementRequestDto(
        @NonNull
        Long stationId,

        @NonNull
        Long typeId,

        @NonNull
        Double value
) {
}
