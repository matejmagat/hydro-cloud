package hr.fer.hydro.db;

import hr.fer.hydro.db.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleDao extends JpaRepository<RoleEntity, Integer> {
}
