package hr.fer.hydro.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "role", schema = "user_management")
public class RoleEntity {
    @Id
    @Column(name = "role_id", nullable = false)
    private Integer id;

    @Column(name = "role_name", nullable = false, length = 32)
    private String roleName;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "description", length = 256)
    private String description;

}