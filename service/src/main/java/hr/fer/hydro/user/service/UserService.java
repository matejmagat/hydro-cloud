package hr.fer.hydro.user.service;

import hr.fer.hydro.user.dto.UserDetailInfoDto;
import hr.fer.hydro.user.dto.UserDto;
import hr.fer.hydro.user.dto.req.UpdateUserReq;

import java.util.List;

public interface UserService {
    void deleteUser(Integer userId);

    UserDetailInfoDto getUserInfo(Integer userId);
    List<UserDto> getAllUsers();
    UserDetailInfoDto updateUser(UpdateUserReq updateUserReq);

    UserDetailInfoDto getLoggedInUserInfo();
}
