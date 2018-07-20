package com.atwork.centralrn.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Contributor implements Serializable {

    @SerializedName("cod")
    private int cod;

    @SerializedName("name")
    private String name;

    @SerializedName("address")
    private String address;

    @SerializedName("number")
    private String number;

    @SerializedName("accesslevel")
    private String accesslevel;

    @SerializedName("token")
    private String token;

    @SerializedName("cashier")
    private double cashier;

    public Contributor() {}

    public int getCod() {
        return cod;
    }

    public void setCod(int cod) {
        this.cod = cod;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getAccesslevel() {
        return accesslevel;
    }

    public void setAccesslevel(String accesslevel) {
        this.accesslevel = accesslevel;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setCashier(double cashier) {
        this.cashier = cashier;
    }

    public double getCashier() { return this.cashier; }
}
