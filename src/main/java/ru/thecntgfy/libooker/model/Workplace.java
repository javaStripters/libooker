package ru.thecntgfy.libooker.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.Setter;
import org.springframework.core.Ordered;

import javax.persistence.*;
import java.util.Set;
import java.util.SortedSet;

@Entity
@Getter
@Setter
public class Workplace {
    @Id
    @GeneratedValue
    private Long id;

    @Column(columnDefinition = "text")
    private String name;

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.PERSIST}, mappedBy = "workplace")
    @OrderBy
            @JsonBackReference
    Set<Booking> bookings;

    protected Workplace() {}

    public Workplace(String name) {
        this.name = name;
    }
}
