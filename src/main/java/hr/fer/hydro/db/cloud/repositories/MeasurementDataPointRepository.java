package hr.fer.hydro.db.cloud.repositories;

import hr.fer.hydro.db.cloud.entities.MeasurementDataPoint;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeasurementDataPointRepository extends JpaRepository<MeasurementDataPoint,Long> {
}
