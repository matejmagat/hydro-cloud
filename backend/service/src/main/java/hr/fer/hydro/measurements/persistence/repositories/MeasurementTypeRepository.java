package hr.fer.hydro.measurements.persistence.repositories;

import hr.fer.hydro.measurements.persistence.entities.MeasurementType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MeasurementTypeRepository extends JpaRepository<MeasurementType, Long> {
    Optional<MeasurementType> findByName(String name);
}
