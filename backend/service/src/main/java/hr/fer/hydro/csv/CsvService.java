package hr.fer.hydro.csv;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import hr.fer.hydro.measurements.dto.MeasurementBulkRequestDto;
import hr.fer.hydro.measurements.persistence.entities.MeasurementType;
import hr.fer.hydro.measurements.persistence.repositories.MeasurementTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class CsvService {

    private final ObjectMapper objectMapper;
    private final MeasurementTypeRepository measurementTypeRepository;
    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public List<MeasurementBulkRequestDto> importMeasurements(byte[] csvFile) {
        try (CSVReader reader = new CSVReader(new InputStreamReader(
                        new ByteArrayInputStream(csvFile),
                        StandardCharsets.UTF_8)
        )) {
            List<String[]> rows = reader.readAll();

            String[] headerKeys = rows.get(0);
            String[] headerValues = rows.get(1);

            Map<String, String> header = new HashMap<>();
            for (int i = 0; i < headerKeys.length; i++) {
                header.put(headerKeys[i].trim(), headerValues[i].trim());
            }

            Long stationId = Long.parseLong(header.get("id"));
            String measurementTypeName = header.get("Measurement type");

            MeasurementType measurementType = measurementTypeRepository
                    .findByName(measurementTypeName)
                    .orElseThrow(() -> new ResponseStatusException(
                                    HttpStatus.NOT_FOUND, "Measurement type with typeId " + measurementTypeName + " not found"));

            List<MeasurementBulkRequestDto> result = new ArrayList<>();
            for (int i = 4; i < rows.size(); i++) {
                String[] row = rows.get(i);
                if (row.length < 2 || row[0].isBlank()) {
                    continue;
                }
                LocalDateTime measuredAt =
                        LocalDateTime.parse(row[0].trim(), DT_FORMAT);

                Double value = Double.parseDouble(row[1].trim());

                result.add(new MeasurementBulkRequestDto(
                        stationId,
                        measurementType.getId(),
                        value,
                        measuredAt
                ));
            }

            log.info(objectMapper.writeValueAsString(result));
            return result;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return List.of();
    }
}
