package com.example.licentaagain.models;

import java.io.Serializable;

public class User implements Serializable {
    private String uid;
    private String name;
    private String surname;
    private String email;
    private int sector;

    public User(String uid, String email, String name, String surname, int sector) {
        this.uid=uid;
        this.email = email;
        this.name = name;
        this.surname = surname;
        this.sector=sector;
    }

    public String getUid() {
        return uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                '}';
    }
}
