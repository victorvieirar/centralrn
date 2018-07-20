package com.atwork.centralrn.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Ticket implements Serializable {

    @SerializedName("ticked")
    private long id;

    public Ticket() {}
    public Ticket(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
