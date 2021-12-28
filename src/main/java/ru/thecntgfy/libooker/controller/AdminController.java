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
import ru.thecntgfy.libooker.model.User;
import ru.thecntgfy.libooker.model.projection.DayStats;
import ru.thecntgfy.libooker.model.projection.OverallStats;
import ru.thecntgfy.libooker.repository.BookingRepo;
import ru.thecntgfy.libooker.repository.DayOffRepo;
import ru.thecntgfy.libooker.repository.UserRepo;
import ru.thecntgfy.libooker.repository.WorkplaceRepo;
import ru.thecntgfy.libooker.service.SimpleProductionCalendarServiceImpl;
import ru.thecntgfy.libooker.service.value.Day;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("admin")
@Validated
@RequiredArgsConstructor
public class AdminController {
    private final DayOffRepo dayOffRepo;
    private final SimpleProductionCalendarServiceImpl productionCalendar;
    private final UserRepo userRepo;
    //TODO: Remove
    private final WorkplaceRepo workplaceRepo;
    private final BookingRepo bookingRepo;

    //TODO: Remove bookings
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

    @PostMapping("user-search")
    @PreAuthorize("hasRole('ADMIN')")
    public Iterable<? extends User> userSearch(@RequestParam String query) {
        return userRepo.searchStudents(query);
    }

    @GetMapping("stats/overall")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Integer> getOverAllStats(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate from,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate to
    ) {
        LocalDate today = LocalDate.now();
        OverallStats overallStats = workplaceRepo.calcOverallStats(from, to);
        Integer cancelled = workplaceRepo.countCancelled(from, to);

        return Map.of(
                "sumHours", overallStats.getSumHours(),
                "visitors", overallStats.getVisitors(),
                "avgSessionMin", overallStats.getAvgSessionMin(),
                "cancelled", cancelled
        );
    }

    @GetMapping("stats/visits")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<Integer, Integer> getVisitStats() {
        return todayVisitsAndWorkplaceUsage();
    }

    protected Map<Integer, Integer> todayVisitsAndWorkplaceUsage() {
        return workplaceRepo.calcDayStats(LocalDate.of(2021, 12, 27)).stream()
                .flatMap(Arrays::stream)
                .collect(Collectors.toMap(DayStats::getHour, DayStats::getCount));
    }
}
