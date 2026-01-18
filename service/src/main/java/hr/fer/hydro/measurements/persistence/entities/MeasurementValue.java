package hr.fer.hydro.measurements.persistence.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Getter
@Entity
@NoArgsConstructor
public class MeasurementValue {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false)
    private Double value;

    @CreationTimestamp
    @Column(nullable = false)
    private OffsetDateTime measuredAt;

    public MeasurementValue(Double value) {
        this.value = value;
    }
}
