package com.example.callback;

import com.google.gson.annotations.SerializedName;

public class PaymentTokenCallback {
    @SerializedName("client_token")
    public String braintreeAuthToken;

    @SerializedName("stripe_payment_token")
    public String stripePiClientSecret;
    @SerializedName("ephemeralKey")
    public String stripeEphemeralKeySecret;
    @SerializedName("customer")
    public String stripeCustomerId;
    @SerializedName("id")
    public String stripePiId;

    @SerializedName("order_id")
    public String razorPayOrderId;

    @SerializedName("payu_hash")
    public String payUHash;

    @SerializedName("success")
    public int success;
    @SerializedName("msg")
    public String message;

}
