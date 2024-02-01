package com.example.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.adapter.CategoryAdapter;
import com.example.callback.CategoryListCallback;
import com.example.jobs.JobListByCatActivity;
import com.example.jobs.MyApplication;
import com.example.jobs.R;
import com.example.jobs.databinding.FragmentCategoryBinding;
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

public class CategoryFragment extends Fragment {

    FragmentCategoryBinding viewBinding;
    CategoryAdapter mAdapter;
    MyApplication myApplication;
    private boolean isLoadMore = false;
    private int pageIndex = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewBinding = FragmentCategoryBinding.inflate(inflater, container, false);
        StatusBarUtil.setLightStatusBars(requireActivity(), true);
        GlobalBus.getBus().register(this);
        myApplication = MyApplication.getInstance();
        initHeader();

        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        viewBinding.recyclerView.setLayoutManager(layoutManager);

        mAdapter = new CategoryAdapter(getActivity());
        viewBinding.recyclerView.setAdapter(mAdapter);
        viewBinding.recyclerView.addItemDecoration(GeneralUtils.gridItemDecoration(getActivity(), R.dimen.item_space));
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
            Intent intent = new Intent(getActivity(), JobListByCatActivity.class);
            intent.putExtra("categoryId", item.getCategoryId());
            intent.putExtra("categoryName", item.getCategoryTitle());
            startActivity(intent);
        });

        return viewBinding.getRoot();
    }

    private void onRequest() {
        if (NetworkUtils.isConnected(requireActivity())) {
            if (!isLoadMore) {
                mAdapter.onLoading();
            }
            getCategoryList();
        } else {
            showErrorState();
        }
    }

    private void getCategoryList() {
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        ApiInterface apiInterface = RestAdapter.createAPI();
        Call<CategoryListCallback> callback = apiInterface.getCategoryList(API.toBase64(jsObj.toString()), pageIndex);
        callback.enqueue(new Callback<CategoryListCallback>() {
            @Override
            public void onResponse(@NonNull Call<CategoryListCallback> call, @NonNull Response<CategoryListCallback> response) {
                CategoryListCallback resp = response.body();
                if (resp != null) {
                    mAdapter.setShouldLoadMore(resp.loadMore);
                    if (isLoadMore) {
                        isLoadMore = false;
                        mAdapter.setListMore(resp.categoryList);
                    } else {
                        mAdapter.setListAll(resp.categoryList);
                    }

                } else {
                    showErrorState();
                }
            }

            @Override
            public void onFailure(@NonNull Call<CategoryListCallback> call, @NonNull Throwable t) {
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
