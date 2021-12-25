package ru.thecntgfy.libooker.model.utils;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import ru.thecntgfy.libooker.dto.RegisterDTO;
import ru.thecntgfy.libooker.model.User;
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

        return new User(
                dto.getUsername(),
                encoder.encode(dto.getPassword()),
                dto.getRole()
        );
    }
}
