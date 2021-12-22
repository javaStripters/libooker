package ru.thecntgfy.libooker.service;

import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;

@Service
public class SimpleProductionCalendarServiceImpl {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    //TODO: Covid days
    private final String URL = "https://isdayoff.ru/";

    //TODO: Catch exceptions
    @SneakyThrows
    public boolean isDayOff(LocalDate date)  {
        HttpRequest request = HttpRequest.newBuilder(new URI(URL + date)).GET().build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        int res = Integer.parseInt(response.body());

        return switch (res) {
            case 0, 4 -> false;
            case 1 -> true;
            case 100 -> throw new IllegalArgumentException();
            default -> throw new IllegalStateException("External service body response unexpected: " + res);
        };
    }
}
