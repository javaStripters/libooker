package ru.thecntgfy.libooker.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.thecntgfy.libooker.model.Booking;
import ru.thecntgfy.libooker.model.User;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

//TODO: Rewrite long generated methods with @Query or write impl with querydsl
@RepositoryRestResource(exported = false)
public interface BookingRepo extends CrudRepository<Booking, Long> {
    Page<Booking> findAll(Pageable pageable);

    Page<Booking> findAllByDate(Pageable pageable, LocalDate date);

    Optional<Booking> findByIdAndCanceledFalseAndFinishedManuallyFalse(long id);

    List<Booking> findAllByDate(LocalDate day);

    List<Booking> findAllByDateAndCanceledFalseAndFinishedManuallyFalse(LocalDate date);

    Set<Booking> findAllByUserId(long userId);

    Set<Booking> findAllByUser_Username(String username);

    List<Booking> findAllByUser_UsernameAndCanceledFalseAndFinishedManuallyFalse(String username);

    Set<Booking> findAllByUser_UsernameAndDate(String username, LocalDate date);

    Set<Booking> findAllByUserAndDate(User user, LocalDate date);

    Set<Booking> findAllByUserAndDateAndCanceledFalseAndFinishedManuallyFalse(User user, LocalDate date);

//    from Booking b join User u
//    where u.username = ?1
//    or (b.date < current_date or (b.date = current_date  and b.endTime < current_time))
//    or b.finishedManually = true
//    or b.canceled = true
    //TODO: Find out why plain join doesn`t work
    @Query("""
           from Booking b inner join b.user u
           where u.username = ?1
           and (
            b.date < current_date or (b.date = current_date  and b.endTime < current_time)
            or b.canceled = true
            or b.finishedManually = true
           )
           """)
    List<Booking> findAllArchivedByUser(String username);

    void removeByUser_UsernameAndId(String username, long id);

    default List<Booking> findAllActiveByDate(LocalDate date) {
        return findAllByDateAndCanceledFalseAndFinishedManuallyFalse(date);
    }

    default Set<Booking> findAllActiveByUserAndDate(User user, LocalDate date) {
        return findAllByUserAndDateAndCanceledFalseAndFinishedManuallyFalse(user, date);
    }

    default Optional<Booking> findActiveById(long id) {
        return findByIdAndCanceledFalseAndFinishedManuallyFalse(id);
    }

    default List<Booking> findAllActiveByUsername(String username) {
        return findAllByUser_UsernameAndCanceledFalseAndFinishedManuallyFalse(username);
    }
}
