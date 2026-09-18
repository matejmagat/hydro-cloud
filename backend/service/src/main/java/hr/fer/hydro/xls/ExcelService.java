package hr.fer.hydro.xls;

import com.fasterxml.jackson.databind.ObjectMapper;
import hr.fer.hydro.measurements.dto.MeasurementBulkRequestDto;
import hr.fer.hydro.measurements.persistence.entities.MeasurementType;
import hr.fer.hydro.measurements.persistence.repositories.MeasurementTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelService {

    private final MeasurementTypeRepository measurementTypeRepository;
    private final ObjectMapper objectMapper;
    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public List<MeasurementBulkRequestDto> importMeasurements(byte[] bytes) {
        DataFormatter formatter = new DataFormatter();
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
            Sheet sheet = workbook.getSheetAt(0);

            Row headerKeys = sheet.getRow(0);
            Row headerValues = sheet.getRow(1);
            if (headerKeys == null || headerValues == null) {
                throw new IllegalArgumentException("Missing header rows");
            }

            Map<String, String> header = new HashMap<>();
            for (int i = 0; i < headerKeys.getLastCellNum(); i++) {
                Cell keyCell = headerKeys.getCell(i);
                Cell valueCell = headerValues.getCell(i);

                if (keyCell == null || valueCell == null) continue;

                header.put(formatter.formatCellValue(keyCell).trim(), formatter.formatCellValue(valueCell).trim());
            }

            Long stationId = Long.parseLong(header.get("id"));
            String measurementTypeName = header.get("Measurement type");

            MeasurementType measurementType = measurementTypeRepository.findByName(measurementTypeName)
                            .orElseThrow(() -> new ResponseStatusException(
                                    HttpStatus.NOT_FOUND, "Measurement type with typeId " + measurementTypeName + " not found"));

            int dataHeaderRow = -1;
            for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Cell cell = row.getCell(0);
                if (cell == null) continue;

                String cellValue = formatter.formatCellValue(cell).trim();
                if ("datetime".equalsIgnoreCase(cellValue)) {
                    dataHeaderRow = i;
                    break;
                }
            }

            if (dataHeaderRow == -1) {
                throw new IllegalArgumentException("Missing 'datetime' header row");
            }

            List<MeasurementBulkRequestDto> result = new ArrayList<>();
            for (int i = dataHeaderRow + 1; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);
                if (row == null) continue;

                Cell datetimeCell = row.getCell(0);
                Cell valueCell = row.getCell(1);

                if (datetimeCell == null || valueCell == null) continue;

                LocalDateTime measuredAt;
                if (DateUtil.isCellDateFormatted(datetimeCell)) {
                    measuredAt = datetimeCell.getLocalDateTimeCellValue();
                } else {
                    measuredAt = LocalDateTime.parse(formatter.formatCellValue(datetimeCell).trim(), DT_FORMAT);
                }

                Double value = Double.parseDouble(formatter.formatCellValue(valueCell).trim());

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
            throw new RuntimeException("Failed to import Excel file", e);
        }
    }
}
