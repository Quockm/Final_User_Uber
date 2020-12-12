package com.example.final_user_uber.model;

public class RiderModel {
    private String fisrtnasme, lastname, phonenumber, avatar;
    private double rating;


    public RiderModel() {
    }

    public String getFisrtnasme() {
        return fisrtnasme;
    }

    public void setFisrtnasme(String fisrtnasme) {
        this.fisrtnasme = fisrtnasme;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
