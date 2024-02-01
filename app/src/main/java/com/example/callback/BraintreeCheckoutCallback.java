package com.example.callback;

import com.google.gson.annotations.SerializedName;

/*
    this callback is used for api which have only msg and success
 */
public class BraintreeCheckoutCallback {

    @SerializedName("paypal_payment_id")
    public String paypalPaymentId;
    @SerializedName("msg")
    public String message;
    @SerializedName("success")
    public int success;
}
