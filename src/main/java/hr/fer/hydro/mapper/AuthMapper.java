package hr.fer.hydro.mapper;

import hr.fer.hydro.api.auth.RegisterReq;
import hr.fer.hydro.db.entity.User2FAEntity;
import hr.fer.hydro.db.entity.User2FAScratchCodeEntity;
import hr.fer.hydro.db.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface AuthMapper {
    @Mapping(target = "password", ignore = true)
    UserEntity toUserEntity(RegisterReq signUpReq);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", expression = "java(user)")
    User2FAEntity toUser2FAEntity(UserEntity user, String secret, Integer validationCode);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", expression = "java(user)")
    User2FAScratchCodeEntity toUser2FAScratchCodeEntity(UserEntity user, Integer code);
}
