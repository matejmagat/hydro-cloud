package hr.fer.hydro.measurements.persistence.entities;

import hr.fer.hydro.stations.persistence.entities.Station;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Entity
@Table(name = "measurement_data_point")
@NoArgsConstructor
@AllArgsConstructor
public class MeasurementDataPoint {

    @Id
    @SequenceGenerator(
            name = "measurement_data_point",
            sequenceName = "measurement_data_point_seq",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "measurement_data_point")
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
