package ru.thecntgfy.libooker;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.thecntgfy.libooker.model.Student;
import ru.thecntgfy.libooker.repository.UserRepo;

import java.io.*;
import java.net.URL;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {
    private final UserRepo userRepo;
    private final PasswordEncoder encoder;
    @Value("${app.downloadUsersUrl}")
    private URL downloadUrl;
    private final String CHARSET = "windows-1251";

    //TODO: Proper buffering by saving separate entities / batches
    @Override
    public void run(String... args) throws Exception {
        //TODO: Remove in production
        if (userRepo.findByUsername("38763").isPresent())
            return;

        BufferedReader reader = new BufferedReader(new InputStreamReader(downloadUrl.openStream(), CHARSET));

        //LOGIN;PASS;F;I;O;TESTBOOK_NUM
        List<Student> users = reader.lines()
                .skip(1)
                .map(s -> s.split(";"))
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
