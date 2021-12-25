package ru.thecntgfy.libooker;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.thecntgfy.libooker.model.Role;
import ru.thecntgfy.libooker.model.User;
import ru.thecntgfy.libooker.model.Workplace;
import ru.thecntgfy.libooker.repository.BookingRepo;
import ru.thecntgfy.libooker.repository.UserRepo;
import ru.thecntgfy.libooker.repository.WorkplaceRepo;

@Component
@RequiredArgsConstructor
@Order(Integer.MAX_VALUE - 1)
public class Mocker implements CommandLineRunner {
    private final BookingRepo bookingRepo;
    private final UserRepo userRepo;
    private final WorkplaceRepo workplaceRepo;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (userRepo.findByUsername("Vasya").isPresent())
            return;

        User user = new User(
                "Vasya",
                "$2a$10$kWWOnNOiToOxcIQ7UJ.cB.XFAflYvMS5BPASR1eqqojc6H9ELWUfC",
                Role.ADMIN
        );
        userRepo.save(user);

        Workplace workplace = new Workplace("KA-123");
        workplaceRepo.save(workplace);

        Workplace workplace1 = new Workplace("BZ-678");
        workplaceRepo.save(workplace1);

//        Booking booking = new Booking(workplace, user, LocalDate.now(), new TimeRange(LocalTime.of(12, 0), LocalTime.of(14, 0)));
//        bookingRepo.save(booking);
//
//        Booking booking1 = new Booking(workplace, user, LocalDate.now(), new TimeRange(LocalTime.of(16, 0), LocalTime.of(18, 0)));
//        bookingRepo.save(booking1);
//
//        Booking booking2 = new Booking(workplace1, user, LocalDate.now(), new TimeRange(LocalTime.of(15, 0), LocalTime.of(17, 0)));
//        bookingRepo.save(booking2);
    }
}
