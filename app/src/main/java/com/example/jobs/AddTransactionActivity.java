package com.example.jobs;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.callback.CommonCallback;
import com.example.jobs.databinding.ActivityAddTransactionBinding;
import com.example.rest.ApiInterface;
import com.example.rest.RestAdapter;
import com.example.util.API;
import com.example.util.GeneralUtils;
import com.example.util.IsRTL;
import com.example.util.NetworkUtils;
import com.example.util.StatusBarUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddTransactionActivity extends AppCompatActivity {
    ActivityAddTransactionBinding viewBinding;
    MyApplication myApplication;
    String planId, paymentId, paymentGateway;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityAddTransactionBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        StatusBarUtil.setFullScreen(this, viewBinding.getRoot());
        IsRTL.ifSupported(this);
        myApplication = MyApplication.getInstance();
        Intent intent = getIntent();
        planId = intent.getStringExtra("planId");
        paymentId = intent.getStringExtra("paymentId");
        paymentGateway = intent.getStringExtra("paymentGateway");

        onRequest();
    }

    private void onRequest() {
        if (NetworkUtils.isConnected(this)) {
            addTransaction();
        } else {
            showErrorState();
        }
    }

    private void addTransaction() {
        showProgress(true);
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("user_id", myApplication.getLoginInfo().getUserId());
        jsObj.addProperty("plan_id", planId);
        jsObj.addProperty("payment_id", paymentId);
        jsObj.addProperty("payment_gateway", paymentGateway);
        ApiInterface apiInterface = RestAdapter.createAPI();
        Call<CommonCallback> callback = apiInterface.addTransaction(API.toBase64(jsObj.toString()));
        callback.enqueue(new Callback<CommonCallback>() {
            @Override
            public void onResponse(@NotNull Call<CommonCallback> call, @NotNull Response<CommonCallback> response) {
                showProgress(false);
                CommonCallback resp = response.body();
                if (resp != null) {
                    viewBinding.incSuccess.getRoot().setVisibility(View.VISIBLE);
                    viewBinding.incSuccess.btnHome.setOnClickListener(view -> {
                        ActivityCompat.finishAffinity(AddTransactionActivity.this);
                        Intent intentDashboard = new Intent(AddTransactionActivity.this, myApplication.isProvider() ? ProviderMainActivity.class : MainActivity.class);
                        intentDashboard.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intentDashboard);
                    });
                } else {
                    showErrorState();
                }
            }

            @Override
            public void onFailure(@NotNull Call<CommonCallback> call, @NotNull Throwable t) {
                if (!call.isCanceled()) {
                    showProgress(false);
                    showErrorState();
                }
            }
        });
    }

    private void showProgress(boolean show) {
        if (show) {
            viewBinding.progressBar.setVisibility(View.VISIBLE);
        } else {
            viewBinding.progressBar.setVisibility(View.GONE);
        }
    }

    private void showErrorState() {
        viewBinding.progressBar.setVisibility(View.GONE);
        viewBinding.incState.errorState.setVisibility(View.VISIBLE);
        GeneralUtils.changeStateInfo(this, viewBinding.incState.ivState, viewBinding.incState.tvError, viewBinding.incState.tvErrorMsg);
        viewBinding.incState.btnTryAgain.setOnClickListener(view -> {
            viewBinding.incState.errorState.setVisibility(View.GONE);
            onRequest();
        });
    }

}
