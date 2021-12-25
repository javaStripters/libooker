package ru.thecntgfy.libooker.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.*;

@Entity
@Getter @Setter
@ToString
public class Student extends User {
    @Column(columnDefinition = "text")
    private String lastname;

    @Column(columnDefinition = "text")
    private String firstname;

    @Column(columnDefinition = "text")
    private String patronymic;

    @Column(columnDefinition = "text")
    private String testbook;

    protected Student() {}

    public Student(String username, String password, String lastname, String firstname, String patronymic, String testbook) {
        super(username, password, Role.STUDENT);
        this.username = username;
        this.password = password;
        this.lastname = lastname;
        this.firstname = firstname;
        this.patronymic = patronymic;
        this.testbook = testbook;
        //TODO: Remove in prod :)
        this.role = lastname.equals("Афанасьев") ? Role.ADMIN : Role.STUDENT;
    }
}
