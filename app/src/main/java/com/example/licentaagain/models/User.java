package com.example.licentaagain.models;

import java.io.Serializable;
import java.util.Objects;

public class User implements Serializable {
    private String uid;
    private String name;
    private String surname;
    private String email;
    private boolean isAdmin;
    private int sector;

    private boolean isDisabled;

    public User() {}
    //user for default sign up
    public User(String uid, String email, String name, String surname, int sector) {
        this.uid=uid;
        this.email = email;
        this.name = name;
        this.surname = surname;
        this.sector=sector;
        this.isAdmin=false;
        this.isDisabled=false;
    }

    //used for google signup
    public User(String uid, String email){
        this.uid=uid;
        this.email=email;
        this.name=name;
        this.surname=surname;
        this.isAdmin=false;
        this.isDisabled=false;
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

    public void setUid(String uid) {
        this.uid = uid;
    }

    public boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(boolean admin) {
        isAdmin = admin;
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

    public boolean getIsDisabled() {
        return isDisabled;
    }

    public void setIsDisabled(boolean disabled) {
        isDisabled = disabled;
    }

    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", uid='" + uid + '\'' +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", isAdmin=" + isAdmin +
                ", sector=" + sector +
                ", isDisabled=" + isDisabled +
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
