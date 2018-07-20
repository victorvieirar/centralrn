package com.atwork.centralrn;

import com.atwork.centralrn.model.Bet;
import com.atwork.centralrn.model.Contributor;
import com.atwork.centralrn.model.Ticket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class App {

    private static final App INSTANCE = new App();

    private Contributor contributor = null;
    private Ticket openedTicket = null;
    private List<Bet> bets = new ArrayList<>();
    public boolean hasGiftChiliad = false;

    public App() {}

    public static App getInstance() { return INSTANCE; }

    public void setupContributor(Contributor contributor) {
        this.contributor = contributor;
    }

    public Contributor getContributor() { return this.contributor; }

    public void openTicket(Ticket ticket) { this.openedTicket = ticket; }
    public void closeTicket() { this.openedTicket = null; }
    public boolean hasTicket() { return this.openedTicket != null; }
    public Ticket getTicket() { return this.openedTicket; }

    public void addBet(Bet bet) { this.bets.add(bet); }
    public void resetBets() { this.bets = new ArrayList<>(); }
    public List<Bet> getBets() { return this.bets; }

    public static final String DOMAIN = "http://10.0.0.100/";

}
