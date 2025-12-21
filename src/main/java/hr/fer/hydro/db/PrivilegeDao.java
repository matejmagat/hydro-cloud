package hr.fer.hydro.db;

import hr.fer.hydro.db.entity.PrivilegeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrivilegeDao extends JpaRepository<PrivilegeEntity, Integer> {
}
