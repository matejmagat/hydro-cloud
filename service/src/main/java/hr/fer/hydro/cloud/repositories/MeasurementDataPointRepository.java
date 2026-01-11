package hr.fer.hydro.cloud.repositories;

import hr.fer.hydro.cloud.entities.MeasurementDataPoint;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeasurementDataPointRepository extends JpaRepository<MeasurementDataPoint, Long> {
}
