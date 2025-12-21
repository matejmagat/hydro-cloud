package hr.fer.hydro.enums;

import lombok.Getter;

@Getter
public enum Roles {
    ADMIN,
    USERS;

    public String getRole() {
        return String.format("ROLE_%s", this.name());
    }
}
