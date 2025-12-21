package hr.fer.hydro.db.auth.repositories;

import hr.fer.hydro.db.auth.entities.RoleEntity;
import hr.fer.hydro.db.auth.entities.RolePrivilegeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RolePrivilegeDao extends JpaRepository<RolePrivilegeEntity, Integer> {
    List<RolePrivilegeEntity> findByRole(RoleEntity roleEntity);
}
