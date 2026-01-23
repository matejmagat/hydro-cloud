package hr.fer.hydro.measurements.persistence.repositories;

import hr.fer.hydro.measurements.persistence.entities.MeasurementType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeasurementTypeRepository extends JpaRepository<MeasurementType, Long> {
}
