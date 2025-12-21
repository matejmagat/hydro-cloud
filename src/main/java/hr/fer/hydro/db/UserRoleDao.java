package hr.fer.hydro.db;

import hr.fer.hydro.db.entity.UserEntity;
import hr.fer.hydro.db.entity.UserRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRoleDao extends JpaRepository<UserRoleEntity, Integer> {
    List<UserRoleEntity> findAllByUser(UserEntity user);
}
