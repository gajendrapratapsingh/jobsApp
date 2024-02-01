package com.example.model;

import com.example.util.AppUtil;
import com.google.gson.annotations.SerializedName;

public class User {

    @SerializedName("user_id")
    public String userId = "";
    @SerializedName("name")
    public String userName = "";
    @SerializedName("email")
    public String userEmail = "";
    @SerializedName("phone")
    public String userPhone = "";
    @SerializedName("user_image")
    public String userImage = "";
    @SerializedName("usertype")
    public String userType = AppUtil.USER_TYPE_USER;

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public String getUserImage() {
        return userImage;
    }

    public String getUserType() {
        return userType;
    }
}
