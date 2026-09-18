package hr.fer.hydro.user.api.rest;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public enum Role {
    ADMIN,
    USER_MANAGER,
    DATA_MANAGER,
    USER;

    private static final Map<String, Role> roleMap = Arrays.stream(values())
            .collect(Collectors.toMap(Enum::name, Function.identity()));

    public static Role getRole(String roleName){
        return Optional.ofNullable(roleMap.get(roleName)).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Nepoznat role")
        );
    }

}
