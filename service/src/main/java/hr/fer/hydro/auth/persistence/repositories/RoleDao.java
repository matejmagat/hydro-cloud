package hr.fer.hydro.auth.persistence.repositories;

import hr.fer.hydro.auth.persistence.entities.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleDao extends JpaRepository<RoleEntity, Integer> {
}
