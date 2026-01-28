package hr.fer.hydro.stations;

import hr.fer.hydro.stations.api.rest.StationsApi;
import hr.fer.hydro.stations.dto.StationRequestDto;
import hr.fer.hydro.stations.dto.StationResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class StationsController implements StationsApi {

    private final StationsService stationsService;

    @Override
    public ResponseEntity<List<StationResponseDto>> getStations(String search) {
        return ResponseEntity
                .ok(stationsService.getStations(search));
    }

    @Override
    public ResponseEntity<StationResponseDto> createStation(StationRequestDto stationRequestDto) {
        StationResponseDto stationResponseDto = stationsService.createStation(stationRequestDto);
        return ResponseEntity
                .created(URI.create("/stations/" + stationResponseDto.stationId()))
                .body(stationResponseDto);
    }

    @Override
    public ResponseEntity<StationResponseDto> getStation(Long stationId) {
        return ResponseEntity
                .ok(stationsService.getStation(stationId));
    }

    @Override
    public void deleteStation(Long stationId) {
        stationsService.deleteStation(stationId);
    }
}
