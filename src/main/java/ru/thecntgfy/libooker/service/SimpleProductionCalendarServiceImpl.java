package ru.thecntgfy.libooker.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import ru.thecntgfy.libooker.repository.DayOffRepo;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class SimpleProductionCalendarServiceImpl {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    //TODO: Covid days
    private final String URL = "https://isdayoff.ru/";
    private final DayOffRepo dayOffRepo;

    public boolean isDayOff(LocalDate date) {
        return isDayOffExternal(date) || isDayOfInternal(date);
    }

    //TODO: Catch exceptions
    @SneakyThrows
    protected boolean isDayOffExternal(LocalDate date)  {
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

    protected boolean isDayOfInternal(LocalDate date) {
        return dayOffRepo.findByDate(date).isPresent();
    }
}
