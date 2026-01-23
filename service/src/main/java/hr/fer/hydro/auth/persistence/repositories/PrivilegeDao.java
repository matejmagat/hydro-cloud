package hr.fer.hydro.auth.persistence.repositories;

import hr.fer.hydro.auth.persistence.entities.PrivilegeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrivilegeDao extends JpaRepository<PrivilegeEntity, Integer> {
}
