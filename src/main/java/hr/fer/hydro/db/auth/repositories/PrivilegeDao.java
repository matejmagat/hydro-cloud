package hr.fer.hydro.db.auth.repositories;

import hr.fer.hydro.db.auth.entities.PrivilegeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrivilegeDao extends JpaRepository<PrivilegeEntity, Integer> {
}
