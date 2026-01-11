package hr.fer.hydro.cloud.repositories;

import hr.fer.hydro.cloud.entities.Station;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StationRepository extends JpaRepository<Station, Long> {
}
