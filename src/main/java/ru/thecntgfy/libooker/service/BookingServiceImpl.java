package ru.thecntgfy.libooker.service;

import com.sun.source.tree.Tree;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.thecntgfy.libooker.model.Booking;
import ru.thecntgfy.libooker.model.User;
import ru.thecntgfy.libooker.model.Workplace;
import ru.thecntgfy.libooker.repository.BookingRepo;
import ru.thecntgfy.libooker.repository.UserRepo;
import ru.thecntgfy.libooker.repository.WorkplaceRepo;
import ru.thecntgfy.libooker.utils.Pair;
import ru.thecntgfy.libooker.utils.TimeRange;

import java.awt.print.Book;
import java.sql.Time;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl {
    private final LocalTime OPENS = LocalTime.of(10, 0);
    private final LocalTime CLOSES = LocalTime.of(20, 0);
    private final Duration SCHEDULE_STEP = Duration.ofMinutes(15);
    private final int MAX_BOOKINGS_FOR_USER = 5;
    private final TimeRange workTime = new TimeRange(OPENS, CLOSES);
    private final Duration bookingDuration = Duration.ofHours(2);

    private final BookingRepo bookingRepo;
    private final WorkplaceRepo workplaceRepo;
    private final UserRepo userRepo;

    public Page<Booking> getBookings(Pageable pageable) {
        return bookingRepo.findAll(pageable);
    }

    //TODO: Rework
    public Stream<TimeRange> getAvailableSchedule(LocalDate date) {
        User user = userRepo.findById(1L).get();
        Map<Workplace, TreeSet<TimeRange>> timeByWorkplace = availableTimeByWorkplace(date);

        TreeSet<TimeRange> availableTime = new TreeSet<>();
        for (TreeSet<TimeRange> byWorkplace : timeByWorkplace.values()) {
            availableTime.addAll(byWorkplace);
        }


        List<TimeRange> schedule = TimeRange.ranges(OPENS, CLOSES, SCHEDULE_STEP);
        return schedule.stream()
                .filter(timeRange ->
                        availableTime.headSet(new TimeRange(timeRange.from(), CLOSES), true).stream()
                                .anyMatch(range -> range.toInclusive().compareTo(timeRange.toInclusive()) >= 0)
                )
                .filter(timeRange ->
                        user.getBookings().stream().noneMatch(booked -> booked.getTimeRange().doesInterfereExclusive(timeRange))
                );
    }

    //TODO: Max user bookings
    //TODO: User not found
    //TODO: Only next week
    public Booking book(LocalDateTime dateTime, long userId) {
        LocalDate date = dateTime.toLocalDate();
        LocalTime time = dateTime.toLocalTime();

        TimeRange untilCloses = new TimeRange(time, CLOSES);
        TimeRange plusDuration = new TimeRange(time, time.plus(duration()));

        TimeRange bookedTime = TimeRange.min(untilCloses, plusDuration);

        User user = userRepo.findById(userId).get();
        Set<Booking> bookings = user.getBookings();
        if (bookings.size() >= MAX_BOOKINGS_FOR_USER)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Достигнут лимит бронирований!");
        if (bookings.stream().anyMatch(booking -> booking.getTimeRange().doesInterfere(bookedTime)))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Новая бронь не должна пересекаться у уже существующими!");

        Map<Workplace, TreeSet<TimeRange>> availableTimeByWorkplace = availableTimeByWorkplace(date);
        Booking booking = null;
        for (var ranges : availableTimeByWorkplace.entrySet()) {
            for (TimeRange range : ranges.getValue()) {
                if (range.includes(bookedTime)) {
                    booking = new Booking(ranges.getKey(), user, date, bookedTime);
                    break;
                }
            }
        }

        if (booking != null)
            bookingRepo.save(booking);
        else
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No time available for " + dateTime);

        return booking;
    }

    //TODO: User does not exist
    public Set<Booking> getBookingsForUser(long userId) {
        return bookingRepo.findAllByUserId(userId);
    }

    public void removeBooking(long bookingId) {
        bookingRepo.deleteById(bookingId);
    }

    protected Map<Workplace, TreeSet<TimeRange>> availableTimeByWorkplace(LocalDate date) {
        List<Booking> booked = bookingRepo.findAllByDate(date);

        Map<Workplace, TreeSet<TimeRange>> availableTimeByWorkplace = new HashMap<>();
        for (Workplace workplace : workplaceRepo.findAll()) {
            availableTimeByWorkplace.put(workplace, new TreeSet<>(List.of(workTime)));
        }

        for (Booking booking : booked) {
            TreeSet<TimeRange> timeForWorkplace = availableTimeByWorkplace.get(booking.getWorkplace());

            TimeRange bookedTime = new TimeRange(
                    booking.getStartTime(),
                    booking.getEndTime()
            );
            //TODO: User ordered set method
            TimeRange included = timeForWorkplace.stream().filter(r -> r.includes(bookedTime)).findAny().get();
            Pair<TimeRange> newRanges = included.differenceInside(bookedTime);

            timeForWorkplace.remove(included);
            timeForWorkplace.addAll(newRanges);
        }

        return availableTimeByWorkplace;
    }

    protected Duration duration() {
        return bookingDuration;
    }
}