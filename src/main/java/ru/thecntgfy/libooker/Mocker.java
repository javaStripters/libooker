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

import java.util.List;
import java.util.stream.IntStream;

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

        User admin = new User(
                "RGAU",
                "$2a$04$01RbolL5TRdm76ZLa92IsOnhq7VjWBUUeSqJpeOCYKjnS/BX/IPiS",
                Role.ADMIN
        );
        userRepo.save(admin);

        int workplacesOneStart = 41;
        int workplacesOneEndInclusive = 58;

        List<Workplace> workplaces = IntStream.range(workplacesOneStart, workplacesOneEndInclusive)
                .boxed()
                .map(i -> new Workplace(i.toString()))
                .toList();
        workplaceRepo.saveAll(workplaces);

        int workplacesTwoStart = 61;
        int workplacesTwoEndInclusive = 69;

        List<Workplace> workplaces1 = IntStream.range(workplacesTwoStart, workplacesTwoEndInclusive)
                .boxed()
                .map(i -> new Workplace(i.toString()))
                .toList();
        workplaceRepo.saveAll(workplaces1);
    }
}
