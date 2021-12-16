package ru.thecntgfy.libooker;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAdminServer
public class LibookerApplication {

    public static void main(String[] args) {
        SpringApplication.run(LibookerApplication.class, args);
    }

}
