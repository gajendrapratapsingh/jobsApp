package com.example.model;

import com.google.gson.annotations.SerializedName;

public class Category {

    @SerializedName("post_id")
    private String categoryId;
    @SerializedName("post_title")
    private String categoryTitle;
    @SerializedName("post_image")
    private String categoryImage;
    @SerializedName("total_jobs")
    private String categoryNoOfJobs;

    public String getCategoryId() {
        return categoryId;
    }

    public String getCategoryTitle() {
        return categoryTitle;
    }

    public String getCategoryImage() {
        return categoryImage;
    }

    public String getCategoryNoOfJobs() {
        return categoryNoOfJobs;
    }
}
