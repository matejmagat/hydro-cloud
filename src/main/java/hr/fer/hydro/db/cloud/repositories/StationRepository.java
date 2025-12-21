package hr.fer.hydro.db.cloud.repositories;

import hr.fer.hydro.db.cloud.entities.Station;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StationRepository extends JpaRepository<Station, Long> {
}
