package com.example.jobs;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.adapter.RecommendJobAdapter;
import com.example.callback.JobListCallback;
import com.example.jobs.databinding.ActivityJobListBinding;
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
import com.example.util.ProfilePopUp;
import com.example.util.StatusBarUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.greenrobot.eventbus.Subscribe;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecommendJobActivity extends AppCompatActivity {

    ActivityJobListBinding viewBinding;
    RecommendJobAdapter mAdapter;
    MyApplication myApplication;
    private boolean isLoadMore = false;
    private int pageIndex = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityJobListBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        StatusBarUtil.setFullScreen(this, viewBinding.getRoot());
        IsRTL.ifSupported(this);
        BannerAds.showBannerAds(this, viewBinding.adView);
        GlobalBus.getBus().register(this);
        myApplication = MyApplication.getInstance();

        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        viewBinding.recyclerView.setLayoutManager(layoutManager);

        mAdapter = new RecommendJobAdapter(this);
        viewBinding.recyclerView.setAdapter(mAdapter);
        viewBinding.recyclerView.addItemDecoration(GeneralUtils.gridItemDecoration(this, R.dimen.item_space));

        onRequest();

        mAdapter.setOnLoadMoreListener(() -> {
            isLoadMore = true;
            pageIndex = pageIndex + 1;
            onRequest();
        });

        mAdapter.setOnTryAgainListener(() -> {
            pageIndex = 1;
            onRequest();
        });

        mAdapter.setOnItemClickListener((item, position) -> {
            Intent intentDetail = new Intent(this, JobDetailActivity.class);
            intentDetail.putExtra("jobId", item.getJobId());
            startActivity(intentDetail);
        });

        initHeader();
        viewBinding.toolbar.tvName.setText(getString(R.string.recommended_job));
        viewBinding.toolbar.fabBack.setOnClickListener(view -> onBackPressed());
    }

    private void onRequest() {
        if (NetworkUtils.isConnected(RecommendJobActivity.this)) {
            if (!isLoadMore) {
                mAdapter.onLoading();
            }
            getJobList();
        } else {
            showErrorState();
        }
    }

    private void getJobList() {
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("user_id", myApplication.getLoginInfo().getUserId());
        ApiInterface apiInterface = RestAdapter.createAPI();
        Call<JobListCallback> callback = apiInterface.getRecommendJobList(API.toBase64(jsObj.toString()), pageIndex);
        callback.enqueue(new Callback<JobListCallback>() {
            @Override
            public void onResponse(@NonNull Call<JobListCallback> call, @NonNull Response<JobListCallback> response) {
                JobListCallback resp = response.body();
                if (resp != null) {
                    mAdapter.setShouldLoadMore(resp.loadMore);
                    if (isLoadMore) {
                        isLoadMore = false;
                        mAdapter.setListMore(resp.jobList);
                    } else {
                        mAdapter.setListAll(resp.jobList);
                    }

                } else {
                    showErrorState();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JobListCallback> call, @NonNull Throwable t) {
                if (!call.isCanceled())
                    showErrorState();
            }
        });
    }

    private void showErrorState() {
        mAdapter.onError();
        if (isLoadMore) {
            isLoadMore = false;
            mAdapter.setShouldLoadMore(false);
        }
    }

    @Subscribe
    public void onEvent(Events.ProfileUpdate profileUpdate) {
        initHeader();
    }

    private void initHeader() {
        if (myApplication.isLogin()) {
            GlideApp.with(RecommendJobActivity.this).load(myApplication.getLoginInfo().getUserImage()).placeholder(R.drawable.dummy_user).error(R.drawable.dummy_user).into(viewBinding.toolbar.includeImage.ivUserImage);
            viewBinding.toolbar.includeImage.ivUserStatus.setImageResource(R.drawable.online);
        }

        viewBinding.toolbar.includeImage.ivUserImage.setOnClickListener(view -> new ProfilePopUp(RecommendJobActivity.this, view));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        GlobalBus.getBus().unregister(this);
    }
}
