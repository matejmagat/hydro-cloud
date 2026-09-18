package hr.fer.hydro.measurements.persistence.repositories;

import hr.fer.hydro.measurements.persistence.entities.Measurement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeasurementDao extends JpaRepository<Measurement, Long> {

}