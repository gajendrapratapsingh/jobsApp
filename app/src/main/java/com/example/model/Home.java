package com.example.model;

import java.util.ArrayList;

public class Home {
    private String homeId;
    private String homeTitle;
    private int homeType;
    private ArrayList<Object> itemHomeContents;
    public static final int RECENT = 1, COMPANY = 2, RECOMMEND = 3, CATEGORY = 4, LATEST = 5;

    public Home() {

    }

    public Home(String homeId, String homeTitle, int homeType, ArrayList<Object> itemHomeContents) {
        this.homeId = homeId;
        this.homeTitle = homeTitle;
        this.homeType = homeType;
        this.itemHomeContents = itemHomeContents;
    }


    public String getHomeId() {
        return homeId;
    }

    public String getHomeTitle() {
        return homeTitle;
    }

    public int getHomeType() {
        return homeType;
    }

    public ArrayList<Object> getItemHomeContents() {
        return itemHomeContents;
    }

}
