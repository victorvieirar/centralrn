package com.atwork.centralrn.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Bet implements Serializable{

    @SerializedName("ticket") private long ticket;
    @SerializedName("betDate") private String datetime;
    @SerializedName("bet") private int bet;
    @SerializedName("type") private int type;
    @SerializedName("giftChiliad") private int giftChiliad;
    @SerializedName("contributor") private int contributor;
    @SerializedName("schedule") private String schedule;
    @SerializedName("value") private double value;

    public long getTicket() {
        return ticket;
    }

    public void setTicket(long ticket) {
        this.ticket = ticket;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public int getBet() {
        return bet;
    }

    public void setBet(int bet) {
        this.bet = bet;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getGiftChiliad() {
        return giftChiliad;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public void setGiftChiliad(int giftChiliad) {
        this.giftChiliad = giftChiliad;
    }

    public int getContributor() {
        return contributor;
    }

    public void setContributor(int contributor) {
        this.contributor = contributor;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>();

        map.put("ticket", ticket+"");
        map.put("betDate", datetime);
        map.put("bet", bet+"");
        map.put("type", type+"");
        map.put("giftChiliad", giftChiliad+"");
        map.put("contributor", contributor+"");
        map.put("schedule", schedule);
        map.put("value", value+"");

        return map;
    }
}
