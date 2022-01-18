package ru.thecntgfy.libooker;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.List;

@SpringBootApplication
@EnableTransactionManagement
@EnableAdminServer
@EnableJpaAuditing
public class LibookerApplication {

    public static void main(String[] args) {
        SpringApplication.run(LibookerApplication.class, args);
    }

    @Bean
    OpenApiCustomiser setServers() {
        return openApi -> openApi.setServers(List.of(
                new Server().url("https://thecntgfy.ru").description("Remote SSL"),
                new Server().url("http://129.159.248.238").description("Remote"),
                new Server().url("http://localhost:8080").description("Local"),
                new Server().url("http://localhost").description("Local Docker")));
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                .addSecuritySchemes("bearer-key", new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")));
    }
}
