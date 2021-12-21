package ru.thecntgfy.libooker.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import ru.thecntgfy.libooker.model.Booking;
import ru.thecntgfy.libooker.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface BookingRepo extends CrudRepository<Booking, Long> {
    Page<Booking> findAll(Pageable pageable);

    List<Booking> findAllByDate(LocalDate day);

    Set<Booking> findAllByUserId(long userId);

    Set<Booking> findAllByUser_Username(String username);

    Set<Booking> findAllByUser_UsernameAndDate(String username, LocalDate date);

    Set<Booking> findAllByUserAndDate(User user, LocalDate date);

    void removeByUser_UsernameAndId(String username, long id);
}
