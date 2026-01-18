package hr.fer.hydro.measurements.persistence.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
public class MeasurementType {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false, unique = true, length = 32)
    private String name;

    @Column(nullable = false)
    private String unit;

    @Column
    private String description;
}
