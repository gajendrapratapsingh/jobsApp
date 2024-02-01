package com.example.jobs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.callback.PaymentTokenCallback;
import com.example.model.Plan;
import com.example.rest.ApiInterface;
import com.example.rest.RestAdapter;
import com.example.util.API;
import com.example.util.GeneralUtils;
import com.example.util.NetworkUtils;
import com.example.util.State;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RazorPayActivity extends PaymentBaseActivity implements PaymentResultListener {

    Plan plan;
    String planGateway = "Razorpay", razorPayKey;
    MyApplication myApplication;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        plan = intent.getParcelableExtra("planInfo");
        razorPayKey = intent.getStringExtra("razorPayKey");
        myApplication = MyApplication.getInstance();

        String payString = getString(R.string.pay_via, plan.getPlanPrice(), plan.planCurrencyCode, planGateway);
        viewBinding.btnPay.setText(payString);
        viewBinding.btnPay.setOnClickListener(view -> {
            viewBinding.btnPay.setVisibility(View.GONE);
            onRequest();
        });
        onRequest();
        setOnTryAgainListener(this::onRequest);
    }

    private void onRequest() {
        if (NetworkUtils.isConnected(this)) {
            getPaymentToken();
        } else {
            showErrorState(State.STATE_NO_INTERNET);
        }
    }

    private void getPaymentToken() {
        showProgress(true);
        double big = Double.parseDouble(plan.getPlanPrice());
        int amount = (int) (big) * 100;
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("user_id", myApplication.getLoginInfo().getUserId());
        jsObj.addProperty("amount", amount);
        ApiInterface apiInterface = RestAdapter.createAPI();
        Call<PaymentTokenCallback> callback = apiInterface.getRazorPayOrderId(API.toBase64(jsObj.toString()));
        callback.enqueue(new Callback<PaymentTokenCallback>() {
            @Override
            public void onResponse(@NonNull Call<PaymentTokenCallback> call, @NonNull Response<PaymentTokenCallback> response) {
                showProgress(false);
                PaymentTokenCallback resp = response.body();
                if (resp != null) {
                    if (resp.success == 1) {
                        startPayment(resp);
                    } else {
                        showErrorState(State.STATE_PAYMENT_TOKEN_ERROR);
                    }
                } else {
                    showErrorState(State.STATE_ERROR_IN_API);
                }
            }

            @Override
            public void onFailure(@NonNull Call<PaymentTokenCallback> call, @NonNull Throwable t) {
                if (!call.isCanceled()) {
                    showProgress(false);
                    showErrorState(State.STATE_ERROR_IN_API);
                }
            }
        });
    }

    private void startPayment(PaymentTokenCallback resp) {
        viewBinding.btnPay.setVisibility(View.VISIBLE);
        final Activity activity = this;
        final Checkout co = new Checkout();
        co.setKeyID(razorPayKey);

        try {
            JSONObject options = new JSONObject();
            options.put("name", getString(R.string.payment_company_name));
            options.put("description", plan.getPlanName());
            options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png");
            options.put("currency", plan.getPlanCurrencyCode());
            options.put("order_id", resp.razorPayOrderId);
            double big = Double.parseDouble(plan.getPlanPrice());
            int amount = (int) (big) * 100;
            options.put("amount", amount);

            JSONObject preFill = new JSONObject();
            preFill.put("email", myApplication.getLoginInfo().getUserEmail());
            //     preFill.put("contact", "9876543210");

            options.put("prefill", preFill);

            co.open(activity, options);
        } catch (Exception e) {
            showError(planGateway, "Error in payment: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @Override
    public void onPaymentSuccess(String razorpayPaymentID) {
        GeneralUtils.addTransaction(RazorPayActivity.this, plan.getPlanId(), razorpayPaymentID, planGateway);
    }

    @Override
    public void onPaymentError(int code, String response) {
        try {
            JSONObject jsonRes = new JSONObject(response);
            JSONObject jsonError = jsonRes.getJSONObject("error");
            showError(planGateway, "Payment failed: " + jsonError.getString("description"));
        } catch (Exception e) {
            Log.e("TAG", "Exception in onPaymentError", e);
        }
    }
}
