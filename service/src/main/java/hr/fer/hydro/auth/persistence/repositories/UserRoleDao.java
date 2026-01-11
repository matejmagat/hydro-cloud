package hr.fer.hydro.auth.persistence.repositories;

import hr.fer.hydro.auth.persistence.entities.UserEntity;
import hr.fer.hydro.auth.persistence.entities.UserRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRoleDao extends JpaRepository<UserRoleEntity, Integer> {
    List<UserRoleEntity> findAllByUser(UserEntity user);
}
