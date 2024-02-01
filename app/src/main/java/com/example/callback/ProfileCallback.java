package com.example.callback;

import com.example.model.Profile;
import com.google.gson.annotations.SerializedName;

public class ProfileCallback {

    @SerializedName("JOBS_APP")
    public Profile profile;
    @SerializedName("success")
    public int success;
    @SerializedName("current_plan")
    public String currentPlan;
    @SerializedName("expired_date")
    public String expiredDate;
    @SerializedName("last_invoice_date")
    public String lsDate;
    @SerializedName("last_invoice_plan_name")
    public String lsPlanName;
    @SerializedName("last_invoice_plan_amount")
    public String lsPlanAmount;
}
