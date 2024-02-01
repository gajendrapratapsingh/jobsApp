package com.example.callback;

import com.example.model.Gateway;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class GatewayListCallback {
    @SerializedName("JOBS_APP")
    public ArrayList<Gateway> gatewayList = new ArrayList<>();

}
