package ru.thecntgfy.libooker.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.thecntgfy.libooker.utils.TimeRange;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

@Entity
@Getter
@Setter
@ToString
public class Booking extends BaseEntity {
    public static final LocalTime OPENS = LocalTime.of(10, 0);

    @Id
    @GeneratedValue
    protected Long id;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, optional = false)
            @JsonManagedReference
    @ToString.Exclude
    private Workplace workplace;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, optional = false)
            @JsonManagedReference
    @ToString.Exclude
    private User user;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Transient
    private TimeRange timeRange;

    @Setter(AccessLevel.PRIVATE)
    private boolean canceled = false;

    @Setter(AccessLevel.PRIVATE)
    private boolean finishedManually = false;

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

    public Booking cancel() {
        if (canceled) throw new IllegalStateException("Cannot cancel already cancelled booking!");
        canceled = true;
        return this;
    }

    public Booking finish() {
        if (finishedManually) throw new IllegalStateException("Cannot finisged already finished booking!");
        finishedManually = true;
        return this;
    }
}
