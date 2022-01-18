package ru.thecntgfy.libooker.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.NaturalId;
import org.springframework.core.Ordered;

import javax.persistence.*;
import java.util.Set;
import java.util.SortedSet;

@Entity
@Getter
@Setter
public class Workplace extends BaseEntity {
    @Id
    @GeneratedValue
    private Long id;

    @NaturalId
    @Column(columnDefinition = "text", unique = true)
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
