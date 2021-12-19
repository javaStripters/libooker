package ru.thecntgfy.libooker.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class DateTimeRange {
    private LocalDateTime from;
    private LocalDateTime to;
}
