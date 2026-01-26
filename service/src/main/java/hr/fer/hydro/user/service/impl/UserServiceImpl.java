package hr.fer.hydro.user.service.impl;

import hr.fer.hydro.auth.persistence.entities.UserEntity;
import hr.fer.hydro.auth.persistence.repositories.UserDao;
import hr.fer.hydro.config.core.UserCoreLocalThread;
import hr.fer.hydro.user.dto.UserDetailInfoDto;
import hr.fer.hydro.user.dto.UserDto;
import hr.fer.hydro.user.dto.req.UpdateUserReq;
import hr.fer.hydro.user.mapper.UserMapper;
import hr.fer.hydro.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

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
        return userMapper.toUserDetailInfoDto(new UserDetailInfoDto(),user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetailInfoDto getLoggedInUserInfo() {
        final UserEntity loggedUser = getUserEntity(UserCoreLocalThread.getUserId());
        return userMapper.toUserDetailInfoDto(new UserDetailInfoDto(),loggedUser);
    }

    private UserEntity getUserEntity(Integer userId) {
        return userDao.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("User not found!")
        );
    }
}
