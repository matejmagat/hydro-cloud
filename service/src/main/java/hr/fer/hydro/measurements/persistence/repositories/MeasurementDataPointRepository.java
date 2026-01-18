package hr.fer.hydro.measurements.persistence.repositories;

import hr.fer.hydro.measurements.persistence.entities.MeasurementDataPoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MeasurementDataPointRepository extends JpaRepository<MeasurementDataPoint, Long> {

    public List<MeasurementDataPoint> findByStationId(Long stationId);

    public List<MeasurementDataPoint> findByMeasurementTypeId(Long typeId);

    public List<MeasurementDataPoint> findByStationIdAndMeasurementTypeId(Long stationId, Long typeId);
}
