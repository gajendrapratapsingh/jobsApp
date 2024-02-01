package com.example.callback;

import com.example.model.User;
import com.google.gson.annotations.SerializedName;

public class LoginCallback {

    @SerializedName("msg")
    public String message;
    @SerializedName("JOBS_APP")
    public User user;
    @SerializedName("success")
    public int success;
}
