package hr.fer.hydro.measurements.persistence.entities;

import hr.fer.hydro.stations.persistence.entities.Station;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Entity
@NoArgsConstructor
public class MeasurementDataPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "measurement_type_id", nullable = false)
    private MeasurementType measurementType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "measurement_value_id", nullable = false)
    private MeasurementValue measurementValue;

    public MeasurementDataPoint(Station station,
                                MeasurementType measurementType,
                                MeasurementValue measurementValue) {
        this.station = station;
        this.measurementType = measurementType;
        this.measurementValue = measurementValue;
    }
}
