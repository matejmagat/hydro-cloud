package hr.fer.hydro;

import hr.fer.hydro.measurements.dto.MeasurementResponseDto;
import hr.fer.hydro.measurements.persistence.entities.MeasurementDataPoint;
import hr.fer.hydro.stations.dto.StationResponseDto;
import hr.fer.hydro.stations.persistence.entities.Station;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DataMapper {

    @Mapping(target = "stationId", source = "id")
    @Mapping(target = "stationName", source = "name")
    @Mapping(target = "longitude", expression = "java(station.getLocation().getX())")
    @Mapping(target = "latitude", expression = "java(station.getLocation().getY())")
    StationResponseDto toStationResponseDto(Station station);

    @Mapping(target = "measurementId", source = "id")
    @Mapping(target = "stationId", expression = "java(measurementDataPoint.getStation().getId())")
    @Mapping(target = "station", expression = "java(measurementDataPoint.getStation().getName())")
    @Mapping(target = "typeId", expression = "java(measurementDataPoint.getMeasurementType().getId())")
    @Mapping(target = "type", expression = "java(measurementDataPoint.getMeasurementType().getName())")
    @Mapping(target = "unit", expression = "java(measurementDataPoint.getMeasurementType().getUnit())")
    @Mapping(target = "value", expression = "java(measurementDataPoint.getMeasurementValue().getValue())")
    @Mapping(target = "measuredAt", expression = "java(measurementDataPoint.getMeasurementValue().getMeasuredAt())")
    MeasurementResponseDto toMeasurementResponseDto(MeasurementDataPoint measurementDataPoint);
}
