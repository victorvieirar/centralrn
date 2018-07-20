package com.atwork.centralrn.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Result implements Serializable {

    @SerializedName("resultDate")
    private String datetime;

    @SerializedName("position")
    private int position;

    @SerializedName("chiliad")
    private int chiliad;

    @SerializedName("dozen")
    private int dozen;

    @SerializedName("animalName")
    private String animalName;

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getChiliad() {
        return chiliad;
    }

    public void setChiliad(int chiliad) {
        this.chiliad = chiliad;
    }

    public int getDozen() {
        return dozen;
    }

    public void setDozen(int dozen) {
        this.dozen = dozen;
    }

    public String getAnimalName() {
        return animalName;
    }

    public void setAnimalName(String animalName) {
        this.animalName = animalName;
    }
}
