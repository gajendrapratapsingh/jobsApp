package com.example.jobs;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.braintreepayments.api.BraintreeFragment;
import com.braintreepayments.api.PayPal;
import com.braintreepayments.api.exceptions.InvalidArgumentException;
import com.braintreepayments.api.interfaces.BraintreeCancelListener;
import com.braintreepayments.api.interfaces.BraintreeErrorListener;
import com.braintreepayments.api.interfaces.PaymentMethodNonceCreatedListener;
import com.braintreepayments.api.models.PayPalRequest;
import com.example.callback.BraintreeCheckoutCallback;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PayPalActivity extends PaymentBaseActivity {

    BraintreeFragment mBraintreeFragment;
    Plan plan;
    String planGateway = "Paypal", authToken = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        plan = intent.getParcelableExtra("planInfo");

        String payString = getString(R.string.pay_via, plan.getPlanPrice(), plan.planCurrencyCode, planGateway);
        viewBinding.btnPay.setText(payString);
        viewBinding.btnPay.setOnClickListener(view -> makePaymentFromBraintree());
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
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        ApiInterface apiInterface = RestAdapter.createAPI();
        Call<PaymentTokenCallback> callback = apiInterface.getBraintreeToken(API.toBase64(jsObj.toString()));
        callback.enqueue(new Callback<PaymentTokenCallback>() {
            @Override
            public void onResponse(@NonNull Call<PaymentTokenCallback> call, @NonNull Response<PaymentTokenCallback> response) {
                showProgress(false);
                PaymentTokenCallback resp = response.body();
                if (resp != null) {
                    if (resp.success == 1) {
                        authToken = resp.braintreeAuthToken;
                        initBraintree();
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

    private void initBraintree() {
        try {
            mBraintreeFragment = BraintreeFragment.newInstance(this, authToken);
            mBraintreeFragment.addListener((PaymentMethodNonceCreatedListener) paymentMethodNonce -> {
                String nNonce = paymentMethodNonce.getNonce();
                checkoutNonce(nNonce);
            });
            mBraintreeFragment.addListener((BraintreeCancelListener) requestCode -> showError(planGateway, getString(R.string.payment_cancel)));

            mBraintreeFragment.addListener((BraintreeErrorListener) error -> showError(planGateway, error.getMessage()));

            viewBinding.btnPay.setVisibility(View.VISIBLE);
            GeneralUtils.showSuccessToast(PayPalActivity.this, getString(R.string.proceed_with_payment));
        } catch (InvalidArgumentException e) {
            // There was an issue with your authorization string.
            showErrorState(State.STATE_PAYMENT_TOKEN_ERROR);
        }
    }

    private void makePaymentFromBraintree() {
        PayPal.requestOneTimePayment(mBraintreeFragment, getPaypalRequest());
    }

    private PayPalRequest getPaypalRequest() {
        PayPalRequest request = new PayPalRequest(plan.getPlanPrice());
        request.currencyCode(plan.getPlanCurrencyCode());
        request.intent(PayPalRequest.INTENT_SALE);
        return request;
    }

    private void checkoutNonce(String paymentNonce) {
        showProgressDialog();

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("payment_nonce", paymentNonce);
        jsObj.addProperty("payment_amount", plan.getPlanPrice());

        ApiInterface apiInterface = RestAdapter.createAPI();
        Call<BraintreeCheckoutCallback> callback = apiInterface.braintreeCheckout(API.toBase64(jsObj.toString()));
        callback.enqueue(new Callback<BraintreeCheckoutCallback>() {
            @Override
            public void onResponse(@NonNull Call<BraintreeCheckoutCallback> call, @NonNull Response<BraintreeCheckoutCallback> response) {
                dismissProgressDialog();
                BraintreeCheckoutCallback resp = response.body();
                if (resp != null) {
                    if (resp.success == 1) {
                        GeneralUtils.addTransaction(PayPalActivity.this, plan.getPlanId(), resp.paypalPaymentId, planGateway);
                    } else {
                        GeneralUtils.showWarningToast(PayPalActivity.this, resp.message);
                    }
                } else {
                    GeneralUtils.showSomethingWrong(PayPalActivity.this);
                }
            }

            @Override
            public void onFailure(@NonNull Call<BraintreeCheckoutCallback> call, @NonNull Throwable t) {
                if (!call.isCanceled()) {
                    dismissProgressDialog();
                    GeneralUtils.showSomethingWrong(PayPalActivity.this);
                }
            }
        });
    }
}
