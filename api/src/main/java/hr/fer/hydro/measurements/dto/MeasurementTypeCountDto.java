package hr.fer.hydro.measurements.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeasurementTypeCountDto {

    @JsonProperty("measurement_type_id")
    private Long measurementTypeId;

    @JsonProperty("measurement_type_name")
    private String measurementTypeName;

    @JsonProperty("n_occurrences_in_filtered_data")
    private Long nOccurrencesInFilteredData;
}
