package com.example.model;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class Gateway {

    @SerializedName("gateway_id")
    private int gatewayId;
    @SerializedName("gateway_name")
    private String gatewayName;
    @SerializedName("gateway_logo")
    private String gatewayLogo;
    @SerializedName("gateway_info")
    private JsonObject gatewayInfo;

    public int getGatewayId() {
        return gatewayId;
    }

    public String getGatewayName() {
        return gatewayName;
    }

    public String getGatewayLogo() {
        return gatewayLogo;
    }

    public JsonObject getGatewayInfo() {
        return gatewayInfo;
    }
}
