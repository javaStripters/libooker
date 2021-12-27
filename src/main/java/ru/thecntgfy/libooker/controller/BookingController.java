package ru.thecntgfy.libooker.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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

import java.security.Principal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("bookings")
@Validated
@RequiredArgsConstructor
public class BookingController {
    private final BookingServiceImpl bookingService;

    private final Duration MAX_BOOKING_DISTANCE = Duration.ofDays(7);

    @GetMapping("available")
    @Operation(security = { @SecurityRequirement(name = "bearer-key") })
    @Validated
    public Iterable<ScheduleStep> available(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") @Parameter(example = "2021-12-23") LocalDate date,
            Principal principal
    ) {
        return bookingService.getAvailableSchedule(date, principal.getName()).collect(Collectors.toList());
    }

    //TODO: Remove pageable, add limit
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    @Operation(security = { @SecurityRequirement(name = "bearer-key") })
    public Iterable<Booking> getBookings(
            @RequestParam(defaultValue = "false") boolean today,
            @Parameter(hidden = true) Pageable pageable
    ) {
        Optional<LocalDate> date = today ? Optional.of(LocalDate.of(2021, 12, 27)) : Optional.empty();
        return bookingService.getBookings(pageable, date);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("today/current")
    @Operation(security = { @SecurityRequirement(name = "bearer-key") })
    public Iterable<Booking> getCurrentBookings() {
        return bookingService.getCurrentBookings();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("today/next")
    @Operation(security = { @SecurityRequirement(name = "bearer-key") })
    public Iterable<Booking> getNextBookings() {
        return bookingService.getNextBookings();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("today/closed")
    @Operation(security = { @SecurityRequirement(name = "bearer-key") })
    public Iterable<Booking> getClosedBookings() {
        return bookingService.getTodayClosed();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Бронь.",
            description = "Создает бронь для авторизованного пользователя с *from* до *to*",
            security = { @SecurityRequirement(name = "bearer-key") }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", description = """
                    - Бронь начинается и заканчивается в разные дни
                    - Бронь раньше чем за неделю
                    - Превышено ограничение по длительности одной брони
                    """),
            @ApiResponse(responseCode = "409", description = """
                    - Достигнут лимит бронирования
                    - Новая бронь пересекается с существующими
                    - Нет доступного времени
                    """)
    })
    public Booking book(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @Schema(example = "2021-12-23T12:00:00.00Z") LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @Schema(example = "2021-12-23T13:30:00.00Z") LocalDateTime to,
            Principal principal
    ) {
        if (!from.toLocalDate().equals(to.toLocalDate()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Бронь должна начинаться и заканчиваться в один день!");
        if (LocalDateTime.now().plus(MAX_BOOKING_DISTANCE).toLocalDate().isBefore(from.toLocalDate()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Нельзя бронировать раньше чем за " + MAX_BOOKING_DISTANCE.toDays() + " дней");


        return bookingService.book(from, to, principal.getName());
    }

    //TODO: Add normal paging. Maybe impl cursor
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("user/{username}")
    @Operation(
            summary = "Все брони указанного пользователя",
            description = """
                            Все брони (активные, отмененные завершенные) брони **указанного** пользователя пользователя. 
                            Доступно только администратору.
                            """,
            security = { @SecurityRequirement(name = "bearer-key") }
    )
    public Iterable<Booking> getBookingsForUserByAdmin(
            @PathVariable String username,
            @RequestParam(defaultValue = "false") Boolean archive
    ) {
            return archive
                   ? bookingService.getArchivedBookingsForUser(username, Pageable.unpaged())
                   : bookingService.getCurrentAndFutureForUser(username, Pageable.unpaged());
    }

    @GetMapping("user")
    @Operation(security = { @SecurityRequirement(name = "bearer-key") })
    public Iterable<Booking> getBookingsForUser(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            Pageable pageable
    ) {
        return bookingService.getCurrentAndFutureForUser(userPrincipal.getUsername(), pageable);
    }

    @GetMapping("user/archive")
    @Operation(security = { @SecurityRequirement(name = "bearer-key") })
    public Iterable<Booking> getArchivedBookingForUser(Principal principal, Pageable pageable) {
        return bookingService.getArchivedBookingsForUser(principal.getName(), pageable);
    }


    //TODO: Prod: Return only present or future
    @GetMapping("user/active")
    @Operation(
            summary = "Активные брони авторизованного пользователя",
            description = "Активные (не отмененные и не завершенные вручную) брони **авторизированного** пользователя",
            security = { @SecurityRequirement(name = "bearer-key") }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content)
    })
    public Iterable<Booking> getActiveBookingsForUser(Principal principal) {
        return bookingService.getActiveBookingsForUser(principal.getName());
    }

    @DeleteMapping("{bookingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Отмена брони",
            description = "Помечает бронь c указанным *bookingId* как отмененную",
            security = { @SecurityRequirement(name = "bearer-key") }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Успешно"),
            @ApiResponse(responseCode = "404", description = "Бронь не найдена"),
            @ApiResponse(responseCode = "403", description = "Удаление брони другого пользователя не от имени администратора")
    })
    public void removeBooking(
            @PathVariable long bookingId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")))
            bookingService.removeBooking(bookingId);
        else
            bookingService.removeBooking(bookingId, principal.getUsername());
    }

    @PutMapping("finish/{bookingId}")
    @Operation(
            summary = "Завершение текущей брони",
            description = "Помечает бронь c указанным *bookingId* как завершенную вручную.",
            security = { @SecurityRequirement(name = "bearer-key") }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешное завершение. Возвращает бронь с измененным полем finishedManually"),
            @ApiResponse(
                    responseCode = "409",
                    description = """
                                  Конфликт, если:
                                  - Сеанс еще не начался
                                  - Время брони прошло
                                  - Бронь была отменена
                                  - Бронь уже была закончена вручную
                                  """,
                    content = @Content
            )
    })
    public Booking finishBookingManually(
            @PathVariable long bookingId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
            return bookingService.finishBooking(
                    bookingId,
                    principal.getUsername(),
                    principal.getAuthorities().stream().findAny().orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN))
            );
    }
}
