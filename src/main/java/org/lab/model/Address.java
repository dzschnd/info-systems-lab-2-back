package org.lab.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "addresses")
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // Поле не может быть null, Значение поля должно быть уникальным, Значение этого поля должно генерироваться автоматически

    @Column(nullable = false)
    @NotNull
    @NotEmpty
    private String street; // Строка не может быть пустой, Поле не может быть null

    @Column(name = "zip_code", length = 30)
    @Size(max = 30)
    private String zipCode; // Длина строки не должна быть больше 30, Поле может быть null

    @ManyToOne
    @JoinColumn(nullable = false)
    @NotNull
    private Location town; // Поле не может быть null

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    @NotNull
    private User author; // Поле не может быть null


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public Location getTown() {
        return town;
    }

    public void setTown(Location town) {
        this.town = town;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }
}
