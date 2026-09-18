package hr.fer.hydro.stations.persistence.repositories;

import hr.fer.hydro.stations.persistence.entities.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StationRepository extends JpaRepository<Station, Long> {
    List<Station> findByNameContainingIgnoreCase(String name);
}
