package ru.thecntgfy.libooker;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.thecntgfy.libooker.model.Student;
import ru.thecntgfy.libooker.repository.UserRepo;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.*;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {
    private final UserRepo userRepo;
    private final PasswordEncoder encoder;
    @Value("${app.downloadUsersUrl}")
    private URL downloadUrl;
    private final String CHARSET = "windows-1251";

    //TODO: Proper buffering by saving separate entities / batches
    //TODO: Logging
    @Override
    public void run(String... args) throws Exception {
        //TODO: Remove in production
        if (userRepo.findByUsername("38763").isPresent())
            return;

        BufferedReader reader = new BufferedReader(new InputStreamReader(downloadUrl.openStream(), CHARSET));

        // логин,пароль,имя,фамилия,отчество,зачетная книжка
        List<Student> users = reader.lines()
                .map(s -> s.split(","))
                .filter(arr -> arr.length == 6)
                .parallel()
                .map(split -> new Student(
                        split[0],
                        encoder.encode(split[1]),
                        split[2],
                        split[3],
                        split[4],
                        split[5])
                )
                .toList();

        userRepo.saveAll(users);
    }
}
