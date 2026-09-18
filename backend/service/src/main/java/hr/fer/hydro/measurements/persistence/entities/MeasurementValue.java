package hr.fer.hydro.measurements.persistence.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;

@Getter
@Entity
@Table(name = "measurement_value")
@NoArgsConstructor
@AllArgsConstructor
public class MeasurementValue {

    @Id
    @SequenceGenerator(
            name = "measurement_value",
            sequenceName = "measurement_value_seq",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "measurement_value")
    private Long id;

    @Column(nullable = false)
    private Double value;

    @Column(nullable = false)
    private OffsetDateTime measuredAt;

    public MeasurementValue(Double value, OffsetDateTime measuredAt) {
        this.value = value;
        this.measuredAt = measuredAt;
    }
}
