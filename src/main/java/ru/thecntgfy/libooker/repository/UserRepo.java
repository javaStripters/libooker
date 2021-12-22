package ru.thecntgfy.libooker.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.thecntgfy.libooker.model.User;

import java.util.Optional;

@RepositoryRestResource(exported = false)
public interface UserRepo extends CrudRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
