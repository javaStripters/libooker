package ru.thecntgfy.libooker.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import ru.thecntgfy.libooker.repository.DayOffRepo;
import ru.thecntgfy.libooker.service.value.Day;
import ru.thecntgfy.libooker.utils.Pair;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SimpleProductionCalendarServiceImpl {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    //TODO: Covid days
    private final String baseURL = "https://isdayoff.ru/";
    private final String monthURLtemplate = "https://isdayoff.ru/api/getdata?date1=%s&date2=%s&delimeter=%%0A";
    private final DayOffRepo dayOffRepo;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    public boolean isDayOff(LocalDate date) {
        return isDayOffExternal(date) || isDayOfInternal(date);
    }

    //TODO: Rework
    @SneakyThrows
    public List<Day> forRange(LocalDate from, LocalDate to) {
        String formattedFrom = from.format(dateFormatter);
        String formattedTo = to.format(dateFormatter);
        URI url = new URI(monthURLtemplate.formatted(formattedFrom, formattedTo));
        System.out.println(url);
        HttpRequest request = HttpRequest.newBuilder(url).GET().build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.body());
        String[] vals = response.body().split("\n");

        ArrayList<Day> days = new ArrayList<>();

        LocalDate curr = from;
        int idx = 0;
        while (!curr.isAfter(to)) {
            Day.State state = responseToState(Integer.parseInt(vals[idx]));
            state = state == Day.State.UNMODIFIABLE_DAYOFF
                    ? state
                    : isDayOfInternal(curr)
                        ? Day.State.DAYOFF
                        : Day.State.WORKING;
            days.add(new Day(curr, state));

            curr = curr.plusDays(1);
            idx++;
        }

        return days;
    }

    @SneakyThrows
    protected boolean isDayOffExternal(LocalDate date)  {
        HttpRequest request = HttpRequest.newBuilder(new URI(baseURL + date)).GET().build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        char res = response.body().charAt(0);

        return responseToState(res) == Day.State.UNMODIFIABLE_DAYOFF;
    }

    protected boolean isDayOfInternal(LocalDate date) {
        return dayOffRepo.findByDate(date).isPresent();
    }

    protected Day.State responseToState(int res) {
        return switch (res) {
            case 0, 4 -> Day.State.WORKING;
            case 1 -> Day.State.UNMODIFIABLE_DAYOFF;
            case 100 -> throw new IllegalArgumentException();
            default -> throw new IllegalStateException("External service body response unexpected: " + res);
        };
    }
}
