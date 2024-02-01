package com.example.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Plan implements Parcelable {
    @SerializedName("plan_id")
    public String planId;
    @SerializedName("plan_name")
    public String planName;
    @SerializedName("plan_duration")
    public String planDuration;
    @SerializedName("plan_price")
    public String planPrice;
    @SerializedName("plan_job_limit")
    public String planJobLimit;
    @SerializedName("currency_code")
    public String planCurrencyCode;

    protected Plan(Parcel in) {
        planId = in.readString();
        planName = in.readString();
        planDuration = in.readString();
        planPrice = in.readString();
        planJobLimit = in.readString();
        planCurrencyCode = in.readString();
    }

    public static final Creator<Plan> CREATOR = new Creator<Plan>() {
        @Override
        public Plan createFromParcel(Parcel in) {
            return new Plan(in);
        }

        @Override
        public Plan[] newArray(int size) {
            return new Plan[size];
        }
    };

    public String getPlanId() {
        return planId;
    }

    public String getPlanName() {
        return planName;
    }

    public String getPlanDuration() {
        return planDuration;
    }

    public String getPlanPrice() {
        return planPrice;
    }

    public String getPlanJobLimit() {
        return planJobLimit;
    }

    public String getPlanCurrencyCode() {
        return planCurrencyCode;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int i) {
        dest.writeString(planId);
        dest.writeString(planName);
        dest.writeString(planDuration);
        dest.writeString(planPrice);
        dest.writeString(planJobLimit);
        dest.writeString(planCurrencyCode);
    }
}
