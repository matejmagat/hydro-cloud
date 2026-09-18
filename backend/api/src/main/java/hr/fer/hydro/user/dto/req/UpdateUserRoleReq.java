package hr.fer.hydro.user.dto.req;

import hr.fer.hydro.user.api.rest.Role;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserRoleReq {
    @NotNull(message = "New role is required")
    private Role role;
    @NotNull(message = "User id is required")
    private Integer userId;
}
