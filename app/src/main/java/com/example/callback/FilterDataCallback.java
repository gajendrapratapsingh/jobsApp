package com.example.callback;

import com.example.model.FilterData;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class FilterDataCallback {
    @SerializedName("category_list")
    public ArrayList<FilterData> categoryList = new ArrayList<>();
    @SerializedName("location_list")
    public ArrayList<FilterData> cityList = new ArrayList<>();
    @SerializedName("company_list")
    public ArrayList<FilterData> companyList = new ArrayList<>();
    @SerializedName("job_types_list")
    public ArrayList<FilterData> jobTypeList = new ArrayList<>();
    @SerializedName("qualification_list")
    public ArrayList<FilterData> qualificationList = new ArrayList<>();
    @SerializedName("min_salary")
    public float minSalary;
    @SerializedName("max_salary")
    public float maxSalary;
}
