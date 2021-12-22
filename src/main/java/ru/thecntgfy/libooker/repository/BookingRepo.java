package ru.thecntgfy.libooker.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.thecntgfy.libooker.model.Booking;
import ru.thecntgfy.libooker.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

//TODO: Rewrite?
@RepositoryRestResource(exported = false)
public interface BookingRepo extends CrudRepository<Booking, Long> {
    Page<Booking> findAll(Pageable pageable);

    Optional<Booking> findByIdAndCanceledFalse(long id);

    List<Booking> findAllByDate(LocalDate day);

    List<Booking> findAllByDateAndCanceledFalse(LocalDate date);

    Set<Booking> findAllByUserId(long userId);

    Set<Booking> findAllByUser_Username(String username);

    List<Booking> findAllByUser_UsernameAndCanceledFalse(String username);

    Set<Booking> findAllByUser_UsernameAndDate(String username, LocalDate date);

    Set<Booking> findAllByUserAndDate(User user, LocalDate date);

    Set<Booking> findAllByUserAndDateAndCanceled(User user, LocalDate date, boolean canceled);

    void removeByUser_UsernameAndId(String username, long id);

    default List<Booking> findAllActiveByDate(LocalDate date) {
        return findAllByDateAndCanceledFalse(date);
    }

    default Set<Booking> findAllActiveByUserAndDate(User user, LocalDate date) {
        return findAllByUserAndDateAndCanceled(user, date, false);
    }

    default Optional<Booking> findActiveById(long id) {
        return findByIdAndCanceledFalse(id);
    }

    default List<Booking> findAllActiveByUsername(String username) {
        return findAllByUser_UsernameAndCanceledFalse(username);
    }
}
