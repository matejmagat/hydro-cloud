package hr.fer.hydro.measurements.persistence.entities;

import hr.fer.hydro.stations.persistence.entities.Station;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "measurement")
public class Measurement {
    @Id
    @Column(name = "id")
    @SequenceGenerator(
            name = "measurement",
            sequenceName = "measurement_seq",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "measurement")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "measurement_type_id", nullable = false)
    private MeasurementType measurementType;

    @Column(name = "measured_at")
    private LocalDateTime measuredAt;

    @NotNull
    @Column(name = "value", nullable = false)
    private Double value;

}