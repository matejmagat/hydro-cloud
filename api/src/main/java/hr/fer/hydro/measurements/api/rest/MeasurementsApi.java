package hr.fer.hydro.measurements.api.rest;

import hr.fer.hydro.measurements.dto.MeasurementRequestDto;
import hr.fer.hydro.measurements.dto.MeasurementResponseDto;
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
@Tag(name = "Measurements", description = "Operacije za stvaranje, dohvaćanje i brisanje meteoroloških mjerenja.")
@RequestMapping(value = "/measurements")
public interface MeasurementsApi {

    @Operation(summary = "Dohvat svih meteoroloških mjerenja")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Popis meteoroloških mjerenja uspješno dohvaćen.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = StationResponseDto.class)))),
            @ApiResponse(responseCode = "400", description = "Neispravan zahtjev."),
            @ApiResponse(responseCode = "403", description = "Zabranjen pristup.")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<MeasurementResponseDto>> getMeasurements(
            @RequestParam(required = false) Long stationId,

            @RequestParam(required = false) Long typeId
    );

    @Operation(summary = "Kreira novo meteorološko mjerenje")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Meteorološka stanica uspješno kreirana.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MeasurementResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Neispravan zahtjev."),
            @ApiResponse(responseCode = "403", description = "Zabranjen pristup."),
            @ApiResponse(responseCode = "404", description = "Entiteti s navedenim ID-jem ne postoje.")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<MeasurementResponseDto> createMeasurement(
            @RequestBody MeasurementRequestDto measurementRequestDto
    );

    @Operation(summary = "Dohvat meteorološkog mjerenja po ID-u")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Meteorološko mjerenje pronađeno.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = StationResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Neispravan zahtjev."),
            @ApiResponse(responseCode = "403", description = "Zabranjen pristup."),
            @ApiResponse(responseCode = "404", description = "Meteorološko mjerenje s danim ID-jem ne postoji.")
    })
    @GetMapping(value = "/{measurementId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<MeasurementResponseDto> getMeasurement(
            @Parameter(description = "ID meteorološkog mjerenja", required = true, example = "57")
            @PathVariable Long measurementId
    );

    @Operation(summary = "Obriši meteorološko mjerenje po ID-u")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Meteorološko mjerenje uspješno obrisano."),
            @ApiResponse(responseCode = "400", description = "Neispravan zahtjev."),
            @ApiResponse(responseCode = "403", description = "Zabranjen pristup."),
            @ApiResponse(responseCode = "404", description = "Meteorološko mjerenje s danim ID-jem ne postoji.")

    })
    @DeleteMapping(value = "/{measurementId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteMeasurement(
            @Parameter(description = "ID meteorološkog mjerenja", required = true, example = "57")
            @PathVariable Long measurementId
    );
}
