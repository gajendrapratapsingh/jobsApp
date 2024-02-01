package com.example.jobs;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.jobs.databinding.ActivityPageBinding;
import com.example.model.Page;
import com.example.rest.ApiInterface;
import com.example.rest.RestAdapter;
import com.example.util.API;
import com.example.util.BannerAds;
import com.example.util.GeneralUtils;
import com.example.util.IsRTL;
import com.example.util.NetworkUtils;
import com.example.util.StatusBarUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PageActivity extends AppCompatActivity {
    ActivityPageBinding viewBinding;
    Page page;
    String pageId, pageTitle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityPageBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        StatusBarUtil.setFullScreen(this, viewBinding.getRoot());
        IsRTL.ifSupported(this);
        BannerAds.showBannerAds(this, viewBinding.adView);
        viewBinding.webView.setBackgroundColor(Color.TRANSPARENT);
        Intent intent = getIntent();
        pageId = intent.getStringExtra("pageId");
        pageTitle = intent.getStringExtra("pageTitle");
        page = new Page();

        viewBinding.toolbar.includeImage.getRoot().setVisibility(View.GONE);
        viewBinding.toolbar.tvName.setText(pageTitle);
        viewBinding.toolbar.fabBack.setOnClickListener(view -> onBackPressed());
        onRequest();
    }

    private void onRequest() {
        if (NetworkUtils.isConnected(PageActivity.this)) {
            getPageDetails();
        } else {
            showErrorState();
        }
    }

    private void getPageDetails() {
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
                        JsonArray jaPages = joRoot.getAsJsonArray("page_list");
                        for (int i = 0; i < jaPages.size(); i++) {
                            JsonObject joPage = jaPages.get(i).getAsJsonObject();
                            if (joPage.get("page_id").getAsString().equals(pageId)) {
                                page.setPageId(joPage.get("page_id").getAsString());
                                page.setPageTitle(joPage.get("page_title").getAsString());
                                page.setPageContent(joPage.get("page_content").getAsString());
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
        boolean isRTL = Boolean.parseBoolean(getString(R.string.isRTL));
        viewBinding.webView.loadDataWithBaseURL(null, GeneralUtils.convertHtml(page.getPageContent(), isRTL), "text/html", "utf-8", null);
        viewBinding.toolbar.tvName.setText(page.getPageTitle());
    }

    private void showProgress(boolean show) {
        if (show) {
            viewBinding.progressBar.setVisibility(View.VISIBLE);
            viewBinding.webView.setVisibility(View.GONE);
        } else {
            viewBinding.progressBar.setVisibility(View.GONE);
            viewBinding.webView.setVisibility(View.VISIBLE);
        }
    }

    private void showErrorState() {
        viewBinding.progressBar.setVisibility(View.GONE);
        viewBinding.webView.setVisibility(View.GONE);
        viewBinding.incState.errorState.setVisibility(View.VISIBLE);
        GeneralUtils.changeStateInfo(this, viewBinding.incState.ivState, viewBinding.incState.tvError, viewBinding.incState.tvErrorMsg);
        viewBinding.incState.btnTryAgain.setOnClickListener(view -> {
            viewBinding.incState.errorState.setVisibility(View.GONE);
            onRequest();
        });
    }
}
