package com.example.jobs;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.applovin.sdk.AppLovinMediationProvider;
import com.applovin.sdk.AppLovinSdk;
import com.example.jobs.databinding.ActivitySplashBinding;
import com.example.jobs.databinding.LayoutInvalidLicenseSheetBinding;
import com.example.rest.ApiInterface;
import com.example.rest.RestAdapter;
import com.example.util.API;
import com.example.util.AppUtil;
import com.example.util.GeneralUtils;
import com.example.util.NetworkUtils;
import com.example.util.StatusBarUtil;
import com.facebook.ads.AudienceNetworkAds;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.StartAppSDK;
import com.wortise.ads.WortiseSdk;

import java.util.Collections;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    ActivitySplashBinding viewBinding;
    MyApplication myApplication;
    private boolean mIsBackButtonPressed;
    private static final int SPLASH_DURATION = 1000;
    boolean isLoginDisable = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        StatusBarUtil.setFullScreenWithLightStatusBars(this, viewBinding.getRoot(), false);
        myApplication = MyApplication.getInstance();
        onRequest();
    }

    private void onRequest() {
        if (NetworkUtils.isConnected(this)) {
            getAppDetail();
        } else {
            showErrorState();
        }
    }

    private void getAppDetail() {
        showProgress(true);
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("user_id", myApplication.getLoginInfo().getUserId());
        ApiInterface apiInterface = RestAdapter.createAPI();
        Call<JsonObject> callback = apiInterface.getAppDetail(API.toBase64(jsObj.toString()));
        callback.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                //    showProgress(false);
                JsonObject resp = response.body();
                if (resp != null) {
                    try {
                        isLoginDisable = resp.get("user_status").getAsBoolean();
                        JsonObject joRoot = resp.getAsJsonObject("JOBS_APP");
                        AppUtil.currencyCode = joRoot.get("currency_symbol").getAsString();
                        AppUtil.isAppUpdate = joRoot.get("app_update_hide_show").getAsBoolean();
                        AppUtil.isAppUpdateCancel = joRoot.get("app_update_cancel_option").getAsBoolean();
                        AppUtil.appUpdateVersion = joRoot.get("app_update_version_code").getAsInt();
                        AppUtil.appUpdateUrl = joRoot.get("app_update_link").getAsString();
                        AppUtil.appUpdateDesc = joRoot.get("app_update_desc").getAsString();

                        JsonArray jaAds = joRoot.getAsJsonArray("ads_list");
                        if (!jaAds.isEmpty()) {
                            JsonObject joAd = jaAds.get(0).getAsJsonObject();
                            AppUtil.adNetworkType = joAd.get("ad_id").getAsString();
                            JsonObject joAdInfo = joAd.getAsJsonObject("ads_info");
                            AppUtil.appIdOrPublisherId = joAdInfo.get("publisher_id").getAsString();

                            AppUtil.isBanner = joAdInfo.get("banner_on_off").getAsString().equals("1");
                            AppUtil.bannerId = joAdInfo.get("banner_id").getAsString();

                            AppUtil.isInterstitial = joAdInfo.get("interstitial_on_off").getAsString().equals("1");
                            AppUtil.interstitialId = joAdInfo.get("interstitial_id").getAsString();
                            AppUtil.interstitialAdCount = joAdInfo.get("interstitial_clicks").getAsInt();

                            AppUtil.isNative = joAdInfo.get("native_on_off").getAsString().equals("1");
                            AppUtil.nativeId = joAdInfo.get("native_id").getAsString();
                            AppUtil.nativeAdCount = joAdInfo.get("native_position").getAsInt();
                        }
                        initializeAds();
                        String packageName = joRoot.get("app_package_name").getAsString();
                        if (packageName.isEmpty() || !packageName.equals(getPackageName())) {
                            invalidLicenseSheet();
                        } else {
                            splashScreen();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        showErrorState();
                    }
                } else {
                    showErrorState();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (!call.isCanceled()) {
                    showProgress(false);
                    showErrorState();
                }
            }
        });
    }

    private void showProgress(boolean show) {
        viewBinding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showErrorState() {
        viewBinding.progressBar.setVisibility(View.GONE);
        viewBinding.errorState.setVisibility(View.VISIBLE);
        viewBinding.tvError.setText(NetworkUtils.isConnected(this) ? getString(R.string.no_error) : getString(R.string.no_internet));
        viewBinding.btnRetry.setOnClickListener(view -> {
            viewBinding.errorState.setVisibility(View.INVISIBLE);
            onRequest();
        });
    }

    private void splashScreen() {
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            if (!mIsBackButtonPressed) {
                boolean isIntroOn = Boolean.parseBoolean(getResources().getString(R.string.is_introduction_see));
                if (isIntroOn && !myApplication.isIntroSeen()) {
                    goNext(IntroductionActivity.class);
                } else if (isLoginDisable && myApplication.isLogin()) {
                    goNext(myApplication.isProvider() ? ProviderMainActivity.class : MainActivity.class);
                } else if (!isLoginDisable && myApplication.isLogin()) {
                    myApplication.setLogin(false);
                    GeneralUtils.showWarningToast(SplashActivity.this, getString(R.string.user_disable));
                    goNext(LoginActivity.class);
                } else {
                    goNext(MainActivity.class);
                }
            }
        }, SPLASH_DURATION);
    }

    private void goNext(Class<?> cl) {
        Intent intent = new Intent(getApplicationContext(), cl);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        // set the flag to true so the next activity won't start up
        mIsBackButtonPressed = true;
        super.onBackPressed();
    }

    private void invalidLicenseSheet() {
        BottomSheetDialog sheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialog);
        LayoutInvalidLicenseSheetBinding sheetBinding = LayoutInvalidLicenseSheetBinding.inflate(getLayoutInflater());
        sheetDialog.setContentView(sheetBinding.getRoot());
        boolean isRTL = Boolean.parseBoolean(getString(R.string.isRTL));
        if (isRTL) {
            sheetDialog.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
        sheetDialog.setCancelable(false);
        sheetDialog.setCanceledOnTouchOutside(false);
        sheetBinding.btnOkay.setOnClickListener(view -> finish());
        sheetDialog.show();
    }

    private void initializeAds() {
        if (AppUtil.isBanner || AppUtil.isInterstitial) {
            switch (AppUtil.adNetworkType) {
                case AppUtil.admobAd:
                    MobileAds.initialize(this, initializationStatus -> {

                    });
                    break;
                case AppUtil.startAppAd:
                    StartAppSDK.init(this, AppUtil.appIdOrPublisherId, false);
                    StartAppAd.disableSplash();
                    break;
                case AppUtil.facebookAd:
                    AudienceNetworkAds.initialize(this);
                    break;
                case AppUtil.appLovinMaxAd:
                    AppLovinSdk.getInstance(this).setMediationProvider(AppLovinMediationProvider.MAX);
                    AppLovinSdk.getInstance(this).getSettings().setTestDeviceAdvertisingIds(Collections.singletonList("93df459b-929c-448c-9202-064c1f4c2545"));
                    AppLovinSdk.getInstance(this).initializeSdk(config -> {
                    });
                    break;
                case AppUtil.wortiseAd:
                    WortiseSdk.initialize(this, AppUtil.appIdOrPublisherId);
                    break;
            }
        }
    }
}
