package com.example.callback;

import com.google.gson.annotations.SerializedName;

/*
    this callback is used for api which have only msg and success
 */
public class JobSaveCallback {

    @SerializedName("msg")
    public String message;
    @SerializedName("success")
    public boolean success;
}
