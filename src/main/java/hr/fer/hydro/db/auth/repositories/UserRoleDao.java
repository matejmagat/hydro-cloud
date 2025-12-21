package hr.fer.hydro.db.auth.repositories;

import hr.fer.hydro.db.auth.entities.UserEntity;
import hr.fer.hydro.db.auth.entities.UserRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRoleDao extends JpaRepository<UserRoleEntity, Integer> {
    List<UserRoleEntity> findAllByUser(UserEntity user);
}
