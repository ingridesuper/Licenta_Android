package com.example.licentaagain.models;

import java.io.Serializable;
import java.util.Objects;

public class User implements Serializable {
    private String uid;
    private String name;
    private String surname;
    private String email;
    private int sector; //nu l-am pus for now de type sector pt ca firestore nu suporta enums

    public User() {}
    //user for default sign up
    public User(String uid, String email, String name, String surname, int sector) {
        this.uid=uid;
        this.email = email;
        this.name = name;
        this.surname = surname;
        this.sector=sector;
    }

    //used for google signup
    public User(String uid, String email){
        this.uid=uid;
        this.email=email;
        this.name=name;
        this.surname=surname;
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

    public int getSector() {
        return sector;
    }

    public void setSector(int sector) {
        this.sector = sector;
    }



    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", uid='" + uid + '\'' +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", sector=" + sector +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(uid, user.uid);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uid);
    }
}
