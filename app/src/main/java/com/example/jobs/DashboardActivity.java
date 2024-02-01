package com.example.jobs;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.callback.ProfileCallback;
import com.example.jobs.databinding.ActivityDashboardBinding;
import com.example.rest.ApiInterface;
import com.example.rest.RestAdapter;
import com.example.util.API;
import com.example.util.BannerAds;
import com.example.util.Events;
import com.example.util.GeneralUtils;
import com.example.util.GlideApp;
import com.example.util.GlobalBus;
import com.example.util.IsRTL;
import com.example.util.NetworkUtils;
import com.example.util.StatusBarUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.greenrobot.eventbus.Subscribe;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends AppCompatActivity {
    ActivityDashboardBinding viewBinding;
    MyApplication myApplication;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        StatusBarUtil.setFullScreen(this, viewBinding.getRoot());
        IsRTL.ifSupported(this);
        BannerAds.showBannerAds(this, viewBinding.adView);
        GlobalBus.getBus().register(this);
        myApplication = MyApplication.getInstance();

        initHeader();
        viewBinding.toolbar.tvName.setText(getString(R.string.dashboard));
        viewBinding.toolbar.fabBack.setOnClickListener(view -> onBackPressed());
        onRequest();
    }

    private void onRequest() {
        if (NetworkUtils.isConnected(this)) {
            getDashboard();
        } else {
            showErrorState();
        }
    }

    private void getDashboard() {
        showProgress(true);
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("user_id", myApplication.getLoginInfo().getUserId());
        ApiInterface apiInterface = RestAdapter.createAPI();
        Call<ProfileCallback> callback = apiInterface.userProfile(API.toBase64(jsObj.toString()));
        callback.enqueue(new Callback<ProfileCallback>() {
            @Override
            public void onResponse(@NonNull Call<ProfileCallback> call, @NonNull Response<ProfileCallback> response) {
                showProgress(false);
                ProfileCallback resp = response.body();
                if (resp != null && resp.success == 1) {
                    setDataToView(resp);
                } else {
                    showErrorState();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProfileCallback> call, @NonNull Throwable t) {
                if (!call.isCanceled()) {
                    showProgress(false);
                    showErrorState();
                }
            }
        });
    }

    private void setDataToView(ProfileCallback resp) {
        viewBinding.tvUserName.setText(resp.profile.getUserName());
        viewBinding.tvEmail.setText(resp.profile.getUserEmail());
        viewBinding.tvEmail.setSelected(true);
        GlideApp.with(this).load(resp.profile.getUserImage()).into(viewBinding.ivProfilePic);

        viewBinding.tvPlanName.setText(resp.currentPlan.isEmpty() ? getString(R.string.n_a) : resp.currentPlan);
        viewBinding.tvExpireOn.setText(getString(R.string.expire_on, resp.expiredDate.isEmpty() ? getString(R.string.n_a) : resp.expiredDate));
        viewBinding.tvLsDate.setText(resp.lsDate.isEmpty() ? getString(R.string.n_a) : resp.lsDate);
        viewBinding.tvLsPlan.setText(resp.lsPlanName.isEmpty() ? getString(R.string.n_a) : resp.lsPlanName);
        viewBinding.tvLsAmount.setText(resp.lsPlanAmount.isEmpty() ? getString(R.string.n_a) : resp.lsPlanAmount);

        viewBinding.btnEdit.setOnClickListener(view -> {
            Intent intent = new Intent(this, EditProfileActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        viewBinding.btnUpgrade.setOnClickListener(view -> {
            Intent intent = new Intent(this, PlanActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
    }

    private void showProgress(boolean show) {
        if (show) {
            viewBinding.progressBar.setVisibility(View.VISIBLE);
            viewBinding.rootView.setVisibility(View.GONE);
        } else {
            viewBinding.progressBar.setVisibility(View.GONE);
            viewBinding.rootView.setVisibility(View.VISIBLE);
        }
    }

    private void showErrorState() {
        viewBinding.progressBar.setVisibility(View.GONE);
        viewBinding.rootView.setVisibility(View.GONE);
        viewBinding.incState.errorState.setVisibility(View.VISIBLE);
        GeneralUtils.changeStateInfo(this, viewBinding.incState.ivState, viewBinding.incState.tvError, viewBinding.incState.tvErrorMsg);
        viewBinding.incState.btnTryAgain.setOnClickListener(view -> {
            viewBinding.incState.errorState.setVisibility(View.GONE);
            onRequest();
        });
    }

    @Subscribe
    public void onEvent(Events.ProfileUpdate profileUpdate) {
        initHeader();
    }

    private void initHeader() {
        if (myApplication.isLogin()) {
            GlideApp.with(DashboardActivity.this).load(myApplication.getLoginInfo().getUserImage()).placeholder(R.drawable.dummy_user).error(R.drawable.dummy_user).into(viewBinding.toolbar.includeImage.ivUserImage);
            GlideApp.with(DashboardActivity.this).load(myApplication.getLoginInfo().getUserImage()).placeholder(R.drawable.dummy_user).error(R.drawable.dummy_user).into(viewBinding.ivProfilePic);
            viewBinding.toolbar.includeImage.ivUserStatus.setImageResource(R.drawable.online);
            viewBinding.tvUserName.setText(myApplication.getLoginInfo().getUserName());
            viewBinding.tvEmail.setText(myApplication.getLoginInfo().getUserEmail());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        GlobalBus.getBus().unregister(this);
    }

}
