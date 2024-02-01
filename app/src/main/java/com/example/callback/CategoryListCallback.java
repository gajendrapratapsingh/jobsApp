package com.example.callback;

import com.example.model.Category;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class CategoryListCallback {
    @SerializedName("JOBS_APP")
    public ArrayList<Category> categoryList = new ArrayList<>();
    @SerializedName("load_more")
    public boolean loadMore = false;

}
