package ru.thecntgfy.libooker.service;

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

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl {
    private final LocalTime OPENS = LocalTime.of(10, 0);
    private final LocalTime CLOSES = LocalTime.of(20, 0);
    private final Duration SCHEDULE_STEP = Duration.ofMinutes(15);
    private final Duration MAX_BOOKING_DURATION = Duration.ofHours(2);
    private final int MAX_BOOKINGS_FOR_USER = 5;
    private final TimeRange workTime = new TimeRange(OPENS, CLOSES);

    private final BookingRepo bookingRepo;
    private final WorkplaceRepo workplaceRepo;
    private final UserRepo userRepo;

    public Page<Booking> getBookings(Pageable pageable) {
        return bookingRepo.findAll(pageable);
    }

    //TODO: Rework
    public Stream<TimeRange> getAvailableSchedule(LocalDate date, String username) {
        User user = userRepo.findByUsername(username);
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
    public Booking book(LocalDateTime from, LocalDateTime to, String username) {
        if (Duration.between(to, from).compareTo(MAX_BOOKING_DURATION) > 0)
            //TODO: Custom Exceptions
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Максимальная длительность брони: " + MAX_BOOKING_DURATION.toMinutes() + "мин.");

        LocalDate date = from.toLocalDate();
        LocalTime startTime = from.toLocalTime();
        LocalTime endTime = to.toLocalTime();

        TimeRange untilCloses = new TimeRange(startTime, CLOSES);
        TimeRange requested = new TimeRange(startTime, endTime);

        TimeRange bookedTime = TimeRange.min(untilCloses, requested);

        User user = userRepo.findByUsername(username);
        Set<Booking> bookings = bookingRepo.findAllByUser_UsernameAndDate(username, date);
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
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Нет доступной брони на" + startTime + " - " + endTime);

        return booking;
    }

    //TODO: User does not exist
    public Set<Booking> getBookingsForUser(String username) {
        return bookingRepo.findAllByUser_Username(username);
    }

    public void removeBooking(long bookingId) {
        bookingRepo.deleteById(bookingId);
    }

    public void removeBooking(long bookingId, String username) {
        Booking booking = bookingRepo.findById(bookingId).get();
        if (!booking.getUser().getUsername().equals(username))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Только администратор может отменять брони других пользователей!");

        bookingRepo.delete(booking);
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
        return MAX_BOOKING_DURATION;
    }
}
