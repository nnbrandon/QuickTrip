package com.example.brandon.quicktrip.models;

public class User {

    private String userEmail;
    private String userName;
    private String tokenID;

    public User() {

    }

    public User(String userEmail, String userName, String tokenID) {
        this.userEmail = userEmail;
        this.userName = userName;
        this.tokenID = tokenID;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getUserName() {
        return userName;
    }

    public String getTokenID() {
        return tokenID;
    }
}
