package ru.thecntgfy.libooker.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.thecntgfy.libooker.dto.CreateBookingReq;
import ru.thecntgfy.libooker.model.Booking;
import ru.thecntgfy.libooker.model.User;
import ru.thecntgfy.libooker.service.BookingServiceImpl;
import ru.thecntgfy.libooker.utils.TimeRange;

import javax.validation.Valid;
import javax.validation.constraints.FutureOrPresent;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("bookings")
@Validated
@RequiredArgsConstructor
public class BookingController {
    private final BookingServiceImpl bookingService;

    @GetMapping("available")
    @Validated
    public Iterable<TimeRange> free(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") @FutureOrPresent LocalDate date
    ) {
        return bookingService.getAvailableSchedule(date).collect(Collectors.toList());
    }

    @GetMapping
    public Iterable<Booking> getBookings(Pageable pageable) {
        return bookingService.getBookings(pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public Booking book(@RequestBody @Valid CreateBookingReq dto) {
        return bookingService.book(dto.getTime(), dto.getUserId());
    }

    @GetMapping("user/{userId}")
    public Iterable<Booking> getBookingsForUser(@PathVariable long userId) {
        return bookingService.getBookingsForUser(userId);
    }

    @DeleteMapping("{bookingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeBooking(@PathVariable long bookingId) {
        bookingService.removeBooking(bookingId);
    }
}
