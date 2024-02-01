package com.example.jobs;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.jobs.databinding.ActivityAboutUsBinding;
import com.example.rest.ApiInterface;
import com.example.rest.RestAdapter;
import com.example.util.API;
import com.example.util.BannerAds;
import com.example.util.GeneralUtils;
import com.example.util.GlideApp;
import com.example.util.IsRTL;
import com.example.util.NetworkUtils;
import com.example.util.StatusBarUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AboutUsActivity extends AppCompatActivity {

    ActivityAboutUsBinding viewBinding;
    String appName, appLogo, appVersion, appCompanyName, appEmail,
            appWebsite, appContactNo, aboutUs, appPageTitle, appFacebook, appTwitter, appInstagram, appYoutube;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityAboutUsBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        StatusBarUtil.setFullScreen(this, viewBinding.getRoot());
        IsRTL.ifSupported(this);
        BannerAds.showBannerAds(this, viewBinding.adView);
        viewBinding.webView.setBackgroundColor(Color.TRANSPARENT);

        viewBinding.toolbar.includeImage.getRoot().setVisibility(View.GONE);
        viewBinding.toolbar.tvName.setText(getString(R.string.lbl_about_us));
        viewBinding.toolbar.fabBack.setOnClickListener(view -> onBackPressed());
        onRequest();
    }

    private void onRequest() {
        if (NetworkUtils.isConnected(AboutUsActivity.this)) {
            getAboutUs();
        } else {
            showErrorState();
        }
    }

    private void getAboutUs() {
        showProgress(true);
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        ApiInterface apiInterface = RestAdapter.createAPI();
        Call<JsonObject> callback = apiInterface.getAppDetail(API.toBase64(jsObj.toString()));
        callback.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                showProgress(false);
                JsonObject resp = response.body();
                if (resp != null) {
                    try {
                        JsonObject joRoot = resp.getAsJsonObject("JOBS_APP");
                        appName = joRoot.get("app_name").getAsString();
                        appLogo = joRoot.get("app_logo").getAsString();
                        appVersion = joRoot.get("app_version").getAsString();
                        appCompanyName = joRoot.get("app_company").getAsString();
                        appEmail = joRoot.get("app_email").getAsString();
                        appWebsite = joRoot.get("app_website").getAsString();
                        appContactNo = joRoot.get("app_contact").getAsString();
                        appFacebook = joRoot.get("facebook_link").getAsString();
                        appTwitter = joRoot.get("twitter_link").getAsString();
                        appInstagram = joRoot.get("instagram_link").getAsString();
                        appYoutube = joRoot.get("youtube_link").getAsString();
                        JsonArray jaPages = joRoot.getAsJsonArray("page_list");
                        for (int i = 0; i < jaPages.size(); i++) {
                            JsonObject joPage = jaPages.get(i).getAsJsonObject();
                            if (joPage.get("page_id").getAsString().equals("1")) {
                                appPageTitle = joPage.get("page_title").getAsString();
                                aboutUs = joPage.get("page_content").getAsString();
                            }
                        }
                        setDataToView();
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

    private void setDataToView() {
        viewBinding.tvAppName.setText(appName);
        viewBinding.tvAppVersion.setText(appVersion);
        viewBinding.tvCompany.setText(appCompanyName);
        viewBinding.tvEmail.setText(appEmail);
        viewBinding.tvWebsite.setText(appWebsite);
        viewBinding.tvContact.setText(appContactNo);

        GlideApp.with(AboutUsActivity.this).load(appLogo).into(viewBinding.ivApp);
        boolean isRTL = Boolean.parseBoolean(getString(R.string.isRTL));
        viewBinding.webView.loadDataWithBaseURL(null, GeneralUtils.convertHtml(aboutUs, isRTL), "text/html", "utf-8", null);
        viewBinding.toolbar.tvName.setText(appPageTitle);
        viewBinding.ivFacebook.setOnClickListener(view -> openSocialLink(appFacebook));
        viewBinding.ivTwitter.setOnClickListener(view -> openSocialLink(appTwitter));
        viewBinding.ivInstagram.setOnClickListener(view -> openSocialLink(appInstagram));
        viewBinding.ivYoutube.setOnClickListener(view -> openSocialLink(appYoutube));
    }

    private void openSocialLink(String link) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
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

    private void showErrorState() {
        viewBinding.progressBar.setVisibility(View.GONE);
        viewBinding.parent.setVisibility(View.GONE);
        viewBinding.incState.errorState.setVisibility(View.VISIBLE);
        GeneralUtils.changeStateInfo(this, viewBinding.incState.ivState, viewBinding.incState.tvError, viewBinding.incState.tvErrorMsg);
        viewBinding.incState.btnTryAgain.setOnClickListener(view -> {
            viewBinding.incState.errorState.setVisibility(View.GONE);
            onRequest();
        });
    }
}
