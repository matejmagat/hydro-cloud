package hr.fer.hydro.db;

import hr.fer.hydro.db.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
public interface UserDao extends JpaRepository<UserEntity, Integer> {
}
