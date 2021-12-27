package ru.thecntgfy.libooker.repository;

import org.hibernate.LockMode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.thecntgfy.libooker.model.Booking;
import ru.thecntgfy.libooker.model.User;

import javax.persistence.LockModeType;
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

    Slice<Booking> findAllByUser_Username(String username, Pageable pageable);

    List<Booking> findAllByUser_UsernameAndCanceledFalseAndFinishedManuallyFalse(String username);

    Set<Booking> findAllByUser_UsernameAndDate(String username, LocalDate date);

    Set<Booking> findAllByUserAndDate(User user, LocalDate date);

    Set<Booking> findAllByUserAndDateAndCanceledFalseAndFinishedManuallyFalse(User user, LocalDate date);

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
    Slice<Booking> findAllArchivedByUser(String username, Pageable pageable);

    @Query("""
           from Booking 
           where date = current_date 
           and startTime <= current_time 
           and endTime >= current_time 
           and finishedManually = false 
           and canceled = false 
           order by startTime
           """)
    List<Booking> findAllCurrent();

    @Query("""
           from Booking
           where date = current_date
           and startTime > current_time
           and canceled = false
           """)
    Slice<Booking> findNext(Pageable pageable);

    @Query("""
           from Booking
           where date = current_date
           and endTime <= current_time
           or canceled = true
           or finishedManually = true
           order by startTime
           """)
    Slice<Booking> findTodayClosed(Pageable pageable);

    @Query("""
           from Booking b 
           join b.user u
           where u.username = ?1
           and b.date >= current_date 
           """)
    Slice<Booking> findFutureOrCurrentBookingForUsername(String username, Pageable pageable);

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
