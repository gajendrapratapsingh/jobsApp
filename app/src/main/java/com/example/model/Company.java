package com.example.model;

import com.google.gson.annotations.SerializedName;

public class Company {
    @SerializedName("post_id")
    private String companyId;
    @SerializedName("post_title")
    private String companyTitle;
    @SerializedName("post_image")
    private String companyImage;
    @SerializedName("city")
    private String companyLocation;

    public String getCompanyId() {
        return companyId;
    }

    public String getCompanyTitle() {
        return companyTitle;
    }

    public String getCompanyImage() {
        return companyImage;
    }

    public String getCompanyLocation() {
        return companyLocation;
    }
}
