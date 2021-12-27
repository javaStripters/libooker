package ru.thecntgfy.libooker.service;

import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.thecntgfy.libooker.dto.ScheduleStep;
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
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingServiceImpl {
    private final LocalTime OPENS = LocalTime.of(9, 0);
    private final LocalTime CLOSES = LocalTime.of(19, 0);
    private final Duration SCHEDULE_STEP = Duration.ofMinutes(30);
    private final Duration MAX_BOOKING_DURATION = Duration.ofHours(2);
    private final int MAX_BOOKINGS_FOR_USER = 5;
    private final TimeRange workTime = new TimeRange(OPENS, CLOSES);

    private final BookingRepo bookingRepo;
    private final WorkplaceRepo workplaceRepo;
    private final UserRepo userRepo;
    private final SimpleProductionCalendarServiceImpl productionCalendar;

    public Page<Booking> getBookings(Pageable pageable, Optional<LocalDate> dateOptional) {
        return dateOptional.isPresent()
                ? bookingRepo.findAllByDate(pageable, dateOptional.get())
                : bookingRepo.findAll(pageable);
    }

    public List<Booking> getCurrentBookings() {
        return bookingRepo.findAllCurrent();
    }

    public Slice<Booking> getNextBookings() {
        return bookingRepo.findNext(Pageable.unpaged());
    }

    public Slice<Booking> getTodayClosed() {
        return bookingRepo.findTodayClosed(Pageable.unpaged());
    }


    //TODO: Rework
    public Stream<ScheduleStep> getAvailableSchedule(LocalDate date, String username) {
        Map<Workplace, TreeSet<TimeRange>> timeByWorkplace = availableTimeByWorkplace(date);

        TreeSet<TimeRange> availableTime = new TreeSet<>();
        for (TreeSet<TimeRange> byWorkplace : timeByWorkplace.values()) {
            availableTime.addAll(byWorkplace);
        }

        List<TimeRange> schedule = TimeRange.ranges(OPENS, CLOSES, SCHEDULE_STEP);

        boolean isDayOff = productionCalendar.isDayOff(date);
        if (isDayOff)
            return schedule.stream().map(ScheduleStep::closed);

        List<Booking> userBookings = bookingRepo.findAllActiveByUsername(username);
        return schedule.stream()
                .map(timeRange -> {
                    boolean doesInterfereWithBooked = userBookings.stream()
                            .filter(Predicate.not(Booking::isCanceled))
                            .filter(booking -> booking.getDate().equals(date))
                            .anyMatch(booked -> booked.getTimeRange().doesInterfereExclusive(timeRange) || booked.getTimeRange().equals(timeRange));
                    if (doesInterfereWithBooked)
                        return ScheduleStep.self(timeRange);

                    boolean isAvailable = availableTime.headSet(new TimeRange(timeRange.from(), CLOSES), true).stream()
                            .anyMatch(range -> range.toInclusive().compareTo(timeRange.toInclusive()) >= 0);
                    if (!isAvailable)
                        return ScheduleStep.occupied(timeRange);

                    return ScheduleStep.free(timeRange);
                });
    }

    public synchronized Booking book(LocalDateTime from, LocalDateTime to, String username) {
        if (Duration.between(to, from).compareTo(MAX_BOOKING_DURATION) > 0)
            //TODO: Custom Exceptions
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Максимальная длительность брони: " + MAX_BOOKING_DURATION.toMinutes() + " мин.");

        LocalDate date = from.toLocalDate();
        LocalTime startTime = from.toLocalTime();
        LocalTime endTime = to.toLocalTime();

        if (productionCalendar.isDayOff(date))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Нельзя бронировать в выходной!");

        TimeRange untilCloses = new TimeRange(startTime, CLOSES);
        TimeRange requested = new TimeRange(startTime, endTime);

        TimeRange bookedTime = TimeRange.min(untilCloses, requested);

        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        Set<Booking> dateBookings = bookingRepo.findAllActiveByUserAndDate(user, date);
        List<Booking> allActive = bookingRepo.findAllActiveByUsername(username);
        if (allActive.size() >= MAX_BOOKINGS_FOR_USER)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Достигнут лимит бронирований!");
        if (dateBookings.stream().anyMatch(booking -> booking.getTimeRange().doesInterfereExclusive(bookedTime) || booking.getTimeRange().equals(bookedTime)))
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

    //TODO: Move authority check to controller?
    public Booking finishBooking(long bookingId, String username, GrantedAuthority authority) {
        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found!"));

        if (!username.equals(booking.getUser().getUsername()) && !authority.getAuthority().equals("ROLE_ADMIN"))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Только администратор может заканчивать брони других пользователей!");

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        if (booking.getDate().isAfter(today) || (booking.getDate().equals(today) && now.isBefore(booking.getStartTime())))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Сеанс еще не начался!");
        if (booking.getDate().isBefore(today) || (booking.getDate().equals(today) && now.isAfter(booking.getEndTime())))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Время брони уже прошло!");
        if (booking.isCanceled())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Бронь была отменена!");
        if (booking.isFinishedManually())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Попытка закончить бронь дважды!");

        return booking.finish();
    }

    public Slice<Booking> getCurrentAndFutureForUser(String username, Pageable pageable) {
        return bookingRepo.findFutureOrCurrentBookingForUsername(username, pageable);
    }

    public Slice<Booking> getArchivedBookingsForUser(String username, Pageable pageable) {
        return bookingRepo.findAllArchivedByUser(username, pageable);
    }

    public List<Booking> getActiveBookingsForUser(String username) {
        User user = userRepo.findByUsername(username).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User %s not found".formatted(username))
        );

        return user.getBookings()
                .stream()
                .filter(b -> !b.isCanceled())
                .filter(b -> !b.isFinishedManually())
                .collect(Collectors.toList());
    }

    public void removeBooking(long bookingId) {
        Booking booking = bookingRepo.findActiveById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Бронь не существует, закончилась или уже отменена!"));

        booking.cancel();
    }

    public void removeBooking(long bookingId, String username) {
        Booking booking = bookingRepo.findActiveById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Бронь не существует, закончилась или уже отменена!"));

        if (!booking.getUser().getUsername().equals(username))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Только администратор может отменять брони других пользователей!");

        booking.cancel();
    }

    //TODO: Store booked in memory
    protected Map<Workplace, TreeSet<TimeRange>> availableTimeByWorkplace(LocalDate date) {
        List<Booking> booked = bookingRepo.findAllActiveByDate(date);

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
}
