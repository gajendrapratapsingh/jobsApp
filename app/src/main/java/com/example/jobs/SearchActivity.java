package com.example.jobs;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.adapter.FilterSelectAdapter;
import com.example.adapter.JobAdapter;
import com.example.callback.FilterDataCallback;
import com.example.callback.JobListCallback;
import com.example.fragment.FilterBottomFragment;
import com.example.jobs.databinding.ActivitySearchBinding;
import com.example.model.Filter;
import com.example.model.FilterData;
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

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {

    ActivitySearchBinding viewBinding;
    MyApplication myApplication;
    String searchText;
    JobAdapter mAdapter;
    FilterSelectAdapter filterSelectAdapter;
    private boolean isLoadMore = false, isFilter = false;
    private int pageIndex = 1;
    ProgressDialog pDialog;
    FragmentManager fragmentManager;
    ArrayList<Filter> filterList, filterSelectedList;
    float minSalary, maxSalary, minSalarySelected, maxSalarySelected;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        StatusBarUtil.setFullScreen(this, viewBinding.getRoot());
        IsRTL.ifSupported(this);
        BannerAds.showBannerAds(this, viewBinding.adView);
        GlobalBus.getBus().register(this);
        myApplication = MyApplication.getInstance();
        pDialog = new ProgressDialog(this, R.style.AlertDialogStyle);

        Intent intent = getIntent();
        searchText = intent.getStringExtra("searchText");

        fragmentManager = getSupportFragmentManager();
        filterSelectedList = new ArrayList<>();
        filterList = new ArrayList<>();

        viewBinding.toolbar.fabFilter.setOnClickListener(view -> onRequestFilterData());

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        viewBinding.recyclerView.setLayoutManager(layoutManager);

        mAdapter = new JobAdapter(this);
        viewBinding.recyclerView.setAdapter(mAdapter);
        viewBinding.recyclerView.addItemDecoration(GeneralUtils.listItemDecoration(this, R.dimen.item_space));

        filterSelectAdapter = new FilterSelectAdapter(this);
        viewBinding.rvFilterSelect.setAdapter(filterSelectAdapter);
        viewBinding.rvFilterSelect.addItemDecoration(GeneralUtils.listItemDecoration(this, R.dimen.item_space_filter));
        filterSelectAdapter.setList(filterSelectedList);
        filterSelectAdapter.setOnItemClickListener((item, position) -> {
            if (item.getFilterType().equals(Filter.SALARY)) {
                minSalarySelected = 0;
                maxSalarySelected = 0;
            }
            filterSelectedList.remove(position);
            filterSelectAdapter.notifyItemRemoved(position);
            filterSelectAdapter.notifyItemRangeChanged(position, filterSelectedList.size());
            onRequestFilter();
        });

        onRequest(isFilter);

        mAdapter.setOnLoadMoreListener(() -> {
            isLoadMore = true;
            pageIndex = pageIndex + 1;
            onRequest(isFilter);
        });

        mAdapter.setOnTryAgainListener(() -> {
            pageIndex = 1;
            onRequest(isFilter);
        });

        mAdapter.setOnItemClickListener((item, position) -> {
            Intent intentDetail = new Intent(this, JobDetailActivity.class);
            intentDetail.putExtra("jobId", item.getJobId());
            startActivity(intentDetail);
        });

        initHeader();
        viewBinding.toolbar.tvName.setText(searchText);
        viewBinding.toolbar.fabBack.setOnClickListener(view -> onBackPressed());
    }

    private void onRequest(boolean isFilter) {
        if (NetworkUtils.isConnected(this)) {
            if (!isLoadMore) {
                mAdapter.onLoading();
            }
            getJobSearchList(isFilter);
        } else {
            showErrorState();
        }
    }

    private void getJobSearchList(boolean isFilter) {
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        ApiInterface apiInterface = RestAdapter.createAPI();
        jsObj.addProperty("search_text", searchText);
        jsObj.addProperty("user_id", myApplication.getLoginInfo().getUserId());
        Call<JobListCallback> callback;
        if (isFilter) {
            jsObj.addProperty("cat_ids", getCommaSepIds(Filter.CATEGORY, true));
            jsObj.addProperty("location_ids", getCommaSepIds(Filter.CITY, true));
            jsObj.addProperty("company_ids", getCommaSepIds(Filter.COMPANY, true));
            jsObj.addProperty("salary_start", minSalarySelected);
            jsObj.addProperty("salary_end", maxSalarySelected);
            jsObj.addProperty("job_type", getCommaSepIds(Filter.JOB_TYPE, false));
            jsObj.addProperty("qualification", getCommaSepIds(Filter.QUALIFICATION, false));
            callback = apiInterface.getJobFilter(API.toBase64(jsObj.toString()), pageIndex);
        } else {
            callback = apiInterface.getJobSearch(API.toBase64(jsObj.toString()), pageIndex);
        }
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
                    if (!resp.jobList.isEmpty())
                        viewBinding.toolbar.fabFilter.setVisibility(View.VISIBLE);
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
    public void onEvent(Events.SaveJob saveJob) {
        mAdapter.onEvent(saveJob);
    }

    @Subscribe
    public void onEvent(Events.ProfileUpdate profileUpdate) {
        initHeader();
    }

    private void initHeader() {
        if (myApplication.isLogin()) {
            GlideApp.with(SearchActivity.this).load(myApplication.getLoginInfo().getUserImage()).placeholder(R.drawable.dummy_user).error(R.drawable.dummy_user).into(viewBinding.toolbar.includeImage.ivUserImage);
            viewBinding.toolbar.includeImage.ivUserStatus.setImageResource(R.drawable.online);
        }

        viewBinding.toolbar.includeImage.ivUserImage.setOnClickListener(view -> new ProfilePopUp(SearchActivity.this, view));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        GlobalBus.getBus().unregister(this);
    }

    private void onRequestFilterData() {
        if (filterList.isEmpty()) {
            if (NetworkUtils.isConnected(this)) {
                getFilterData();
            } else {
                GeneralUtils.showNoNetwork(this);
            }
        } else {
            unCheckAllAndSelect();
            showBottomFilter();
        }
    }

    private void showBottomFilter() {
        Bundle args = new Bundle();
        args.putParcelableArrayList("filterList", filterList);
        args.putFloat("minSalary", minSalary);
        args.putFloat("maxSalary", maxSalary);
        args.putFloat("minSalarySelected", minSalarySelected);
        args.putFloat("maxSalarySelected", maxSalarySelected);
        FilterBottomFragment filterBottomFragment = new FilterBottomFragment();
        filterBottomFragment.show(fragmentManager, filterBottomFragment.getTag());
        filterBottomFragment.setArguments(args);
        filterBottomFragment.setOnFilterButtonClickListener((mFilterList, minSalarySel, maxSalarySel) -> {
            filterSelectedList = mFilterList;
            minSalarySelected = minSalarySel;
            maxSalarySelected = maxSalarySel;
            filterSelectAdapter.setList(filterSelectedList);
            onRequestFilter();
        });
    }

    private void onRequestFilter() {
        isFilter = true;
        pageIndex = 1;
        mAdapter.setShouldLoadMore(true);
        onRequest(isFilter);
    }

    private void getFilterData() {
        showProgressDialog();
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        ApiInterface apiInterface = RestAdapter.createAPI();
        Call<FilterDataCallback> callback = apiInterface.getFilterData(API.toBase64(jsObj.toString()));
        callback.enqueue(new Callback<FilterDataCallback>() {
            @Override
            public void onResponse(@NonNull Call<FilterDataCallback> call, @NonNull Response<FilterDataCallback> response) {
                dismissProgressDialog();
                FilterDataCallback resp = response.body();
                if (resp != null) {
                    setFilterList(resp);
                } else {
                    GeneralUtils.showSomethingWrong(SearchActivity.this);
                }
            }

            @Override
            public void onFailure(@NonNull Call<FilterDataCallback> call, @NonNull Throwable t) {
                if (!call.isCanceled()) {
                    dismissProgressDialog();
                    GeneralUtils.showSomethingWrong(SearchActivity.this);
                }
            }
        });
    }

    private void setFilterList(FilterDataCallback resp) {
        minSalary = resp.minSalary;
        maxSalary = resp.maxSalary;
        for (FilterData filterData : resp.categoryList) {
            filterList.add(new Filter(filterData.getPostId(), filterData.getPostTitle(), Filter.CATEGORY, false));
        }
        for (FilterData filterData : resp.cityList) {
            filterList.add(new Filter(filterData.getPostId(), filterData.getPostTitle(), Filter.CITY, false));
        }
        for (FilterData filterData : resp.companyList) {
            filterList.add(new Filter(filterData.getPostId(), filterData.getPostTitle(), Filter.COMPANY, false));
        }
        for (FilterData filterData : resp.jobTypeList) {
            filterList.add(new Filter(filterData.getPostId(), filterData.getPostTitle(), Filter.JOB_TYPE, false));
        }

        for (FilterData filterData : resp.qualificationList) {
            filterList.add(new Filter(filterData.getPostId(), filterData.getPostTitle(), Filter.QUALIFICATION, false));
        }

        showBottomFilter();
    }

    private void unCheckAllAndSelect() { // list have parcel so when select and press close button it also checked
        for (int i = 0; i < filterList.size(); i++) {
            Filter filter = filterList.get(i);
            filterList.set(i, new Filter(filter.getFilterId(), filter.getFilterName(), filter.getFilterType(), false));
        }
        for (Filter itemFilter : filterSelectedList) {
            if (itemFilter.isSelected()) {
                int index = getIndex(itemFilter);
                if (index != -1)
                    filterList.set(index, itemFilter);
            }
        }
    }

    private int getIndex(Filter itemFilter) {
        return filterList.indexOf(new Filter(itemFilter.getFilterId(), itemFilter.getFilterName(), itemFilter.getFilterType(), false));
    }

    @NonNull
    private String getCommaSepIds(String type, boolean isId) {
        StringBuilder stringBuilder = new StringBuilder();
        String prefix = "";
        for (Filter itemFilter : filterSelectedList) {
            if (itemFilter.getFilterType().equals(type)) {
                stringBuilder.append(prefix);
                prefix = ",";
                stringBuilder.append(isId ? itemFilter.getFilterId() : itemFilter.getFilterName());
            }
        }
        return stringBuilder.toString();
    }

    private void showProgressDialog() {
        pDialog.setMessage(getString(R.string.loading));
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();
    }

    private void dismissProgressDialog() {
        if (pDialog != null && pDialog.isShowing()) {
            pDialog.dismiss();
        }
    }
}
