package com.example.jobs;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.adapter.CompanyAdapter;
import com.example.callback.CompanyListCallback;
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

public class CompanyListActivity extends AppCompatActivity {

    ActivityJobListBinding viewBinding;
    CompanyAdapter mAdapter;
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

        mAdapter = new CompanyAdapter(this);
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
            Intent intent = new Intent(this, JobListByCompanyActivity.class);
            intent.putExtra("companyId", item.getCompanyId());
            intent.putExtra("companyName", item.getCompanyTitle());
            startActivity(intent);
        });

        initHeader();
        viewBinding.toolbar.tvName.setText(getString(R.string.company));
        viewBinding.toolbar.fabBack.setOnClickListener(view -> onBackPressed());
    }

    private void onRequest() {
        if (NetworkUtils.isConnected(CompanyListActivity.this)) {
            if (!isLoadMore) {
                mAdapter.onLoading();
            }
            getCompanyList();
        } else {
            showErrorState();
        }
    }

    private void getCompanyList() {
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        ApiInterface apiInterface = RestAdapter.createAPI();
        Call<CompanyListCallback> callback = apiInterface.getCompanyList(API.toBase64(jsObj.toString()), pageIndex);
        callback.enqueue(new Callback<CompanyListCallback>() {
            @Override
            public void onResponse(@NonNull Call<CompanyListCallback> call, @NonNull Response<CompanyListCallback> response) {
                CompanyListCallback resp = response.body();
                if (resp != null) {
                    mAdapter.setShouldLoadMore(resp.loadMore);
                    if (isLoadMore) {
                        isLoadMore = false;
                        mAdapter.setListMore(resp.companyList);
                    } else {
                        mAdapter.setListAll(resp.companyList);
                    }

                } else {
                    showErrorState();
                }
            }

            @Override
            public void onFailure(@NonNull Call<CompanyListCallback> call, @NonNull Throwable t) {
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
    public void onEvent(Events.SaveJob saveJob) {
        mAdapter.onEvent(saveJob);
    }

    @Subscribe
    public void onEvent(Events.ProfileUpdate profileUpdate) {
        initHeader();
    }

    private void initHeader() {
        if (myApplication.isLogin()) {
            GlideApp.with(CompanyListActivity.this).load(myApplication.getLoginInfo().getUserImage()).placeholder(R.drawable.dummy_user).error(R.drawable.dummy_user).into(viewBinding.toolbar.includeImage.ivUserImage);
            viewBinding.toolbar.includeImage.ivUserStatus.setImageResource(R.drawable.online);
        }

        viewBinding.toolbar.includeImage.ivUserImage.setOnClickListener(view -> new ProfilePopUp(CompanyListActivity.this, view));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        GlobalBus.getBus().unregister(this);
    }
}
