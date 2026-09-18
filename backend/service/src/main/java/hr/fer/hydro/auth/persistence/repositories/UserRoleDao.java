package hr.fer.hydro.auth.persistence.repositories;

import hr.fer.hydro.auth.persistence.entities.UserEntity;
import hr.fer.hydro.auth.persistence.entities.UserRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRoleDao extends JpaRepository<UserRoleEntity, Integer> {
    Optional<UserRoleEntity> findByUser(UserEntity user);
}
