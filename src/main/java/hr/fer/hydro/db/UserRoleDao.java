package hr.fer.hydro.db;

import hr.fer.hydro.db.entity.UserRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleDao extends JpaRepository<UserRoleEntity, Integer> {
}
