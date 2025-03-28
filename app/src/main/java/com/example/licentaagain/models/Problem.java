package com.example.licentaagain.models;

public class Problem {
    private String title;
    private String description;
    private String authorUid;
    private int sector;
    private String address; //the human readable one?
    private double latitude;
    private double longitude;
    private String categorieProblema;

    private Problem(){
        //for Firebase
    }

    public Problem(String address, String authorUid, String description, double latitude, double longitude, int sector, String title, String categorieProblema) {
        this.address = address;
        this.authorUid = authorUid;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.sector = sector;
        this.title = title;
        this.categorieProblema=categorieProblema;
    }

    public String getCategorieProblema() {
        return categorieProblema;
    }

    public void setCategorieProblema(String categorieProblema) {
        this.categorieProblema = categorieProblema;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAuthorUid() {
        return authorUid;
    }

    public void setAuthorUid(String authorUid) {
        this.authorUid = authorUid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getSector() {
        return sector;
    }

    public void setSector(int sector) {
        this.sector = sector;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "Problem{" +
                "address='" + address + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", authorUid='" + authorUid + '\'' +
                ", sector=" + sector +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", categorieProblema='" + categorieProblema + '\'' +
                '}';
    }

}
