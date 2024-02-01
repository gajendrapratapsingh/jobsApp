package com.example.callback;

import com.example.model.Plan;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class PlanListCallback {
    @SerializedName("JOBS_APP")
    public ArrayList<Plan> planList = new ArrayList<>();

}
