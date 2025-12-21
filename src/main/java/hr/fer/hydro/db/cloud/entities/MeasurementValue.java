package hr.fer.hydro.db.cloud.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.OffsetDateTime;

@Getter
@Entity
@NoArgsConstructor
public class MeasurementValue {

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "data_id", nullable = false)
    private MeasurementDataPoint dataPoint;

    @Column(nullable = false)
    private Long value;

    @CreationTimestamp
    @Column(nullable = false)
    private OffsetDateTime measuredAt;

    public MeasurementValue(MeasurementDataPoint dataPoint,
                            Long value) {
        this.dataPoint = dataPoint;
        this.value = value;
    }
}
