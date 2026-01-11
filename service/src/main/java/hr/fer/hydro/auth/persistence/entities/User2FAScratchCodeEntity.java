package hr.fer.hydro.auth.persistence.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "user_2fa_scratch_code", schema = "auth")
public class User2FAScratchCodeEntity {
    @Id
    @Column(name = "user_2fa_scratch_code_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @NotNull
    @Column(name = "code", nullable = false)
    private Integer code;

}