package ru.thecntgfy.libooker.dto;

import lombok.Data;
import ru.thecntgfy.libooker.model.Role;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class RegisterDTO {
    @Size(max = 255, min = 3)
    private String username;
    @Size(max = 255, min = 3)
    private String password;
    @NotNull
    private Role role;
}
