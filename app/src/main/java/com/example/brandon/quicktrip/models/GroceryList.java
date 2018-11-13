package com.example.brandon.quicktrip.models;

import java.io.Serializable;
import java.util.Date;
import com.google.firebase.firestore.ServerTimestamp;

public class GroceryList implements Serializable {

    private String listID;
    private String listName;
    private String createdBy;

    @ServerTimestamp
    private Date date;

    public GroceryList() {

    }

    public GroceryList(String listID, String listName, String createdBy) {
        this.listID = listID;
        this.listName = listName;
        this.createdBy = createdBy;
    }

    public String getListID() {
        return listID;
    }

    public String getListName() {
        return listName;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Date getDate() {
        return date;
    }
}
