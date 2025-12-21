package hr.fer.hydro.db.auth.repositories;

import hr.fer.hydro.db.auth.entities.User2FAScratchCodeEntity;
import hr.fer.hydro.db.auth.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;

public interface User2FAScratchCodeDao extends JpaRepository<User2FAScratchCodeEntity, Integer> {
    List<User2FAScratchCodeEntity> findAllByUser(UserEntity user);
    @Modifying
    void deleteAllByUser(UserEntity user);
}
