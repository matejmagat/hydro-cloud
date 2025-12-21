package hr.fer.hydro.db.auth.repositories;

import hr.fer.hydro.db.auth.entities.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleDao extends JpaRepository<RoleEntity, Integer> {
}
