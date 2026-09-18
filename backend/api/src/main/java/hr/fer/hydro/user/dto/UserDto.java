package hr.fer.hydro.user.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Integer id;
    private String username;
    private boolean isBlocked;
    private boolean is2FAEnabled;
}
