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
import hr.fer.hydro.pagination.HydroPage;
import hr.fer.hydro.stations.persistence.entities.Station;
import hr.fer.hydro.stations.persistence.repositories.StationRepository;
import hr.fer.hydro.util.HydroPageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeasurementsService {

    private final StationRepository stationRepository;
    private final MeasurementDataPointRepository measurementDataPointRepository;
    private final MeasurementTypeRepository measurementTypeRepository;
    private final MeasurementValueRepository measurementValueRepository;
    private final DataMapper dataMapper;

    public HydroPage<MeasurementResponseDto> getMeasurements(
            List<Long> stationIds,
            Long typeId,
            OffsetDateTime fromDate,
            OffsetDateTime toDate,
            Pageable pageable) {

        // The repository query now handles all NULL checks internally
        Page<MeasurementDataPoint> measurementDataPoints =
                measurementDataPointRepository.findWithFilters(stationIds, typeId, fromDate, toDate, pageable);

        return HydroPageUtil.toPage(measurementDataPoints, dataMapper::toMeasurementResponseDto);
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
            List<Long> stationIds,
            Long typeId,
            OffsetDateTime fromDate,
            OffsetDateTime toDate) {

        return measurementDataPointRepository.countTypesWithFilters(stationIds, typeId, fromDate, toDate);
    }
}
