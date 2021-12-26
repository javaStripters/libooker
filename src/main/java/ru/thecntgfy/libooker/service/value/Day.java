package ru.thecntgfy.libooker.service.value;

import java.time.LocalDate;

public record Day(LocalDate date, State state) {
    public enum State { WORKING, DAYOFF, UNMODIFIABLE_DAYOFF }
}
