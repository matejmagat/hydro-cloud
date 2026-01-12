package hr.fer.hydro.stations;

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
}
