package hr.fer.hydro.user.service.impl;

import hr.fer.hydro.auth.persistence.entities.UserEntity;
import hr.fer.hydro.auth.persistence.entities.UserRoleEntity;
import hr.fer.hydro.auth.persistence.repositories.RoleDao;
import hr.fer.hydro.auth.persistence.repositories.UserDao;
import hr.fer.hydro.auth.persistence.repositories.UserRoleDao;
import hr.fer.hydro.config.core.UserCoreLocalThread;
import hr.fer.hydro.user.api.rest.Role;
import hr.fer.hydro.user.dto.UserDetailInfoDto;
import hr.fer.hydro.user.dto.UserDto;
import hr.fer.hydro.user.dto.req.UpdateUserReq;
import hr.fer.hydro.user.dto.req.UpdateUserRoleReq;
import hr.fer.hydro.user.mapper.UserMapper;
import hr.fer.hydro.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRoleDao userRoleDao;
    private final RoleDao roleDao;

    private final UserDao userDao;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public void deleteUser(Integer userId) {
        UserEntity user = getUserEntity(userId);
        userDao.delete(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetailInfoDto getUserInfo(Integer userId) {
        UserEntity user = getUserEntity(userId);
        return userMapper.toUserDetailInfoDto(new UserDetailInfoDto(), user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userDao.findAll().stream()
                .map(userMapper::toUserDto)
                .toList();
    }

    @Override
    @Transactional
    public UserDetailInfoDto updateUser(UpdateUserReq updateUserReq) {
        UserEntity user = getUserEntity(updateUserReq.getId());
        userMapper.updateUser(user, updateUserReq);
        return userMapper.toUserDetailInfoDto(new UserDetailInfoDto(), user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetailInfoDto getLoggedInUserInfo() {
        final UserEntity loggedUser = getUserEntity(UserCoreLocalThread.getUserId());
        return userMapper.toUserDetailInfoDto(new UserDetailInfoDto(), loggedUser);
    }

    @Override
    @Transactional
    public void updateUserRole(UpdateUserRoleReq updateUserRoleReq) {
        final UserEntity currentUser = getUserEntity(UserCoreLocalThread.getUserId());
        final UserEntity userToUpdate = getUserEntity(updateUserRoleReq.getUserId());
        validateRoleChange(currentUser, userToUpdate);
        roleDao.findByRoleName(updateUserRoleReq.getRole().name()).ifPresent(
                role -> {
                    userRoleDao.findByUser(userToUpdate).ifPresent(userRoleDao::delete);

                    final UserRoleEntity userRoleEntity = new UserRoleEntity();
                    userRoleEntity.setCreatedAt(LocalDateTime.now());
                    userRoleEntity.setUser(userToUpdate);
                    userRoleEntity.setRole(role);
                    userRoleDao.save(userRoleEntity);
                }
        );
    }

    private void validateRoleChange(UserEntity currentUser, UserEntity userToUpdate) {
        Role currentUserRole = Role.getRole(userRoleDao.findByUser(currentUser).orElseThrow().getRole().getRoleName());
        Role userToUpdateRole = Role.getRole(userRoleDao.findByUser(userToUpdate).orElseThrow().getRole().getRoleName());

        if (currentUserRole.equals(userToUpdateRole)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not enough rights");
        }

        if (userToUpdateRole.equals(Role.ADMIN)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not enough rights");
        }
    }

    private UserEntity getUserEntity(Integer userId) {
        return userDao.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("User not found!")
        );
    }
}
