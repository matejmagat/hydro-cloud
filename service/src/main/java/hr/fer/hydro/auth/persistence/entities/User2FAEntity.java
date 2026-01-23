package hr.fer.hydro.auth.persistence.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "user_2fa")
public class User2FAEntity {
    @Id
    @Column(name = "user_2fa_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Size(max = 128)
    @NotNull
    @Column(name = "secret", nullable = false, length = 128)
    private String secret;

    @NotNull
    @Column(name = "validation_code", nullable = false)
    private Integer validationCode;

    @NotNull
    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt = LocalDateTime.now();

    @NotNull
    @Column(name = "CONFIRMED")
    private Boolean confirmed = Boolean.FALSE;
}