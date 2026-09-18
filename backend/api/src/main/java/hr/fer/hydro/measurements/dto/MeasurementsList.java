package hr.fer.hydro.measurements.dto;

import java.util.List;

public record MeasurementsList(

        List<MeasurementResponseDto> measurements,

        Integer count
) {
}
