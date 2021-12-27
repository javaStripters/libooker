package ru.thecntgfy.libooker.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.thecntgfy.libooker.model.User;
import ru.thecntgfy.libooker.repository.UserRepo;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("users")
@Validated
@RequiredArgsConstructor
public class UserController {
    private final UserRepo userRepo;

    @GetMapping
    @Operation(security = { @SecurityRequirement(name = "bearer-key") })
    @Validated
    public User getCurrentUser(Principal principal) {
        return userRepo.findByUsername(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("{username}")
    @Operation(security = { @SecurityRequirement(name = "bearer-key") })
    public User getUser(@PathVariable String username) {
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping("statistic")
    @Operation(security = { @SecurityRequirement(name = "bearer-key") })
    public Map<String, Integer> getStatisticsByUser(Principal principal) {
        return getStatistics(principal.getName());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("{username}/statistic")
    @Operation(security = { @SecurityRequirement(name = "bearer-key") })
    public Map<String, Integer> getStatisitcsByAdmin(@PathVariable String username) {
        return getStatistics(username);
    }

    protected Map<String, Integer> getStatistics(String username) {
        return Map.of(
                "hours", userRepo.countUserHours(username),
                "activeBookings", userRepo.countUserFutureBookings(username),
                "closedSessions", userRepo.countClosedBookings(username)
        );
    }
}
