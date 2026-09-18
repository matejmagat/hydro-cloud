package hr.fer.hydro.auth.mapper;

import hr.fer.hydro.auth.auth.dto.RegisterReq;
import hr.fer.hydro.auth.persistence.entities.User2FAEntity;
import hr.fer.hydro.auth.persistence.entities.User2FAScratchCodeEntity;
import hr.fer.hydro.auth.persistence.entities.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface AuthMapper {
    @Mapping(target = "password", ignore = true)
    UserEntity toUserEntity(RegisterReq signUpReq);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", expression = "java(user)")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "confirmed", expression = "java(Boolean.FALSE)")
    User2FAEntity toUser2FAEntity(UserEntity user, String secret, Integer validationCode);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", expression = "java(user)")
    User2FAScratchCodeEntity toUser2FAScratchCodeEntity(UserEntity user, Integer code);
}
