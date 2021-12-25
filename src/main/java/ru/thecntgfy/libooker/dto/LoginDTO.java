package ru.thecntgfy.libooker.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class LoginDTO {
    @NotBlank
    String username;
    @NotBlank
    String password;
}
