package ru.thecntgfy.libooker.repository;

import org.springframework.data.repository.CrudRepository;
import ru.thecntgfy.libooker.model.User;

import java.util.Optional;

public interface UserRepo extends CrudRepository<User, Long> {
    //TODO: Oprional<>
    User findByUsername(String username);
}
