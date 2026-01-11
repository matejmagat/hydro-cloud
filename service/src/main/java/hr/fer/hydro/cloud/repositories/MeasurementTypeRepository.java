package hr.fer.hydro.cloud.repositories;

import hr.fer.hydro.cloud.entities.MeasurementType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeasurementTypeRepository extends JpaRepository<MeasurementType, Long> {
}
