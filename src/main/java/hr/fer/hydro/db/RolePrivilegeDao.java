package hr.fer.hydro.db;

import hr.fer.hydro.db.entity.RolePrivilegeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolePrivilegeDao extends JpaRepository<RolePrivilegeEntity, Integer> {
}
