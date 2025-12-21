package hr.fer.hydro.db.cloud.repositories;

import hr.fer.hydro.db.cloud.entities.MeasurementValue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeasurementValueRepository extends JpaRepository<MeasurementValue, Long> {
}
