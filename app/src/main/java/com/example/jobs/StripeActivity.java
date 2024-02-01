package com.example.jobs;

import android.content.Intent;
import android.os.Bundle;
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
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StripeActivity extends PaymentBaseActivity {

    Plan plan;
    String planGateway = "Stripe", paymentId, stripePublisherKey;
    PaymentSheet paymentSheet;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        plan = intent.getParcelableExtra("planInfo");
        stripePublisherKey = intent.getStringExtra("stripePublisherKey");

        String payString = getString(R.string.pay_via, plan.getPlanPrice(), plan.planCurrencyCode, planGateway);
        viewBinding.btnPay.setText(payString);
        viewBinding.btnPay.setOnClickListener(view -> {
            viewBinding.btnPay.setVisibility(View.GONE);
            onRequest();
        });
        onRequest();
        setOnTryAgainListener(this::onRequest);
        initPaymentGateway();
        paymentSheet = new PaymentSheet(StripeActivity.this, this::onPaymentSheetResult);
    }

    private void onRequest() {
        if (NetworkUtils.isConnected(this)) {
            getPaymentToken();
        } else {
            showErrorState(State.STATE_NO_INTERNET);
        }
    }

    private void initPaymentGateway() {
        PaymentConfiguration.init(this, stripePublisherKey);
    }

    private void getPaymentToken() {
        showProgress(true);
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("amount", plan.getPlanPrice());
        ApiInterface apiInterface = RestAdapter.createAPI();
        Call<PaymentTokenCallback> callback = apiInterface.getStripeToken(API.toBase64(jsObj.toString()));
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
        paymentId = resp.stripePiId;
        paymentSheet.presentWithPaymentIntent(
                resp.stripePiClientSecret,
                new PaymentSheet.Configuration(
                        getString(R.string.payment_company_name),
                        new PaymentSheet.CustomerConfiguration(
                                resp.stripeCustomerId,
                                resp.stripeEphemeralKeySecret
                        )
                )
        );
    }

    private void onPaymentSheetResult(final PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            showError(planGateway, getString(R.string.payment_cancel));
        } else if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            showError(planGateway, getString(R.string.payment_failed));
        } else if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            GeneralUtils.addTransaction(StripeActivity.this, plan.getPlanId(), paymentId, planGateway);
        }
    }
}
