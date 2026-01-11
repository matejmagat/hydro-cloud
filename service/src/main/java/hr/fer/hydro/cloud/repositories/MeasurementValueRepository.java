package hr.fer.hydro.cloud.repositories;

import hr.fer.hydro.cloud.entities.MeasurementValue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeasurementValueRepository extends JpaRepository<MeasurementValue, Long> {
}
