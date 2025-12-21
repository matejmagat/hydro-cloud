package hr.fer.hydro.db.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "user_2fa_scratch_code", schema = "user_management")
public class User2FAScratchCodeEntity {
    @Id
    @Column(name = "user_2fa_scratch_code_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_2fa_scratch_code_id_gen")
    @SequenceGenerator(name = "user_2fa_scratch_code_id_gen", sequenceName = "users_2fa_scratch_code_seq")
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @NotNull
    @Column(name = "code", nullable = false)
    private Integer code;

}