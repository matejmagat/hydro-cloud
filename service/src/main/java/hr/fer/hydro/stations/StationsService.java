package hr.fer.hydro.stations;

import hr.fer.hydro.stations.dto.StationRequestDto;
import hr.fer.hydro.stations.dto.StationResponseDto;
import hr.fer.hydro.stations.persistence.entities.Station;
import hr.fer.hydro.stations.persistence.repositories.StationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StationsService {

    private final StationRepository stationRepository;
    private final DataMapper dataMapper;

    public List<StationResponseDto> getStations() {
        List<Station> stations = stationRepository.findAll();

        return stations.stream()
                .map(dataMapper::toStationResponseDto)
                .toList();
    }

    @Transactional
    public StationResponseDto createStation(StationRequestDto stationRequestDto) {
        GeometryFactory gf = new GeometryFactory(new PrecisionModel(), 4326);
        Point location = gf.createPoint(
                new Coordinate(stationRequestDto.longitude(), stationRequestDto.latitude())
        );

        Station station = new Station(
                stationRequestDto.stationName(),
                location,
                OffsetDateTime.now(),
                OffsetDateTime.now().plusYears(10)
        );
        stationRepository.save(station);

        return dataMapper.toStationResponseDto(station);
    }

    public StationResponseDto getStation(Long stationId) {
        Optional<Station> station = stationRepository.findById(stationId);

        return station.map(dataMapper::toStationResponseDto)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Station with stationId " + stationId + " not found"
                ));
    }

    @Transactional
    public void deleteStation(Long stationId) {
        if (!stationRepository.existsById(stationId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Station with stationId " + stationId + " not found"
            );
        }
        stationRepository.deleteById(stationId);
    }
}
