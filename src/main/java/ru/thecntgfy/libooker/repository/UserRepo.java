package ru.thecntgfy.libooker.repository;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.thecntgfy.libooker.model.Student;
import ru.thecntgfy.libooker.model.User;

import javax.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@RepositoryRestResource(exported = false)
public interface UserRepo extends CrudRepository<User, String> {
    Optional<User> findByUsername(String username);

    Optional<User> findLockingByUsername(String username);

    @Query(value = """
                   select * from student
                   inner join booking b on b.user_id = student.id
                   where firstname % ?1 or firstname ilike '%' || ?1 || '%'
                   or lastname % ?1 or lastname ilike '%' || ?1 || '%'
                   or patronymic % ?1 or patronymic ilike '%' || ?1 || '%'
                   or testbook % ?1 or testbook ilike '%' || ?1 || '%'
                   order by (firstname <-> ?1) + (lastname <-> ?1) + (patronymic <-> ?1) + (testbook <-> ?1) 
                   """, nativeQuery = true)
    List<Student> searchStudents(String query);
}
