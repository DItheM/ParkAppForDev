package com.example.parkapp.database;

public class User {
    public String userId;
    public String email;
    public String accType;
    public String username;
    public String phoneNo;
    public String licenseNo;
    public String model;
    public String vehicleType;
    public String pfpURL;
    public String description;



    public User(String userId, String email, String accType) {
        this.userId = userId;
        this.email = email;
        this.accType = accType;
    }

    public User(String accType, String username, String phoneNo, String licenseNo, String model, String vehicleType, String pfpURL) {
        this.accType = accType;
        this.username = username;
        this.phoneNo = phoneNo;
        this.licenseNo = licenseNo;
        this.model = model;
        this.vehicleType = vehicleType;
        this.pfpURL = pfpURL;
    }

    public User(String accType, String username, String phoneNo, String description, String pfpURL) {
        this.accType = accType;
        this.username = username;
        this.phoneNo = phoneNo;
        this.description = description;
        this.pfpURL = pfpURL;
    }

    public String getUserId () {
        return userId;
    }

    public String getAccType () {
        return accType;
    }

    public String getEmail () {
        return email;
    }

}
