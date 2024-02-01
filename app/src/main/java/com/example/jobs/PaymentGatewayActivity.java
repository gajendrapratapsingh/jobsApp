package com.example.jobs;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.adapter.GatewayAdapter;
import com.example.callback.GatewayListCallback;
import com.example.jobs.databinding.ActivityPaymentGatewayBinding;
import com.example.model.Gateway;
import com.example.model.Plan;
import com.example.rest.ApiInterface;
import com.example.rest.RestAdapter;
import com.example.util.API;
import com.example.util.GeneralUtils;
import com.example.util.IsRTL;
import com.example.util.NetworkUtils;
import com.example.util.State;
import com.example.util.StatusBarUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentGatewayActivity extends AppCompatActivity {

    ActivityPaymentGatewayBinding viewBinding;
    ArrayList<Gateway> listGateway;
    GatewayAdapter mAdapter;
    int selectedGateway = -1;
    Plan plan;
    MyApplication myApplication;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityPaymentGatewayBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        StatusBarUtil.setFullScreen(this, viewBinding.getRoot());
        IsRTL.ifSupported(this);
        Intent intent = getIntent();
        plan = intent.getParcelableExtra("planInfo");
        listGateway = new ArrayList<>();
        myApplication = MyApplication.getInstance();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        viewBinding.recyclerView.setLayoutManager(layoutManager);
        viewBinding.recyclerView.addItemDecoration(GeneralUtils.listItemDecoration(this, R.dimen.item_space));

        onRequest();

        viewBinding.ivClose.setOnClickListener(view -> finish());
        setPlanInfo();
    }

    private void setPlanInfo() {
        viewBinding.llPlan.tvPlanName.setText(plan.getPlanName());
        viewBinding.llPlan.tvNoApplied.setText(getString(myApplication.isProvider()
                ? R.string.plan_num_job_add : R.string.plan_num_job, plan.getPlanJobLimit()));
        viewBinding.llPlan.tvAmount.setText(getString(R.string.plan_price, plan.getPlanPrice()));
        viewBinding.llPlan.tvPlanDuration.setText(getString(R.string.plan_duration, plan.getPlanDuration()));
        viewBinding.llPlan.tvCurrency.setText(plan.getPlanCurrencyCode());
    }

    private void onRequest() {
        if (NetworkUtils.isConnected(PaymentGatewayActivity.this)) {
            getGatewayList();
        } else {
            showErrorState(State.STATE_NO_INTERNET);
        }
    }

    private void getGatewayList() {
        showProgress(true);
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        ApiInterface apiInterface = RestAdapter.createAPI();
        Call<GatewayListCallback> callback = apiInterface.getPaymentGatewayList(API.toBase64(jsObj.toString()));
        callback.enqueue(new Callback<GatewayListCallback>() {
            @Override
            public void onResponse(@NonNull Call<GatewayListCallback> call, @NonNull Response<GatewayListCallback> response) {
                GatewayListCallback resp = response.body();
                showProgress(false);
                if (resp != null) {
                    if (resp.gatewayList.isEmpty()) {
                        showErrorState(State.STATE_NO_GATEWAY);
                    } else {
                        listGateway.addAll(resp.gatewayList);
                        setDataToView();
                    }
                } else {
                    showErrorState(State.STATE_ERROR_IN_API);
                }
            }

            @Override
            public void onFailure(@NonNull Call<GatewayListCallback> call, @NonNull Throwable t) {
                if (!call.isCanceled())
                    showErrorState(State.STATE_ERROR_IN_API);
            }
        });

    }

    private void setDataToView() {
        mAdapter = new GatewayAdapter(this, listGateway);
        viewBinding.recyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener((item, position) -> {
            selectedGateway = position;
            mAdapter.select(position);
        });
        viewBinding.btnPay.setOnClickListener(view -> {
            if (selectedGateway == -1) {
                GeneralUtils.showWarningToast(PaymentGatewayActivity.this, getString(R.string.select_gateway));
            } else {
                Gateway gateway = listGateway.get(selectedGateway);
                Intent intent = new Intent();
                intent.putExtra("planInfo", plan);
                switch (gateway.getGatewayId()) {
                    case 1:
                        intent.setClass(this, PayPalActivity.class);
                        startActivity(intent);
                        break;
                    case 2:
                        if (isGatewayIdNull(gateway.getGatewayInfo(), "stripe_publishable_key"))
                            break;
                        intent.putExtra("stripePublisherKey", gatewayIds(gateway.getGatewayInfo(), "stripe_publishable_key"));
                        intent.setClass(this, StripeActivity.class);
                        startActivity(intent);
                        break;
                    case 3:
                        if (isGatewayIdNull(gateway.getGatewayInfo(), "razorpay_key"))
                            break;
                        intent.putExtra("razorPayKey", gatewayIds(gateway.getGatewayInfo(), "razorpay_key"));
                        intent.setClass(this, RazorPayActivity.class);
                        startActivity(intent);
                        break;
                    case 4:
                        if (isGatewayIdNull(gateway.getGatewayInfo(), "paystack_public_key"))
                            break;
                        intent.putExtra("payStackPublicKey", gatewayIds(gateway.getGatewayInfo(), "paystack_public_key"));
                        intent.setClass(this, PayStackActivity.class);
                        startActivity(intent);
                        break;
                    case 8:
                        if (isGatewayIdNull(gateway.getGatewayInfo(), "flutterwave_public_key") || isGatewayIdNull(gateway.getGatewayInfo(), "flutterwave_encryption_key"))
                            break;
                        intent.putExtra("fwPublicKey", gatewayIds(gateway.getGatewayInfo(), "flutterwave_public_key"));
                        intent.putExtra("fwEncryptionKey", gatewayIds(gateway.getGatewayInfo(), "flutterwave_encryption_key"));
                        intent.setClass(this, FlutterWaveActivity.class);
                        startActivity(intent);
                        break;
                    case 6:
                        if (isGatewayIdNull(gateway.getGatewayInfo(), "mode") || isGatewayIdNull(gateway.getGatewayInfo(), "payu_key"))
                            break;
                        intent.putExtra("isSandbox", gatewayIds(gateway.getGatewayInfo(), "mode").equals("sandbox"));
                        intent.putExtra("payUMoneyMerchantKey", gatewayIds(gateway.getGatewayInfo(), "payu_key"));
                        intent.setClass(this, PayUProActivity.class);
                        startActivity(intent);
                        break;
                }
            }
        });
    }

    private boolean isGatewayIdNull(JsonObject jsonObject, String tagValue) {
        boolean status = jsonObject.get(tagValue) == null || jsonObject.get(tagValue).isJsonNull();
        if (status) {
            GeneralUtils.showWarningToast(this, getString(R.string.gateway_id_null));
        }
        return status;
    }

    private String gatewayIds(JsonObject jsonObject, String tagValue) {
        return jsonObject.get(tagValue).getAsString();
    }

    private void showProgress(boolean show) {
        if (show) {
            viewBinding.progressBar.setVisibility(View.VISIBLE);
            viewBinding.parent.setVisibility(View.GONE);
        } else {
            viewBinding.progressBar.setVisibility(View.GONE);
            viewBinding.parent.setVisibility(View.VISIBLE);
        }
    }

    private void showErrorState(int state) {
        viewBinding.progressBar.setVisibility(View.GONE);
        viewBinding.parent.setVisibility(View.GONE);
        viewBinding.incState.errorState.setVisibility(View.VISIBLE);
        GeneralUtils.changeStateInfo(this, state, viewBinding.incState.ivState, viewBinding.incState.tvError, viewBinding.incState.tvErrorMsg);
        viewBinding.incState.btnTryAgain.setOnClickListener(view -> {
            viewBinding.incState.errorState.setVisibility(View.GONE);
            onRequest();
        });
    }
}
