package hr.fer.hydro.measurements.persistence.repositories;

import hr.fer.hydro.measurements.dto.MeasurementTypeCountDto;
import hr.fer.hydro.measurements.persistence.entities.MeasurementDataPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface MeasurementDataPointRepository extends JpaRepository<MeasurementDataPoint, Long> {

    public List<MeasurementDataPoint> findByStationId(Long stationId);

    public List<MeasurementDataPoint> findByMeasurementTypeId(Long typeId);

    public List<MeasurementDataPoint> findByStationIdAndMeasurementTypeId(Long stationId, Long typeId);

    @Query("SELECT m FROM MeasurementDataPoint m WHERE " +
            "(:stationId IS NULL OR m.station.id = :stationId) AND " +
            "(:typeId IS NULL OR m.measurementType.id = :typeId) AND " +
            "(cast(:fromDate as timestamp) IS NULL OR m.measurementValue.measuredAt >= :fromDate) AND " +
            "(cast(:toDate as timestamp) IS NULL OR m.measurementValue.measuredAt <= :toDate)")
    List<MeasurementDataPoint> findWithFilters(
            @Param("stationId") Long stationId,
            @Param("typeId") Long typeId,
            @Param("fromDate") OffsetDateTime fromDate,
            @Param("toDate") OffsetDateTime toDate
    );

    @Query("SELECT new hr.fer.hydro.measurements.dto.MeasurementTypeCountDto(" +
            "m.measurementType.id, " +
            "m.measurementType.name, " +
            "COUNT(m)) " +
            "FROM MeasurementDataPoint m WHERE " +
            "(:stationId IS NULL OR m.station.id = :stationId) AND " +
            "(:typeId IS NULL OR m.measurementType.id = :typeId) AND " +
            "(cast(:fromDate as timestamp) IS NULL OR m.measurementValue.measuredAt >= :fromDate) AND " +
            "(cast(:toDate as timestamp) IS NULL OR m.measurementValue.measuredAt <= :toDate) " +
            "GROUP BY m.measurementType.id, m.measurementType.name")
    List<MeasurementTypeCountDto> countTypesWithFilters(
            @Param("stationId") Long stationId,
            @Param("typeId") Long typeId,
            @Param("fromDate") OffsetDateTime fromDate,
            @Param("toDate") OffsetDateTime toDate
    );

}
