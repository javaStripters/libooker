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

    @Column(columnDefinition = "text", unique = true, nullable = false)
    private String username;

    //TODO: proper colum defs
    @Column(columnDefinition = "text", nullable = false)
    private String password;

    @Column(columnDefinition = "text")
    private String lastname;

    @Column(columnDefinition = "text")
    private String firstname;

    @Column(columnDefinition = "text")
    private String patronymic;

    @Column(columnDefinition = "text")
    private String testbook;

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, mappedBy = "user")
    @OrderBy
    @JsonBackReference
    private Set<Booking> bookings = new HashSet<>();

    //TODO: Remove
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    Role role;

    protected User() {}

    public User(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public User(String username, String password, String lastname, String firstname, String patronymic, String testbook) {
        this.username = username;
        this.password = password;
        this.lastname = lastname;
        this.firstname = firstname;
        this.patronymic = patronymic;
        this.testbook = testbook;
        this.role = lastname.equals("Афанасьев") ? Role.ADMIN : Role.STUDENT;
    }
}
