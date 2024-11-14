package org.lab.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.util.Date;

@Entity
@Table(name = "persons")
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "eye_color", nullable = false)
    @NotNull // Поле не может быть null
    private String eyeColor;

    @Column(name = "hair_color", nullable = false)
    @NotNull // Поле не может быть null
    private String hairColor;

    @ManyToOne
    @JoinColumn(name = "location_id", nullable = false)
    @NotNull // Поле не может быть null
    private Location location;

    @Temporal(TemporalType.DATE)
    private Date birthday; // Поле может быть null

    @Column(nullable = false)
    @Min(1) // Значение поля должно быть больше 0
    private int height;

    @Column(name = "passport_id", unique = true)
    @NotNull // Строка не может быть пустой, Длина строки не должна быть больше 25, Значение этого поля должно быть уникальным, Поле может быть null
    @NotBlank
    @Size(max = 25)
    private String passportID;

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    @NotNull
    private User author;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEyeColor() {
        return eyeColor;
    }

    public void setEyeColor(String eyeColor) {
        this.eyeColor = eyeColor;
    }

    public String getHairColor() {
        return hairColor;
    }

    public void setHairColor(String hairColor) {
        this.hairColor = hairColor;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getPassportID() {
        return passportID;
    }

    public void setPassportID(String passportID) {
        this.passportID = passportID;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }
}
