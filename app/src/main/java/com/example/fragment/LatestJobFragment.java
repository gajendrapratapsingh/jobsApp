package com.example.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.adapter.JobAdapter;
import com.example.callback.JobListCallback;
import com.example.jobs.JobDetailActivity;
import com.example.jobs.MyApplication;
import com.example.jobs.R;
import com.example.jobs.databinding.FragmentJobBinding;
import com.example.rest.ApiInterface;
import com.example.rest.RestAdapter;
import com.example.util.API;
import com.example.util.Events;
import com.example.util.GeneralUtils;
import com.example.util.GlideApp;
import com.example.util.GlobalBus;
import com.example.util.NetworkUtils;
import com.example.util.ProfilePopUp;
import com.example.util.StatusBarUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.greenrobot.eventbus.Subscribe;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LatestJobFragment extends Fragment {

    FragmentJobBinding viewBinding;
    JobAdapter mAdapter;
    MyApplication myApplication;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewBinding = FragmentJobBinding.inflate(inflater, container, false);
        StatusBarUtil.setLightStatusBars(requireActivity(), true);
        GlobalBus.getBus().register(this);
        myApplication = MyApplication.getInstance();
        initHeader();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        viewBinding.recyclerView.setLayoutManager(layoutManager);

        mAdapter = new JobAdapter(getActivity());
        viewBinding.recyclerView.setAdapter(mAdapter);
        viewBinding.recyclerView.addItemDecoration(GeneralUtils.listItemDecoration(getActivity(), R.dimen.item_space));

        onRequest();

        mAdapter.setOnTryAgainListener(this::onRequest);

        mAdapter.setOnItemClickListener((item, position) -> {
            Intent intentDetail = new Intent(requireActivity(), JobDetailActivity.class);
            intentDetail.putExtra("jobId", item.getJobId());
            startActivity(intentDetail);
        });


        return viewBinding.getRoot();
    }

    private void onRequest() {
        if (NetworkUtils.isConnected(requireActivity())) {
            mAdapter.onLoading();
            getLatestList();
        } else {
            showErrorState();
        }
    }

    private void getLatestList() {
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("user_id", myApplication.getLoginInfo().getUserId());
        ApiInterface apiInterface = RestAdapter.createAPI();
        Call<JobListCallback> callback = apiInterface.getLatestJobList(API.toBase64(jsObj.toString()));
        callback.enqueue(new Callback<JobListCallback>() {
            @Override
            public void onResponse(@NonNull Call<JobListCallback> call, @NonNull Response<JobListCallback> response) {
                JobListCallback resp = response.body();
                if (resp != null) {
                    mAdapter.setShouldLoadMore(false);
                    mAdapter.setListAll(resp.jobList);
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
            viewBinding.toolbar.tvUserName.setText(myApplication.getLoginInfo().getUserName());
            GlideApp.with(requireActivity()).load(myApplication.getLoginInfo().getUserImage()).placeholder(R.drawable.dummy_user).error(R.drawable.dummy_user).into(viewBinding.toolbar.includeImage.ivUserImage);
            viewBinding.toolbar.includeImage.ivUserStatus.setImageResource(R.drawable.online);
        }

        viewBinding.toolbar.includeImage.ivUserImage.setOnClickListener(view -> new ProfilePopUp(requireActivity(), view));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        GlobalBus.getBus().unregister(this);
    }

}
