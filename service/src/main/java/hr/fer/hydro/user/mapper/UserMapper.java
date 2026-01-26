package hr.fer.hydro.user.mapper;

import hr.fer.hydro.auth.persistence.entities.UserEntity;
import hr.fer.hydro.auth.persistence.entities.UserRoleEntity;
import hr.fer.hydro.auth.persistence.repositories.UserRoleDao;
import hr.fer.hydro.user.api.rest.Role;
import hr.fer.hydro.user.dto.UserDetailInfoDto;
import hr.fer.hydro.user.dto.UserDto;
import hr.fer.hydro.user.dto.req.UpdateUserReq;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class UserMapper {
    @Autowired
    private UserRoleDao userRoleDao;
    public abstract UserDetailInfoDto toUserDetailInfoDto(@MappingTarget UserDetailInfoDto userDetailInfoDto, UserEntity user);
    @AfterMapping
    protected void addUserRole(@MappingTarget UserDetailInfoDto userDetailInfoDto, UserEntity user){
        UserRoleEntity userRoleEntity = userRoleDao.findByUser(user).orElseThrow();
        userDetailInfoDto.setRole(Role.getRole(userRoleEntity.getRole().getRoleName()));
    }
    public abstract UserDto toUserDto(UserEntity user);

    public abstract void updateUser(@MappingTarget UserEntity user, UpdateUserReq updateUserReq);
}
