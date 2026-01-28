package hr.fer.hydro.measurements;

import hr.fer.hydro.DataMapper;
import hr.fer.hydro.measurements.dto.MeasurementRequestDto;
import hr.fer.hydro.measurements.dto.MeasurementResponseDto;
import hr.fer.hydro.measurements.dto.MeasurementTypeCountDto;
import hr.fer.hydro.measurements.persistence.entities.MeasurementDataPoint;
import hr.fer.hydro.measurements.persistence.entities.MeasurementType;
import hr.fer.hydro.measurements.persistence.entities.MeasurementValue;
import hr.fer.hydro.measurements.persistence.repositories.MeasurementDataPointRepository;
import hr.fer.hydro.measurements.persistence.repositories.MeasurementTypeRepository;
import hr.fer.hydro.measurements.persistence.repositories.MeasurementValueRepository;
import hr.fer.hydro.stations.persistence.entities.Station;
import hr.fer.hydro.stations.persistence.repositories.StationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeasurementsService {

    private final StationRepository stationRepository;
    private final MeasurementDataPointRepository measurementDataPointRepository;
    private final MeasurementTypeRepository measurementTypeRepository;
    private final MeasurementValueRepository measurementValueRepository;
    private final DataMapper dataMapper;

    public List<MeasurementResponseDto> getMeasurements(
            Long stationId,
            Long typeId,
            OffsetDateTime fromDate,
            OffsetDateTime toDate) {

        // The repository query now handles all NULL checks internally
        List<MeasurementDataPoint> measurementDataPoints =
                measurementDataPointRepository.findWithFilters(stationId, typeId, fromDate, toDate);

        return measurementDataPoints.stream()
                .map(dataMapper::toMeasurementResponseDto)
                .toList();
    }

    @Transactional
    public MeasurementResponseDto createMeasurement(MeasurementRequestDto measurementRequestDto) {
        Optional<Station> station = stationRepository.findById(measurementRequestDto.stationId());

        if (station.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Station with stationId " + measurementRequestDto.stationId() + " not found"
            );
        }

        Optional<MeasurementType> measurementType = measurementTypeRepository.findById(measurementRequestDto.typeId());

        if (measurementType.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Measurement type with typeId " + measurementRequestDto.typeId() + " not found"
            );
        }

        MeasurementValue measurementValue = new MeasurementValue(measurementRequestDto.value());

        MeasurementDataPoint measurementDataPoint = new MeasurementDataPoint(station.get(), measurementType.get(), measurementValue);

        return dataMapper.toMeasurementResponseDto(measurementDataPoint);
    }

    public MeasurementResponseDto getMeasurement(Long measurementId) {
        Optional<MeasurementDataPoint> measurementDataPoint = measurementDataPointRepository.findById(measurementId);

        return measurementDataPoint.map(dataMapper::toMeasurementResponseDto)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Measurement with measurementId " + measurementId + " not found"
                ));
    }

    @Transactional
    public void deleteMeasurement(Long measurementId) {
        if (!measurementDataPointRepository.existsById(measurementId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Measurement with measurementId " + measurementId + " not found"
            );
        }
        measurementDataPointRepository.deleteById(measurementId);
    }

    public List<MeasurementTypeCountDto> getMeasurementTypesStatistics(
            Long stationId,
            Long typeId,
            OffsetDateTime fromDate,
            OffsetDateTime toDate) {

        return measurementDataPointRepository.countTypesWithFilters(stationId, typeId, fromDate, toDate);
    }
}
