package ru.thecntgfy.libooker.model;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import ru.thecntgfy.libooker.dto.RegisterDTO;
import ru.thecntgfy.libooker.security.UserPrincipal;

import java.util.List;

public class UserMapper {
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public static UserPrincipal userToPrincipal(User user) {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole());

        UserPrincipal userPrincipal = new UserPrincipal(
                user.getUsername(),
                user.getPassword(),
                List.of(authority)
        );

        userPrincipal.setEnabled(true);

        return userPrincipal;
    }

    public static User registerDtoToUser(RegisterDTO dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(encoder.encode(dto.getPassword()));
        user.setRole(dto.getRole());

        return user;
    }
}
