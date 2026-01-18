package hr.fer.hydro.measurements.persistence.repositories;

import hr.fer.hydro.measurements.persistence.entities.MeasurementValue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeasurementValueRepository extends JpaRepository<MeasurementValue, Long> {
}
