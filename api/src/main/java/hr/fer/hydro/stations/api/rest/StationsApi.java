package hr.fer.hydro.stations.api.rest;

import hr.fer.hydro.stations.dto.StationRequestDto;
import hr.fer.hydro.stations.dto.StationResponseDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@Tag(name = "Stations", description = "Operacije za stvaranje, dohvaćanje i brisanje meteoroloških stanica.")
@RequestMapping(value = "/stations")
public interface StationsApi {

    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<StationResponseDto>> getStations();

    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<StationResponseDto> createStation(
            @RequestBody StationRequestDto stationRequestDto
    );

    @GetMapping(value = "/{stationId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<StationResponseDto> getStation(
            @PathVariable Long stationId
    );

    @DeleteMapping(value = "/{stationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteStation(
            @PathVariable Long stationId
    );
}
