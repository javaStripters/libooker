package ru.thecntgfy.libooker.repository;

import org.springframework.data.repository.CrudRepository;
import ru.thecntgfy.libooker.model.User;

public interface UserRepo extends CrudRepository<User, Long> {
}
