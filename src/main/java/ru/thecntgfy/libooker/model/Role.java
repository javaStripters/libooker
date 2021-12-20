package ru.thecntgfy.libooker.model;

public enum Role {
    STUDENT, TUTOR, ADMIN
}




//
//import lombok.Getter;
//
//import javax.persistence.*;
//
//@Entity
//@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"id", "role"})})
//@Getter
//public class Role {
//    public enum Values {STUDENT, TUTOR, ADMIN}
//    @Transient
//    public static final Role STUDENT = new Role(Values.STUDENT);
//    @Transient
//    public static final Role TUTOR = new Role(Values.TUTOR);
//    @Transient
//    //TODO: Rework
//    public static final Role ADMIN = new Role(Values.ADMIN);
//
//    @Id
//    @GeneratedValue
//    Long id;
//    @Enumerated(EnumType.STRING)
//    private Values role;
//
//    protected Role() {}
//
//    public Role(Values role) {
//        this.role = role;
//    }
//
//    @Override
//    public String toString() {
//        return role.toString();
//    }
//}
