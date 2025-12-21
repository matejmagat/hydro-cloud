package hr.fer.hydro.db;

import hr.fer.hydro.db.entity.RoleEntity;
import hr.fer.hydro.db.entity.RolePrivilegeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RolePrivilegeDao extends JpaRepository<RolePrivilegeEntity, Integer> {
    List<RolePrivilegeEntity> findByRole(RoleEntity roleEntity);
}
