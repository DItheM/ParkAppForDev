package com.example.parkapp.database;

public class BookRequest {
    public String uid;
    public String requestId;
    public String spotId;
    public String date;
    public String fromTime;
    public String toTime;
    public Boolean confirmed;
    public String price;

    public BookRequest () {
    }

    public BookRequest(String uid, String requestId, String spotId, String date, String fromTime, String toTime, Boolean confirmed, String price) {
        this.uid = uid;
        this.requestId = requestId;
        this.spotId = spotId;
        this.date = date;
        this.fromTime = fromTime;
        this.toTime = toTime;
        this.confirmed = confirmed;
        this.price = price;
    }

    public String getUid() {
        return uid;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getSpotId() {
        return spotId;
    }

    public String getDate() {
        return date;
    }

    public String getFromTime() {
        return fromTime;
    }

    public String getToTime() {
        return toTime;
    }

    public String getPrice() {
        return price;
    }

    public Boolean getConfirmed() {
        return confirmed;
    }
}
