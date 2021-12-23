package ru.thecntgfy.libooker.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDate;

@Entity
public class DayOff {
    @Id
    LocalDate date;

    protected DayOff() {}

    public DayOff(LocalDate date) {
        this.date = date;
    }
}
