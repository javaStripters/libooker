package ru.thecntgfy.libooker.dto;

import lombok.Data;
import ru.thecntgfy.libooker.model.Role;

@Data
public class JwtResponse {
    String token;
    String tokenHeader = "Bearer";
    Role role;

    public JwtResponse(String token, Role role) {
        this.token = token;
        this.role = role;
    }
}
