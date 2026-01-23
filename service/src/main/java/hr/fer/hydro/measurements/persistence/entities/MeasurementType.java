package hr.fer.hydro.measurements.persistence.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "measurement_type")
@NoArgsConstructor
@AllArgsConstructor
public class MeasurementType {

    @Id
    @SequenceGenerator(
            name = "measurement_type",
            sequenceName = "measurement_type_seq",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "measurement_type")
    private Long id;

    @Column(nullable = false, unique = true, length = 32)
    private String name;

    @Column(nullable = false)
    private String unit;

    @Column
    private String description;
}
