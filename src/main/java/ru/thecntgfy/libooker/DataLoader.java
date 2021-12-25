package ru.thecntgfy.libooker;

import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.Charsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.thecntgfy.libooker.model.User;
import ru.thecntgfy.libooker.repository.UserRepo;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {
    private final UserRepo userRepo;
    private final PasswordEncoder encoder;
    @Value("${app.downloadUsersUrl}")
    private String downloadUrl;
    private final String CHARSET = "windows-1251";

    //TODO: Proper buffering by saving separate entities / batches
    @Override
    public void run(String... args) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(downloadUrl).openStream(), CHARSET));

        //LOGIN;PASS;F;I;O;TESTBOOK_NUM
        List<User> users = reader.lines()
                .skip(1)
                .map(s -> s.split(";"))
                .map(split -> new User(
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
