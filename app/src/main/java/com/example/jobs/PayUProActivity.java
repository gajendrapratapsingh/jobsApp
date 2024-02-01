package com.example.jobs;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.example.callback.PaymentTokenCallback;
import com.example.model.Plan;
import com.example.rest.ApiInterface;
import com.example.rest.RestAdapter;
import com.example.util.API;
import com.example.util.GeneralUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.payu.base.models.ErrorResponse;
import com.payu.base.models.PayUPaymentParams;
import com.payu.checkoutpro.PayUCheckoutPro;
import com.payu.checkoutpro.models.PayUCheckoutProConfig;
import com.payu.checkoutpro.utils.PayUCheckoutProConstants;
import com.payu.ui.model.listeners.PayUCheckoutProListener;
import com.payu.ui.model.listeners.PayUHashGenerationListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PayUProActivity extends PaymentBaseActivity {
    Plan plan;
    String planGateway = "PayUMoney", payUMoneyMerchantKey;
    boolean isSandbox = false;
    public static final String SURL = BuildConfig.SERVER_URL + "app_payu_success";
    public static final String FURL = BuildConfig.SERVER_URL + "app_payu_failed";
    MyApplication myApplication;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        plan = intent.getParcelableExtra("planInfo");
        payUMoneyMerchantKey = intent.getStringExtra("payUMoneyMerchantKey");
        isSandbox = intent.getBooleanExtra("isSandbox", false);
        myApplication = MyApplication.getInstance();

        String payString = getString(R.string.pay_via, plan.getPlanPrice(), plan.planCurrencyCode, planGateway);
        viewBinding.btnPay.setText(payString);
        viewBinding.btnPay.setOnClickListener(view -> onRequest());
        viewBinding.btnPay.setVisibility(View.VISIBLE);
        showProgress(false);

        onRequest();
    }

    private void onRequest() {
        if (myApplication.getLoginInfo().getUserPhone().isEmpty()) {
            showErrorPhone();
        } else {
            startPayment();
        }
    }

    @SuppressWarnings("unchecked")
    public void startPayment() {
        PayUPaymentParams.Builder builder = new PayUPaymentParams.Builder();
        builder.setAmount(plan.getPlanPrice())
                .setIsProduction(!isSandbox)
                .setProductInfo(plan.getPlanName())
                .setKey(payUMoneyMerchantKey)
                .setTransactionId(System.currentTimeMillis() + "")
                .setFirstName(myApplication.getLoginInfo().getUserName())
                .setEmail(myApplication.getLoginInfo().getUserEmail())
                .setPhone(myApplication.getLoginInfo().getUserPhone())
                .setUserCredential(myApplication.getLoginInfo().getUserEmail())
                .setSurl(SURL)
                .setFurl(FURL);
        PayUPaymentParams payUPaymentParams = builder.build();
        PayUCheckoutProConfig payUCheckoutProConfig = new PayUCheckoutProConfig();
        payUCheckoutProConfig.setMerchantName(getString(R.string.payment_company_name));
        payUCheckoutProConfig.setMerchantLogo(R.mipmap.ic_launcher);

        PayUCheckoutPro.open(PayUProActivity.this, payUPaymentParams, payUCheckoutProConfig, new PayUCheckoutProListener() {
            @Override
            public void onPaymentSuccess(@NonNull Object response) {
                HashMap<String, Object> result = (HashMap<String, Object>) response;
                String payuResponse = (String) result.get(PayUCheckoutProConstants.CP_PAYU_RESPONSE);
                //  String merchantResponse = (String) result.get(PayUCheckoutProConstants.CP_MERCHANT_RESPONSE);
                try {
                    assert payuResponse != null;
                    JSONObject mainJson = new JSONObject(payuResponse);
                    String paymentId = mainJson.getString("txnid");
                    GeneralUtils.addTransaction(PayUProActivity.this, plan.getPlanId(), paymentId, planGateway);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onPaymentFailure(@NonNull Object response) {
                HashMap<String, Object> result = (HashMap<String, Object>) response;
                String payuResponse = (String) result.get(PayUCheckoutProConstants.CP_PAYU_RESPONSE);
                //    String merchantResponse = (String) result.get(PayUCheckoutProConstants.CP_MERCHANT_RESPONSE);
                try {
                    assert payuResponse != null;
                    JSONObject mainJson = new JSONObject(payuResponse);
                    String errorMessage = mainJson.getString("Error_Message");
                    showError(planGateway, errorMessage);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onPaymentCancel(boolean b) {

            }

            @Override
            public void onError(@NonNull ErrorResponse errorResponse) {
                String errorMessage = errorResponse.getErrorMessage();
                Log.e("onError", "Yes");
                showError(planGateway, errorMessage);
            }

            @Override
            public void generateHash(@NonNull HashMap<String, String> hashMap, @NonNull PayUHashGenerationListener payUHashGenerationListener) {
                String hashName = hashMap.get(PayUCheckoutProConstants.CP_HASH_NAME);
                String hashData = hashMap.get(PayUCheckoutProConstants.CP_HASH_STRING);
                if (!TextUtils.isEmpty(hashName) && !TextUtils.isEmpty(hashData)) {
                    //Do not generate hash from local, it needs to be calculated from server side only. Here, hashString contains hash created from your server side.
                    getHash(hashName, hashData, payUHashGenerationListener);
                }
            }

            @Override
            public void setWebViewProperties(@Nullable WebView webView, @Nullable Object o) {

            }
        });
    }

    private void getHash(String hashName, String hashData, PayUHashGenerationListener payUHashGenerationListener) {
        showProgressDialog();

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("hashdata", hashData);

        ApiInterface apiInterface = RestAdapter.createAPI();
        Call<PaymentTokenCallback> callback = apiInterface.getPayUHash(API.toBase64(jsObj.toString()));
        callback.enqueue(new Callback<PaymentTokenCallback>() {
            @Override
            public void onResponse(@NonNull Call<PaymentTokenCallback> call, @NonNull Response<PaymentTokenCallback> response) {
                dismissProgressDialog();
                PaymentTokenCallback resp = response.body();
                if (resp != null) {
                    if (resp.success == 1) {
                        HashMap<String, String> dataMap = new HashMap<>();
                        dataMap.put(hashName, resp.payUHash);
                        payUHashGenerationListener.onHashGenerated(dataMap);
                    } else {
                        GeneralUtils.showWarningToast(PayUProActivity.this, resp.message);
                    }
                } else {
                    GeneralUtils.showSomethingWrong(PayUProActivity.this);
                }
            }

            @Override
            public void onFailure(@NonNull Call<PaymentTokenCallback> call, @NonNull Throwable t) {
                if (!call.isCanceled()) {
                    dismissProgressDialog();
                    GeneralUtils.showSomethingWrong(PayUProActivity.this);
                }
            }
        });
    }

    private void showErrorPhone() {
        new AlertDialog.Builder(PayUProActivity.this)
                .setTitle(planGateway)
                .setMessage(getString(R.string.payment_need_phone))
                .setIcon(R.mipmap.ic_launcher)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    Intent intent = new Intent(PayUProActivity.this, EditProfileActivity.class);
                    intent.putExtra("isFromPayment", true);
                    activityResultLauncher.launch(intent);
                })
                .setNegativeButton(android.R.string.no, (dialog, which) -> {
                    // do nothing
                })
                .show();
    }

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == 1187) {
            onRequest();
        }
    });
}
