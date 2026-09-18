package hr.fer.hydro.auth.persistence.repositories;

import hr.fer.hydro.auth.persistence.entities.RoleEntity;
import hr.fer.hydro.auth.persistence.entities.RolePrivilegeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RolePrivilegeDao extends JpaRepository<RolePrivilegeEntity, Integer> {
    List<RolePrivilegeEntity> findByRole(RoleEntity roleEntity);
}
