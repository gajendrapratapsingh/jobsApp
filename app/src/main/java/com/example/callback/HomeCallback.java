package com.example.callback;

import com.example.model.Category;
import com.example.model.Company;
import com.example.model.Job;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class HomeCallback {
    @SerializedName("category_list")
    public ArrayList<Category> categoryList = new ArrayList<>();
    @SerializedName("latest_jobs_list")
    public ArrayList<Job> latestJobList = new ArrayList<>();
    @SerializedName("recently_list")
    public ArrayList<Job> recentJobList = new ArrayList<>();
    @SerializedName("recommend_jobs_list")
    public ArrayList<Job> recommendJobList = new ArrayList<>();
    @SerializedName("company_list")
    public ArrayList<Company> companyList = new ArrayList<>();
}
