package hr.fer.hydro.stations.persistence.repositories;

import hr.fer.hydro.stations.persistence.entities.Station;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StationRepository extends JpaRepository<Station, Long> {
}
