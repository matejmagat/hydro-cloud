package hr.fer.hydro.measurements;

import hr.fer.hydro.DataMapper;
import hr.fer.hydro.measurements.dto.MeasurementBulkRequestDto;
import hr.fer.hydro.measurements.dto.MeasurementRequestDto;
import hr.fer.hydro.measurements.dto.MeasurementResponseDto;
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
import java.time.ZoneId;
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
    private final static ZoneId STATION_ZONE = ZoneId.of("Europe/Zagreb");

    public List<MeasurementResponseDto> getMeasurements(Long stationId, Long typeId) {
        List<MeasurementDataPoint> measurementDataPoints;

        if (isNull(stationId) && isNull(typeId)) {
            measurementDataPoints = measurementDataPointRepository.findAll();
        } else if (nonNull(stationId) && isNull(typeId)) {
            measurementDataPoints = measurementDataPointRepository.findByStationId(stationId);
        } else if (isNull(stationId) && nonNull(typeId)) {
            measurementDataPoints = measurementDataPointRepository.findByMeasurementTypeId(typeId);
        } else {
            measurementDataPoints = measurementDataPointRepository.findByStationIdAndMeasurementTypeId(stationId, typeId);
        }

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

        MeasurementValue measurementValue = measurementValueRepository.save(
                new MeasurementValue(measurementRequestDto.value(), OffsetDateTime.now()));

        MeasurementDataPoint measurementDataPoint = measurementDataPointRepository.save(
                new MeasurementDataPoint(station.get(), measurementType.get(), measurementValue));

        return dataMapper.toMeasurementResponseDto(measurementDataPoint);
    }

    @Transactional
    public void createBulkMeasurements(List<MeasurementBulkRequestDto> request) {
        if (request.isEmpty()) {
            return;
        }
        Long stationId = request.getFirst().stationId();
        Long typeId = request.getFirst().typeId();

        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Station with stationId " + stationId + " not found"));

        MeasurementType measurementType = measurementTypeRepository.findById(typeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Measurement type with typeId " + typeId + " not found"));

        List<MeasurementValue> values = request.stream()
                .map(dto -> new MeasurementValue(dto.value(), dto.measuredAt().atZone(STATION_ZONE).toOffsetDateTime()))
                .toList();
        measurementValueRepository.saveAll(values);

        List<MeasurementDataPoint> points = values.stream()
                .map(value -> new MeasurementDataPoint(station, measurementType, value))
                .toList();

        measurementDataPointRepository.saveAll(points);
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
}
