package hr.fer.hydro.mapper;

import hr.fer.hydro.api.auth.SignUpReq;
import hr.fer.hydro.db.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface AuthMapper {
    @Mapping(target = "password", ignore = true)
    UserEntity toUserEntity(SignUpReq signUpReq);
}
