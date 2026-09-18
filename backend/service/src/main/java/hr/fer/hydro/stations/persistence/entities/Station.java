package hr.fer.hydro.stations.persistence.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.geom.Point;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "station")
@NoArgsConstructor
@AllArgsConstructor
public class Station {

    @SequenceGenerator(
            name = "station_seq",
            sequenceName = "station_seq",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "station_seq")
    @Id
    private Long id;

    @Column(nullable = false, unique = true, length = 32)
    private String name;

    @Column(nullable = false, columnDefinition = "geometry(Point,4326)")
    private Point location;

    @Column(nullable = false)
    private OffsetDateTime activeFrom;

    @Column(nullable = false)
    private OffsetDateTime activeTo;

    public Station(String name,
                   Point location,
                   OffsetDateTime activeFrom,
                   OffsetDateTime activeTo) {
        this.name = name;
        this.location = location;
        this.activeFrom = activeFrom;
        this.activeTo = activeTo;
    }
}
