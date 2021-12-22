package ru.thecntgfy.libooker.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.thecntgfy.libooker.dto.ScheduleStep;
import ru.thecntgfy.libooker.model.Booking;
import ru.thecntgfy.libooker.security.UserPrincipal;
import ru.thecntgfy.libooker.service.BookingServiceImpl;
import ru.thecntgfy.libooker.utils.TimeRange;

import javax.validation.constraints.FutureOrPresent;
import java.security.Principal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestController
@RequestMapping("bookings")
@Validated
@RequiredArgsConstructor
public class BookingController {
    private final BookingServiceImpl bookingService;

    private final Duration MAX_BOOKING_DISTANCE = Duration.ofDays(7);

    @GetMapping("available")
    @Validated
    public Iterable<ScheduleStep> available(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") @FutureOrPresent LocalDate date,
            Principal principal
    ) {
        return bookingService.getAvailableSchedule(date, principal.getName()).collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public Iterable<Booking> getBookings(Pageable pageable) {
        return bookingService.getBookings(pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Booking book(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            Principal principal
    ) {
        if (!from.toLocalDate().equals(to.toLocalDate()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Бронь должна начинаться и заканчиваться в один день!");
        if (LocalDateTime.now().plus(MAX_BOOKING_DISTANCE).toLocalDate().isBefore(from.toLocalDate()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Нельзя бронировать раньше чем за " + MAX_BOOKING_DISTANCE.toDays() + " дней");

        return bookingService.book(from, to, principal.getName());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("user/{username}")
    public Iterable<Booking> getBookingsForUserByAdmin(@PathVariable String username) {
            return bookingService.getBookingsForUser(username);
    }

    @GetMapping("user")
    public Iterable<Booking> getBookingsForUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return bookingService.getBookingsForUser(userPrincipal.getUsername());
    }

    //TODO: Set flag instead of deleting
    @DeleteMapping("{bookingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeBookingByAdmin(
            @PathVariable long bookingId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")))
            bookingService.removeBooking(bookingId);
        else
            bookingService.removeBooking(bookingId, principal.getUsername());
    }
}
