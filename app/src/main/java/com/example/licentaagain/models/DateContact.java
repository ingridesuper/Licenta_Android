package com.example.licentaagain.models;

import java.util.List;

public class DateContact {
    private String institutie;

    private String locatie;
    private String email;
    private String telefon;
    private String extra;

    public DateContact() { //required!
    }

    public DateContact(String institutie, String locatie, String email) {
        this.institutie = institutie;
        this.locatie = locatie;
        this.email = email;
    }

    public DateContact(String institutie, String locatie, String email, String telefon, String extra) {
        this.institutie = institutie;
        this.locatie = locatie;
        this.email = email;
        this.telefon = telefon;
        this.extra = extra;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public String getInstitutie() {
        return institutie;
    }

    public void setInstitutie(String institutie) {
        this.institutie = institutie;
    }

    public String getLocatie() {
        return locatie;
    }

    public void setLocatie(String locatie) {
        this.locatie = locatie;
    }

    public String getTelefon() {
        return telefon;
    }

    public void setTelefon(String telefon) {
        this.telefon = telefon;
    }

    @Override
    public String toString() {
        return "DateContact{" +
                "email='" + email + '\'' +
                ", institutie='" + institutie + '\'' +
                ", locatie='" + locatie + '\'' +
                ", telefon='" + telefon + '\'' +
                ", extra='" + extra + '\'' +
                '}';
    }
}
