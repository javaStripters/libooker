package ru.thecntgfy.libooker.controller;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.thecntgfy.libooker.model.DayOff;
import ru.thecntgfy.libooker.repository.DayOffRepo;
import ru.thecntgfy.libooker.service.SimpleProductionCalendarServiceImpl;
import ru.thecntgfy.libooker.service.value.Day;

import java.time.LocalDate;

@RestController
@RequestMapping("admin")
@Validated
@RequiredArgsConstructor
public class AdminController {
    private final DayOffRepo dayOffRepo;
    private final SimpleProductionCalendarServiceImpl productionCalendar;

    @PostMapping("day-off")
    @PreAuthorize("hasRole('ADMIN')")
    public DayOff addDayOff(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") @Parameter(example = "2021-12-31")LocalDate date
    ) {
        return dayOffRepo.save(new DayOff(date));
    }

    @DeleteMapping("day-off")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void removeDayOff(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") @Parameter(example = "2021-12-31")LocalDate date
    ) {
        DayOff dayOff = dayOffRepo.findByDate(date)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Выходной не существует!"));

        dayOffRepo.delete(dayOff);
    }

    //TODO: Validation
    @GetMapping("day-off")
    @PreAuthorize("hasRole('ADMIN')")
    public Iterable<Day> calendar(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate from,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate to
    ) {
        return productionCalendar.forRange(from, to);
    }
}
