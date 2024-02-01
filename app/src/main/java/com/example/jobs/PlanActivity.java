package com.example.jobs;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.adapter.PlanAdapter;
import com.example.callback.PlanListCallback;
import com.example.jobs.databinding.ActivityPlanBinding;
import com.example.model.Plan;
import com.example.rest.ApiInterface;
import com.example.rest.RestAdapter;
import com.example.util.API;
import com.example.util.AppUtil;
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

public class PlanActivity extends AppCompatActivity {

    ActivityPlanBinding viewBinding;
    ArrayList<Plan> listPlan;
    PlanAdapter mAdapter;
    MyApplication myApplication;
    int selectedPlan = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityPlanBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        StatusBarUtil.setFullScreen(this, viewBinding.getRoot());
        IsRTL.ifSupported(this);
        myApplication = MyApplication.getInstance();
        listPlan = new ArrayList<>();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        viewBinding.recyclerView.setLayoutManager(layoutManager);
        viewBinding.recyclerView.addItemDecoration(GeneralUtils.listItemDecoration(this, R.dimen.item_space));

        onRequest();

        viewBinding.ivClose.setOnClickListener(view -> finish());
    }

    private void onRequest() {
        if (NetworkUtils.isConnected(PlanActivity.this)) {
            getPlanList();
        } else {
            showErrorState(State.STATE_NO_INTERNET);
        }
    }

    private void getPlanList() {
        showProgress(true);
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("plan_type", myApplication.isProvider() ? AppUtil.PLAN_TYPE_PROVIDER : AppUtil.PLAN_TYPE_SEEKER);
        ApiInterface apiInterface = RestAdapter.createAPI();
        Call<PlanListCallback> callback = apiInterface.getPlanList(API.toBase64(jsObj.toString()));
        callback.enqueue(new Callback<PlanListCallback>() {
            @Override
            public void onResponse(@NonNull Call<PlanListCallback> call, @NonNull Response<PlanListCallback> response) {
                PlanListCallback resp = response.body();
                showProgress(false);
                if (resp != null) {
                    if (resp.planList.isEmpty()) {
                        showErrorState(State.STATE_EMPTY);
                    } else {
                        listPlan.addAll(resp.planList);
                        setDataToView();
                    }
                } else {
                    showErrorState(State.STATE_ERROR_IN_API);
                }
            }

            @Override
            public void onFailure(@NonNull Call<PlanListCallback> call, @NonNull Throwable t) {
                if (!call.isCanceled())
                    showErrorState(State.STATE_ERROR_IN_API);
            }
        });

    }

    private void setDataToView() {
        mAdapter = new PlanAdapter(this, listPlan);
        viewBinding.recyclerView.setAdapter(mAdapter);
        mAdapter.select(selectedPlan);
        mAdapter.setOnItemClickListener((item, position) -> {
            selectedPlan = position;
            mAdapter.select(position);
        });
        viewBinding.btnPlan.setOnClickListener(view -> {
            Plan plan = listPlan.get(selectedPlan);
            String isFreePlan = plan.getPlanPrice();
            if (isFreePlan.equals("0.00")) {
                GeneralUtils.addTransaction(PlanActivity.this, plan.getPlanId(), "-", "N/A");
            } else {
                Intent intent = new Intent(PlanActivity.this, PaymentGatewayActivity.class);
                intent.putExtra("planInfo", plan);
                startActivity(intent);
            }
        });

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
