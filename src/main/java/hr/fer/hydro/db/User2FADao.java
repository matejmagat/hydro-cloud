package hr.fer.hydro.db;

import hr.fer.hydro.db.entity.User2FAEntity;
import hr.fer.hydro.db.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.Optional;

public interface User2FADao extends JpaRepository<User2FAEntity, Integer> {
    Optional<User2FAEntity> findByUser(UserEntity user);
    @Modifying
    void deleteAllByUser(UserEntity user);
}
