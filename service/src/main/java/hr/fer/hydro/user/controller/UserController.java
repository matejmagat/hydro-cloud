package hr.fer.hydro.user.controller;

import hr.fer.hydro.user.api.rest.UserApi;
import hr.fer.hydro.user.dto.UserDetailInfoDto;
import hr.fer.hydro.user.dto.UserDto;
import hr.fer.hydro.user.dto.req.UpdateUserReq;
import hr.fer.hydro.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController implements UserApi {
    private final UserService userService;
    @Override
    @PreAuthorize("hasAuthority('USER_DELETE')")
    public void deleteUser(Integer userId) {
        userService.deleteUser(userId);
    }

    @Override
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ResponseEntity<UserDetailInfoDto> updateUser(UpdateUserReq updateUserReq) {
        return ResponseEntity.ok(userService.updateUser(updateUserReq));
    }

    @Override
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<UserDetailInfoDto> getDetailedUserInfo(Integer userId) {
        return ResponseEntity.ok(userService.getUserInfo(userId));
    }

    @Override
    @PreAuthorize("hasAuthority('ALL_USER_READ')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @Override
    public ResponseEntity<UserDetailInfoDto> getLoggedInUserInfo() {
        return ResponseEntity.ok(userService.getLoggedInUserInfo());
    }
}
