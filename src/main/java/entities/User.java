package entities;

import annotations.Column;
import annotations.Entity;
import annotations.Id;

import java.time.LocalDate;


@Entity(name="users")
public final class User {

    @Id
    @Column(name = "id")
    private long id;

    @Column(name="username")
    private String username;
    @Column(name="age")
    private int age;
    @Column(name="registrationDate")
    private LocalDate registrationDate;


    public User(String username, int age, LocalDate registrationDate) {
        this.username = username;
        this.age = age;
        this.registrationDate = registrationDate;
    }

    public User(){

    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }
}
