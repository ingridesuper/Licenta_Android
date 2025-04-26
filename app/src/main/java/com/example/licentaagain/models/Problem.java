package com.example.licentaagain.models;

import com.example.licentaagain.enums.StareProblema;
import com.google.firebase.Timestamp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Problem implements Serializable {
    private String id; //ma ajuta cu contains (cu setterul)
    private String title;
    private String description;
    private String authorUid;
    private int sector;
    private String address; //the human readable one?
    private double latitude;
    private double longitude;
    private String categorieProblema;
    private Timestamp createDate;
    private List<String> imageUrls;
    private String stareProblema;

    private Problem(){
        //for Firebase
    }

    public Problem(String address, String authorUid, String description, double latitude, double longitude, int sector, String title, String categorieProblema, StareProblema stareProblema) {
        this.address = address;
        this.authorUid = authorUid;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.sector = sector;
        this.title = title;
        this.categorieProblema=categorieProblema;
        this.imageUrls=new ArrayList<>();
        this.stareProblema=stareProblema.getStare();
    }

    public Problem(String address, String authorUid, String description, double latitude, double longitude, int sector, String title, String categorieProblema, List<String> imageUrls, StareProblema stareProblema) {
        this.address = address;
        this.authorUid = authorUid;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.sector = sector;
        this.title = title;
        this.categorieProblema = categorieProblema;
        this.imageUrls = imageUrls;
        this.stareProblema=stareProblema.getStare();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategorieProblema() {
        return categorieProblema;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
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

    public String getStareProblema() {
        return stareProblema;
    }

    public void setStareProblema(String stareProblema) {
        this.stareProblema = stareProblema;
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

    public Timestamp getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Timestamp createDate) {
        this.createDate = createDate;
    }

    @Override
    public String toString() {
        return "Problem{" +
                "address='" + address + '\'' +
                ", id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", authorUid='" + authorUid + '\'' +
                ", sector=" + sector +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", categorieProblema='" + categorieProblema + '\'' +
                ", createDate=" + createDate +
                ", imageUrls=" + imageUrls +
                ", stareProblema='" + stareProblema + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) { //pt contains
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Problem problem = (Problem) o;
        return id != null && id.equals(problem.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
