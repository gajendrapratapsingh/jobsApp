package com.example.callback;

import com.example.model.Job;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class JobListCallback {

    @SerializedName("JOBS_APP")
    public ArrayList<Job> jobList = new ArrayList<>();
    @SerializedName("load_more")
    public boolean loadMore = false;
}
