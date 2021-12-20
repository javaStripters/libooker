package ru.thecntgfy.libooker.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.thecntgfy.libooker.dto.RegisterDTO;
import ru.thecntgfy.libooker.model.User;
import ru.thecntgfy.libooker.model.UserMapper;
import ru.thecntgfy.libooker.repository.UserRepo;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl {
    private final UserRepo userRepo;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    //TODO: Add checks
    public User createUser(User user) {
        return userRepo.save(user);
    }
}
