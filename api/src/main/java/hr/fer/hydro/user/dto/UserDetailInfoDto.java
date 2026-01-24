package hr.fer.hydro.user.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class UserDetailInfoDto {
    private Integer id;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private boolean is2FAEnabled;
}
