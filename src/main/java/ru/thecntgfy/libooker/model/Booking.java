package ru.thecntgfy.libooker.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Getter;
import lombok.Setter;
import ru.thecntgfy.libooker.utils.TimeRange;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

@Entity
@Getter
@Setter
public class Booking {
    public static final LocalTime OPENS = LocalTime.of(10, 0);

    @Id
    @GeneratedValue
    protected Long id;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
            @JsonManagedReference
    private Workplace workplace;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
            @JsonManagedReference
    private User user;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Transient
    private TimeRange timeRange;

    protected Booking() {}


    public Booking(Workplace workplace, User user, LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end))
            throw new IllegalArgumentException("Booking can`t begin after it ended!");
        if (!start.toLocalDate().equals(end.toLocalDate()))
            throw new IllegalArgumentException("Booking must begin and end during one day!");

        this.startTime = start.toLocalTime();
        this.endTime = end.toLocalTime();
        this.date = start.toLocalDate();
        this.workplace = workplace;
        this.user = user;
    }

    public Booking(Workplace workplace, User user, LocalDate date, TimeRange time) {
        this(workplace, user, LocalDateTime.of(date, time.from()), LocalDateTime.of(date, time.toInclusive()));
    }

    public TimeRange getTimeRange() {
        if (timeRange == null)
            timeRange = new TimeRange(startTime, endTime);
        return timeRange;
    }
}
