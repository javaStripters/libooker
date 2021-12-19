package ru.thecntgfy.libooker;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.thecntgfy.libooker.repository.UserRepo;

@Component
@RequiredArgsConstructor
public class Checker implements CommandLineRunner {
    private final UserRepo userRepo;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println(userRepo.findAll());
    }
}
