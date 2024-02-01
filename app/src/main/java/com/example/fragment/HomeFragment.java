package com.example.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adapter.HomeAdapter;
import com.example.adapter.HomeContentAdapter;
import com.example.callback.HomeCallback;
import com.example.jobs.MyApplication;
import com.example.jobs.R;
import com.example.jobs.SearchActivity;
import com.example.jobs.databinding.FragmentHomeBinding;
import com.example.model.Home;
import com.example.model.Job;
import com.example.rest.ApiInterface;
import com.example.rest.RestAdapter;
import com.example.util.API;
import com.example.util.Events;
import com.example.util.GeneralUtils;
import com.example.util.GlideApp;
import com.example.util.GlobalBus;
import com.example.util.NetworkUtils;
import com.example.util.ProfilePopUp;
import com.example.util.RvOnClickListener;
import com.example.util.State;
import com.example.util.StatusBarUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    FragmentHomeBinding viewBinding;
    MyApplication myApplication;
    RvOnClickListener<Home> viewAllClickListener;
    HomeCallback resp;
    HomeAdapter mAdapter;
    ArrayList<Home> listHome = new ArrayList<>();
    Activity activity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewBinding = FragmentHomeBinding.inflate(inflater, container, false);
        StatusBarUtil.setLightStatusBars(requireActivity(), false);
        GlobalBus.getBus().register(this);
        myApplication = MyApplication.getInstance();
        activity = getActivity();
        initHeader();
        onRequest();

        viewBinding.toolbar.ivSearch.setOnClickListener(view -> goSearch());
        viewBinding.toolbar.etSearch.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_SEARCH) {
                goSearch();
                return true;
            }
            return false;
        });
        return viewBinding.getRoot();
    }

    private void goSearch() {
        String search = viewBinding.toolbar.etSearch.getText().toString();
        if (!search.isEmpty()) {
            Intent intent = new Intent(getActivity(), SearchActivity.class);
            intent.putExtra("searchText", search);
            startActivity(intent);
            viewBinding.toolbar.etSearch.setText("");
        }
    }

    private void onRequest() {
        if (isAdded() && activity != null) {
            if (NetworkUtils.isConnected(requireActivity())) {
                getHome();
            } else {
                showErrorState(State.STATE_NO_INTERNET);
            }
        }
    }

    private void getHome() {
        showProgress(true);
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("user_id", myApplication.getLoginInfo().getUserId());
        ApiInterface apiInterface = RestAdapter.createAPI();
        Call<HomeCallback> callback = apiInterface.getHome(API.toBase64(jsObj.toString()));
        callback.enqueue(new Callback<HomeCallback>() {
            @Override
            public void onResponse(@NonNull Call<HomeCallback> call, @NonNull Response<HomeCallback> response) {
                showProgress(false);
                resp = response.body();
                if (resp != null && getActivity() != null) {
                    setDataToView();
                } else {
                    showErrorState(State.STATE_ERROR_IN_API);
                }
            }

            @Override
            public void onFailure(@NonNull Call<HomeCallback> call, @NonNull Throwable t) {
                if (!call.isCanceled())
                    showErrorState(State.STATE_ERROR_IN_API);
            }
        });
    }

    private void setDataToView() {
        if (resp.categoryList.isEmpty() && resp.latestJobList.isEmpty() && resp.recentJobList.isEmpty() && resp.companyList.isEmpty() && resp.recommendJobList.isEmpty()) {
            showErrorState(State.STATE_EMPTY);
        } else {
            if (!resp.recentJobList.isEmpty()) {
                Home homeRecent = new Home("1", activity.getString(R.string.recent_job), Home.RECENT, new ArrayList<>(resp.recentJobList));
                listHome.add(homeRecent);
            }
            if (!resp.companyList.isEmpty()) {
                Home homeCompany = new Home("2", activity.getString(R.string.company), Home.COMPANY, new ArrayList<>(resp.companyList));
                listHome.add(homeCompany);
            }
            if (!resp.recommendJobList.isEmpty()) {
                Home homeCompany = new Home("3", activity.getString(R.string.recommended_job), Home.RECOMMEND, new ArrayList<>(resp.recommendJobList));
                listHome.add(homeCompany);
            }
            if (!resp.categoryList.isEmpty()) {
                Home homeCategory = new Home("4", activity.getString(R.string.category), Home.CATEGORY, new ArrayList<>(resp.categoryList));
                listHome.add(homeCategory);
            }
            if (!resp.latestJobList.isEmpty()) {
                Home homeLatest = new Home("5", activity.getString(R.string.latest_job), Home.LATEST, new ArrayList<>(resp.latestJobList));
                listHome.add(homeLatest);
            }

            LinearLayoutManager layoutManager = new LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false);
            viewBinding.recyclerView.setLayoutManager(layoutManager);
            mAdapter = new HomeAdapter(requireActivity(), listHome);
            viewBinding.recyclerView.setAdapter(mAdapter);
            mAdapter.setOnItemClickListener((item, position) ->
                    viewAllClickListener.onItemClick(item, position));
        }
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

    private void showErrorState(int state) {
        viewBinding.progressBar.setVisibility(View.GONE);
        viewBinding.parent.setVisibility(View.GONE);
        viewBinding.incState.errorState.setVisibility(View.VISIBLE);
        GeneralUtils.changeStateInfo(activity, state, viewBinding.incState.ivState, viewBinding.incState.tvError, viewBinding.incState.tvErrorMsg);
        viewBinding.incState.btnTryAgain.setOnClickListener(view -> {
            viewBinding.incState.errorState.setVisibility(View.GONE);
            onRequest();
        });
    }

    private void initHeader() {
        if (myApplication.isLogin()) {
            viewBinding.toolbar.tvUserName.setText(myApplication.getLoginInfo().getUserName());
            GlideApp.with(requireActivity()).load(myApplication.getLoginInfo().getUserImage()).placeholder(R.drawable.dummy_user).error(R.drawable.dummy_user).into(viewBinding.toolbar.includeImage.ivUserImage);
            viewBinding.toolbar.includeImage.ivUserStatus.setImageResource(R.drawable.online);
        }

        viewBinding.toolbar.includeImage.ivUserImage.setOnClickListener(view -> new ProfilePopUp(requireActivity(), view));
    }

    @Subscribe
    public void onEvent(Events.ProfileUpdate profileUpdate) {
        initHeader();
    }

    @Subscribe
    public void onEvent(Events.SaveJob saveJob) {
        if (mAdapter != null) {
            for (int i = 0; i < listHome.size(); i++) {
                Home home = listHome.get(i);
                if (home.getHomeType() == Home.LATEST || home.getHomeType() == Home.RECENT) {
                    for (int k = 0; k < home.getItemHomeContents().size(); k++) {
                        Object obj = home.getItemHomeContents().get(k);
                        Job item = (Job) obj;
                        if (item.getJobId().equals(saveJob.getJobId())) {
                            item.setJobSaved(saveJob.isSave());
                            RecyclerView.ViewHolder ithChildViewHolder = viewBinding.recyclerView.
                                    findViewHolderForAdapterPosition(i);
                            if (ithChildViewHolder != null) {
                                RecyclerView ithChildRecyclerView = ithChildViewHolder.itemView.findViewById(R.id.rvContent);
                                HomeContentAdapter ithChildAdapter = (HomeContentAdapter) ithChildRecyclerView.getAdapter();
                                assert ithChildAdapter != null;
                                ithChildAdapter.notifyItemChanged(k, item);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        GlobalBus.getBus().unregister(this);
    }

    public void setOnViewAllClickListener(RvOnClickListener<Home> clickListener) {
        this.viewAllClickListener = clickListener;
    }

}
