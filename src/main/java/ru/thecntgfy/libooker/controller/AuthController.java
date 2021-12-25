package ru.thecntgfy.libooker.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.thecntgfy.libooker.dto.JwtResponse;
import ru.thecntgfy.libooker.dto.LoginDTO;
import ru.thecntgfy.libooker.dto.RegisterDTO;
import ru.thecntgfy.libooker.model.Role;
import ru.thecntgfy.libooker.model.User;
import ru.thecntgfy.libooker.model.UserMapper;
import ru.thecntgfy.libooker.security.JwtProvider;
import ru.thecntgfy.libooker.security.UserPrincipal;
import ru.thecntgfy.libooker.service.UserServiceImpl;

import javax.transaction.Transactional;
import javax.validation.Valid;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final UserServiceImpl userService;

    @PostMapping("sign-in")
    public JwtResponse authenticateUser(@Valid @RequestBody LoginDTO dto) {
        Authentication authentication = authenticate(dto.getUsername(), dto.getPassword());

        String token = jwtProvider.generateToken(authentication);
        //TODO: Rework, it`s ugly
        Role role = Role.valueOf(((UserPrincipal) authentication.getPrincipal()).getAuthorities().stream().findAny().get().getAuthority().substring(5));

        return new JwtResponse(token, role);
    }

    @PostMapping("register")
    public User registerUser(@Valid @RequestBody RegisterDTO dto) {
        Role role = dto.getRole();
        if (!role.equals(Role.TUTOR) && !role.equals(Role.STUDENT))
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Для создания аккаунта администратора обратитесь к действующему администратору"
            );

        User user = UserMapper.registerDtoToUser(dto);
        return userService.createUser(user);
    }

    protected Authentication authenticate(String username, String password) {
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(username, password));
        SecurityContextHolder.getContext().setAuthentication(authentication);


        return authentication;
    }
}
