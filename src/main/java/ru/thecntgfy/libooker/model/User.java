package ru.thecntgfy.libooker.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.*;

@Table(name = "users")
@Entity
@Getter @Setter
//TODO: Remove
@ToString
public class User {
    @Id @GeneratedValue
    protected Long id;

    @Column(columnDefinition = "text", unique = true)
    private String username;

    @Column(columnDefinition = "text")
    private String password;

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, mappedBy = "user")
    @OrderBy
    @JsonBackReference
    private Set<Booking> bookings;

    //TODO: Remove
    @Enumerated(EnumType.STRING)
    Role role;

    protected User() {}

    public User(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }
}
