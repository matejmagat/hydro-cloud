package hr.fer.hydro.user.api.rest;

import hr.fer.hydro.user.dto.UserDetailInfoDto;
import hr.fer.hydro.user.dto.UserDto;
import hr.fer.hydro.user.dto.req.UpdateUserReq;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/user")
public interface UserApi {
    @DeleteMapping("/{userId}")
    void deleteUser(@PathVariable Integer userId);

    @PutMapping
    ResponseEntity<UserDetailInfoDto> updateUser(@RequestBody UpdateUserReq updateUserReq);

    @GetMapping("/{userId}")
    ResponseEntity<UserDetailInfoDto> getDetailedUserInfo(@PathVariable Integer userId);

    @GetMapping("/all-users")
    ResponseEntity<List<UserDto>> getAllUsers();
}
