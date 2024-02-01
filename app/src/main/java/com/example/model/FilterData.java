package com.example.model;

import com.google.gson.annotations.SerializedName;

public class FilterData {
    @SerializedName("post_id")
    private String postId;
    @SerializedName("post_title")
    private String postTitle;

    public String getPostId() {
        return postId;
    }

    public String getPostTitle() {
        return postTitle;
    }
}
