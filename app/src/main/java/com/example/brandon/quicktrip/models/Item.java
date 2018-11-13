package com.example.brandon.quicktrip.models;

public class Item {

    private String itemID;
    private String itemName;
    private Boolean show;

    public Item() {

    }

    public Item(String itemID, String itemName, Boolean show) {
        this.itemID = itemID;
        this.itemName = itemName;
        this.show = show;
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
}
