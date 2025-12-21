package hr.fer.hydro.db;

import hr.fer.hydro.db.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserDao extends JpaRepository<UserEntity, Integer> {
    Optional<UserEntity> findByUsername(String username);
}
