package hr.fer.hydro.measurements;

import hr.fer.hydro.measurements.api.rest.MeasurementsApi;
import hr.fer.hydro.measurements.dto.MeasurementRequestDto;
import hr.fer.hydro.measurements.dto.MeasurementResponseDto;
import hr.fer.hydro.measurements.dto.MeasurementTypeCountDto;
import hr.fer.hydro.pagination.HydroPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MeasurementsController implements MeasurementsApi {

    private final MeasurementsService measurementsService;

    @Override
    public ResponseEntity<HydroPage<MeasurementResponseDto>> getMeasurements(Long stationId, Long typeId,
                                                                             OffsetDateTime fromDate, OffsetDateTime toDate, Pageable pageable) {
        return ResponseEntity
                .ok(measurementsService.getMeasurements(stationId, typeId, fromDate, toDate,pageable));
    }

    @Override
    public ResponseEntity<List<MeasurementTypeCountDto>> getMeasurementTypesStatistics(
            Long stationId,
            Long typeId,
            OffsetDateTime fromDate,
            OffsetDateTime toDate) {

        return ResponseEntity.ok(
                measurementsService.getMeasurementTypesStatistics(stationId, typeId, fromDate, toDate)
        );
    }

    @Override
    public ResponseEntity<MeasurementResponseDto> createMeasurement(MeasurementRequestDto measurementRequestDto) {
        return ResponseEntity
                .ok(measurementsService.createMeasurement(measurementRequestDto));
    }

    @Override
    public ResponseEntity<MeasurementResponseDto> getMeasurement(Long measurementId) {
        return ResponseEntity
                .ok(measurementsService.getMeasurement(measurementId));
    }

    @Override
    public void deleteMeasurement(Long measurementId) {
        measurementsService.deleteMeasurement(measurementId);
    }
}
