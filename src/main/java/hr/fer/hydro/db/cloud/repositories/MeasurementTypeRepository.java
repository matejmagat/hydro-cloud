package hr.fer.hydro.db.cloud.repositories;

import hr.fer.hydro.db.cloud.entities.MeasurementType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeasurementTypeRepository extends JpaRepository<MeasurementType, Long> {
}
