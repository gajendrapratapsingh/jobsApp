package com.example.jobs;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.callback.ProfileCallback;
import com.example.jobs.databinding.ActivityMainProviderBinding;
import com.example.jobs.databinding.LayoutLogoutSheetBinding;
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
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.tuyenmonkey.textdecorator.TextDecorator;

import org.greenrobot.eventbus.Subscribe;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProviderMainActivity extends AppCompatActivity {

    ActivityMainProviderBinding viewBinding;
    MyApplication myApplication;
    boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityMainProviderBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        StatusBarUtil.setFullScreen(this, viewBinding.getRoot());
        IsRTL.ifSupported(this);
        GlobalBus.getBus().register(this);
        myApplication = MyApplication.getInstance();
        BannerAds.showBannerAds(this, viewBinding.adView);
        initHeader();

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
        setProviderMessage();
        viewBinding.tvName.setText(resp.profile.getUserName());
        viewBinding.tvEmail.setText(resp.profile.getUserEmail());
        viewBinding.tvEmail.setSelected(true);
        GlideApp.with(this).load(resp.profile.getUserImage()).into(viewBinding.ivProfilePic);

        viewBinding.tvPlanName.setText(resp.currentPlan.isEmpty() ? getString(R.string.n_a) : resp.currentPlan);
        viewBinding.tvExpireOn.setText(getString(R.string.expire_on, resp.expiredDate.isEmpty() ? getString(R.string.n_a) : resp.expiredDate));
        viewBinding.tvLsDate.setText(resp.lsDate.isEmpty() ? getString(R.string.n_a) : resp.lsDate);
        viewBinding.tvLsPlan.setText(resp.lsPlanName.isEmpty() ? getString(R.string.n_a) : resp.lsPlanName);
        viewBinding.tvLsAmount.setText(resp.lsPlanAmount.isEmpty() ? getString(R.string.n_a) : resp.lsPlanAmount);

        viewBinding.btnEdit.setOnClickListener(view -> logoutSheet());

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
            viewBinding.toolbar.tvUserName.setText(myApplication.getLoginInfo().getUserName());
            GlideApp.with(ProviderMainActivity.this).load(myApplication.getLoginInfo().getUserImage()).placeholder(R.drawable.dummy_user).error(R.drawable.dummy_user).into(viewBinding.toolbar.includeImage.ivUserImage);
            viewBinding.toolbar.includeImage.ivUserStatus.setImageResource(R.drawable.online);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        GlobalBus.getBus().unregister(this);
    }

    private void setProviderMessage() {
        TextDecorator
                .decorate(viewBinding.tvProvider, getString(R.string.job_provider_message, BuildConfig.SERVER_URL))
                .makeTextClickable((view, text) -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(text))), true, BuildConfig.SERVER_URL)
                .setTextColor(R.color.colorPrimary, BuildConfig.SERVER_URL)
                .build();
    }

    private void logoutSheet() {
        BottomSheetDialog sheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialog);
        LayoutLogoutSheetBinding sheetBinding = LayoutLogoutSheetBinding.inflate(getLayoutInflater());
        sheetDialog.setContentView(sheetBinding.getRoot());
        boolean isRTL = Boolean.parseBoolean(getString(R.string.isRTL));
        if (isRTL) {
            sheetDialog.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
        sheetBinding.btnLogoutCancel.setOnClickListener(view -> sheetDialog.dismiss());
        sheetBinding.btnLogout.setOnClickListener(view -> {
            myApplication.setLogin(false);
            Intent intentSignIn = new Intent(ProviderMainActivity.this, LoginActivity.class);
            intentSignIn.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intentSignIn);
            sheetDialog.dismiss();
            finish();
        });

        sheetDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, getString(R.string.back_key), Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
    }
}
