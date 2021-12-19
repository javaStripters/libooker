package ru.thecntgfy.libooker;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableAdminServer
@EnableTransactionManagement
public class LibookerApplication {

    public static void main(String[] args) {
        SpringApplication.run(LibookerApplication.class, args);
    }

}
