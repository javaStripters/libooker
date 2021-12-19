package ru.thecntgfy.libooker.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

@Table(name = "users")
@Entity
@Getter @Setter
@ToString
public class User {
    @Id @GeneratedValue
    Long id;

    @Column(columnDefinition = "text")
    String username;

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, mappedBy = "user")
    @OrderBy
            @JsonBackReference
    Set<Booking> bookings;

    protected User() {}

    public User(String username) {
        this.username = username;
    }
}
