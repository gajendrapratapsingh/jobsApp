package com.example.callback;

import com.example.model.Company;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class CompanyListCallback {

    @SerializedName("JOBS_APP")
    public ArrayList<Company> companyList = new ArrayList<>();
    @SerializedName("load_more")
    public boolean loadMore = false;
}
