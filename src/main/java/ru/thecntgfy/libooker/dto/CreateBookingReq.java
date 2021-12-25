package ru.thecntgfy.libooker.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.Value;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class CreateBookingReq {
    long userId;
    //TODO: Prod: Uncomment
//    @FutureOrPresent
    @NotNull @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    LocalDateTime time;
}
