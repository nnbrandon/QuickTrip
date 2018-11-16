package com.example.brandon.quicktrip.models;

public class Item {

    private String itemID;
    private String itemName;
    private Boolean show;
    private String createdBy;

    public Item() {

    }

    public Item(String itemID, String itemName, Boolean show, String createdBy) {
        this.itemID = itemID;
        this.itemName = itemName;
        this.show = show;
        this.createdBy = createdBy;
    }

    public String getItemID() {
        return itemID;
    }

    public String getItemName() {
        return itemName;
    }

    public Boolean getShow() {
        return show;
    }

    public String getCreatedBy() {
        return createdBy;
    }
}
