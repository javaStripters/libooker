package ru.thecntgfy.libooker.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDate;

@Entity
public class DayOff {
    @Id
    @GeneratedValue
    Integer id;

    LocalDate date;

    protected DayOff() {}

    public DayOff(LocalDate date) {
        this.date = date;
    }
}
