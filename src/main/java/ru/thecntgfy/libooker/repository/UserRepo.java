package ru.thecntgfy.libooker.repository;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.thecntgfy.libooker.model.Student;
import ru.thecntgfy.libooker.model.User;

import javax.persistence.LockModeType;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

@RepositoryRestResource(exported = false)
public interface UserRepo extends CrudRepository<User, String> {
    Optional<User> findByUsername(String username);

    Optional<User> findLockingByUsername(String username);

    @Query(value = """
                   select * from student
                   where firstname % ?1 or firstname ilike '%' || ?1 || '%'
                   or lastname % ?1 or lastname ilike '%' || ?1 || '%'
                   or patronymic % ?1 or patronymic ilike '%' || ?1 || '%'
                   or testbook % ?1 or testbook ilike '%' || ?1 || '%'
                   order by (firstname <-> ?1) + (lastname <-> ?1) + (patronymic <-> ?1) + (testbook <-> ?1) 
                   """, nativeQuery = true)
    List<Student> searchStudents(String query);

    @Query(value =
            """
            select extract(hour from sum(b.end_time - b.start_time)) as range from student
            inner join booking b on student.id = b.user_id
            where username = ?1
            and b.canceled = false
            and (date <= cast(now() as date) or (date <= cast(now() as date) and end_time < cast(now() as time)))
            """, nativeQuery = true)
    Integer countUserHours(String username);

    @Query(value =
        """
        select count(*) from student
        inner join booking b on student.id = b.user_id
        where username = ?1
        and (b.date > cast(now() as date)
                or (b.date = cast(now() as date) and b.end_time > cast(now() as time)))
        and not b.canceled
        and not b.finished_manually;
        """, nativeQuery = true)
    Integer countUserFutureBookings(String username);

    //TODO: Add finishedManually
    @Query(value =
            """
            select count(*) as range from student
            inner join booking b on student.id = b.user_id
            where username = ?1
            and b.canceled = false
            and (date <= cast(now() as date) or (date <= cast(now() as date) and end_time < cast(now() as time)))
            """, nativeQuery = true)
    Integer countClosedBookings(String username);
}
