package com.example.model;

import com.google.gson.annotations.SerializedName;

/*
this class is sub part of user
 */
public class Profile extends User {
    @SerializedName("city")
    public String userCity;
    @SerializedName("address")
    public String userAddress;
    @SerializedName("date_of_birth")
    public String userDob;
    @SerializedName("gender")
    public String userGender;
    @SerializedName("current_company")
    public String userCurrentCompany;
    @SerializedName("skills")
    public String userSkills;
    @SerializedName("experience")
    public String userExp;
    @SerializedName("saved_jobs")
    public int savedJobCount;
    @SerializedName("applied_jobs")
    public int appliedJobCount;
    @SerializedName("resume")
    public String userResume;

    public String getUserCity() {
        return userCity;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public String getUserDob() {
        return userDob;
    }

    public String getUserGender() {
        return userGender;
    }

    public String getUserCurrentCompany() {
        return userCurrentCompany;
    }

    public String getUserSkills() {
        return userSkills;
    }

    public String getUserExp() {
        return userExp;
    }

    public int getSavedJobCount() {
        return savedJobCount;
    }

    public int getAppliedJobCount() {
        return appliedJobCount;
    }

    public String getUserResume() {
        return userResume;
    }
}
