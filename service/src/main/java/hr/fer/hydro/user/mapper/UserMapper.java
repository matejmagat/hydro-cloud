package hr.fer.hydro.user.mapper;

import hr.fer.hydro.auth.persistence.entities.UserEntity;
import hr.fer.hydro.user.dto.UserDetailInfoDto;
import hr.fer.hydro.user.dto.UserDto;
import hr.fer.hydro.user.dto.req.UpdateUserReq;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    UserDetailInfoDto toUserDetailInfoDto(UserEntity user);
    UserDto toUserDto(UserEntity user);

    void updateUser(@MappingTarget UserEntity user, UpdateUserReq updateUserReq);
}
