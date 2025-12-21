package hr.fer.hydro.db.auth.entities;

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
@Table(name = "privilege", schema = "auth")
public class PrivilegeEntity {
    @Id
    @Column(name = "privilege_id", nullable = false)
    private Integer id;

    @Column(name = "privilege_name", nullable = false, length = 32)
    private String privilegeName;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "description", length = 256)
    private String description;

}