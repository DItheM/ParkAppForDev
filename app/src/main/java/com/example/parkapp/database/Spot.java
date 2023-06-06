package com.example.parkapp.database;

public class Spot {
    public String uid;
    public String name;
    public String charge;
    public String latitude;
    public String longitude;
    public String spotId;

    public Spot () {

    }

    public Spot(String name, String charge, String latitude, String longitude) {
        this.name = name;
        this.charge = charge;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Spot(String uid, String name, String charge, String latitude, String longitude) {
        this.uid = uid;
        this.name = name;
        this.charge = charge;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Spot(String spotId, String name, String charge) {
        this.spotId = spotId;
        this.name = name;
        this.charge = charge;
    }

    public String getName() {
        return name;
    }

    public String getCharge() {
        return charge;
    }

    public String getSpotId() {
        return spotId;
    }
}
