package hr.fer.hydro.measurements;

import hr.fer.hydro.csv.CsvService;
import hr.fer.hydro.measurements.api.rest.MeasurementsApi;
import hr.fer.hydro.measurements.dto.MeasurementBulkRequestDto;
import hr.fer.hydro.measurements.dto.MeasurementRequestDto;
import hr.fer.hydro.measurements.dto.MeasurementResponseDto;
import hr.fer.hydro.measurements.dto.MeasurementTypeCountDto;
import hr.fer.hydro.pagination.HydroPage;
import hr.fer.hydro.xls.ExcelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MeasurementsController implements MeasurementsApi {

    private final MeasurementsService measurementsService;
    private final CsvService csvService;
    private final ExcelService excelService;

    @Override
    public ResponseEntity<HydroPage<MeasurementResponseDto>> getMeasurements(List<Long> stationId, Long typeId,
                                                                             OffsetDateTime fromDate, OffsetDateTime toDate, Pageable pageable) {
        return ResponseEntity
                .ok(measurementsService.getMeasurements(stationId, typeId, fromDate, toDate,pageable));
    }

    @Override
    public ResponseEntity<List<MeasurementTypeCountDto>> getMeasurementTypesStatistics(
            List<Long> stationId,
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
    public ResponseEntity<Void> importMeasurementsJson(List<MeasurementBulkRequestDto> measurements) {
        measurementsService.createBulkMeasurements(measurements);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    public ResponseEntity<Void> importMeasurementsFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File must be provided");
        }
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File name is missing");
        }
        String lower = filename.toLowerCase();
        List<MeasurementBulkRequestDto> measurements;

        if (lower.endsWith(".csv")) {
            measurements = csvService.importMeasurements(file.getBytes());
        } else if (lower.endsWith(".xls") || lower.endsWith(".xlsx")) {
            measurements = excelService.importMeasurements(file.getBytes());
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported file type: " + filename);
        }

        measurementsService.createBulkMeasurements(measurements);
        return ResponseEntity.status(HttpStatus.CREATED).build();
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
