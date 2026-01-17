package hr.fer.hydro.stations.api.rest;

import hr.fer.hydro.stations.dto.StationRequestDto;
import hr.fer.hydro.stations.dto.StationResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(summary = "Dohvat svih meteoroloških stanica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Popis meteoroloških stanica uspješno dohvaćen.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = StationResponseDto.class)))),
            @ApiResponse(responseCode = "403", description = "Zabranjen pristup.")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<StationResponseDto>> getStations();

    @Operation(summary = "Kreira novu meteorološku stanicu")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Meteorološka stanica uspješno kreirana.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = StationResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Neispravan zahtjev."),
            @ApiResponse(responseCode = "403", description = "Zabranjen pristup.")
    })
    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<StationResponseDto> createStation(
            @RequestBody StationRequestDto stationRequestDto
    );

    @Operation(summary = "Dohvat meteorološke stanice po ID-u")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Meteorološka stanica pronađena.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = StationResponseDto.class))
            ),
            @ApiResponse(responseCode = "403", description = "Zabranjen pristup."),
            @ApiResponse(responseCode = "404", description = "Meteorološka stanica s danim ID-jem ne postoji.")
    })
    @GetMapping(value = "/{stationId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<StationResponseDto> getStation(
            @Parameter(description = "ID meteorološke stanice", required = true, example = "42")
            @PathVariable Long stationId
    );

    @Operation(summary = "Obriši meteorološku stanicu po ID-u")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Meteorološka stanica uspješno obrisana."),
            @ApiResponse(responseCode = "403", description = "Zabranjen pristup."),
            @ApiResponse(responseCode = "404", description = "Meteorološka stanica s danim ID-jem ne postoji.")

    })
    @DeleteMapping(value = "/{stationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteStation(
            @Parameter(description = "ID meteorološke stanice", required = true, example = "42")
            @PathVariable Long stationId
    );
}
