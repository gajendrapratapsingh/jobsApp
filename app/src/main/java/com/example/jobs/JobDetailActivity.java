package com.example.jobs;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.adapter.SimilarJobAdapter;
import com.example.adapter.SkillAdapter;
import com.example.callback.JobDetailCallback;
import com.example.jobs.databinding.ActivityJobDetailBinding;
import com.example.model.Job;
import com.example.rest.ApiInterface;
import com.example.rest.RestAdapter;
import com.example.util.API;
import com.example.util.AppUtil;
import com.example.util.ApplyJob;
import com.example.util.BannerAds;
import com.example.util.GeneralUtils;
import com.example.util.GlideApp;
import com.example.util.IsRTL;
import com.example.util.LinearLayoutPagerManager;
import com.example.util.NetworkUtils;
import com.example.util.ReportJob;
import com.example.util.SaveJob;
import com.example.util.StatusBarUtil;
import com.google.android.material.appbar.AppBarLayout;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class JobDetailActivity extends AppCompatActivity {

    ActivityJobDetailBinding viewBinding;
    String jobId;
    Job jobDetail;
    MyApplication myApplication;
    boolean isFromNotification = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityJobDetailBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        StatusBarUtil.setFullScreenWithLightStatusBars(this, viewBinding.getRoot(), false);
        IsRTL.ifSupported(this);
        BannerAds.showBannerAds(this, viewBinding.adView);
        myApplication = MyApplication.getInstance();
        jobDetail = new Job();
        Intent intent = getIntent();
        jobId = intent.getStringExtra("jobId");
        if (intent.hasExtra("isNotification")) {
            isFromNotification = intent.getBooleanExtra("isNotification", false);
        }
        onRequest();

        viewBinding.fabBack.setOnClickListener(view -> onBackPressed());

    }

    private void onRequest() {
        if (NetworkUtils.isConnected(JobDetailActivity.this)) {
            getJobDetail();
        } else {
            showErrorState();
        }
    }

    private void getJobDetail() {
        showProgress(true);
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("job_id", jobId);
        jsObj.addProperty("user_id", myApplication.getLoginInfo().getUserId());
        ApiInterface apiInterface = RestAdapter.createAPI();
        Call<JobDetailCallback> callback = apiInterface.getJobDetail(API.toBase64(jsObj.toString()));
        callback.enqueue(new Callback<JobDetailCallback>() {
            @Override
            public void onResponse(@NotNull Call<JobDetailCallback> call, @NotNull Response<JobDetailCallback> response) {
                showProgress(false);
                JobDetailCallback resp = response.body();
                if (resp != null) {
                    jobDetail = resp.jobDetail;
                    setDataToView();
                } else {
                    showErrorState();
                }
            }

            @Override
            public void onFailure(@NotNull Call<JobDetailCallback> call, @NotNull Throwable t) {
                if (!call.isCanceled()) {
                    showProgress(false);
                    showErrorState();
                }
            }
        });
    }

    private void setDataToView() {
        viewBinding.tvJobTitle.setText(jobDetail.getJobTitle());
        viewBinding.tvToolbarTitle.setText(jobDetail.getJobTitle());
        viewBinding.tvJobLocation.setText(jobDetail.getJobLocation());
        viewBinding.tvJobVacancy.setText(getString(R.string.job_vacancy, jobDetail.getJobVacancy()));
        viewBinding.tvJobDate.setText(jobDetail.getJobDate());
        viewBinding.tvJobDesignation.setText(jobDetail.getJobDesignation());
        viewBinding.tvJobPhone.setText(jobDetail.getJobPhone());
        viewBinding.tvJobEmail.setText(jobDetail.getJobEmail());
        viewBinding.tvJobWebsite.setText(jobDetail.getJobWebsite());
        viewBinding.tvJobAddress.setText(jobDetail.getJobAddress());
        viewBinding.tvJobDescription.setText(GeneralUtils.html2text(jobDetail.getJobDescription()));
        viewBinding.tvJobQualification.setText(jobDetail.getJobQualification());
        viewBinding.tvSalary.setText(getString(R.string.salary_currency, AppUtil.currencyCode, GeneralUtils.viewFormat(jobDetail.getJobSalary())));
        viewBinding.tvJobSalary.setText(getString(R.string.salary_currency, AppUtil.currencyCode, GeneralUtils.viewFormat(jobDetail.getJobSalary())));
        viewBinding.tvJobWorkDay.setText(jobDetail.getJobWorkDays());
        viewBinding.tvJobWorkTime.setText(jobDetail.getJobWorkTime());
        viewBinding.tvJobType.setText(jobDetail.getJobType());

        viewBinding.rvSkill.setHasFixedSize(true);
        SkillAdapter skillAdapter = new SkillAdapter(this, GeneralUtils.getSkills(jobDetail.getJobSkills()), true);
        viewBinding.rvSkill.setAdapter(skillAdapter);

        GlideApp.with(this).load(jobDetail.getJobImage()).into(viewBinding.ivJob);
        changeSave(jobDetail.isJobSaved());
        viewBinding.btnJobApplyDetail.setOnClickListener(view -> new ApplyJob(this, jobId));
        viewBinding.fabSave.setOnClickListener(view -> new SaveJob(this, jobId, this::changeSave));
        viewBinding.fabReport.setOnClickListener(view -> new ReportJob(this, jobId));
        viewBinding.fabShare.setOnClickListener(view -> shareJob());

        if (jobDetail.getSimilarJobList().isEmpty()) {
            viewBinding.llSimilar.setVisibility(View.GONE);
        } else {
            viewBinding.rvSimilar.setHasFixedSize(true);
            viewBinding.rvSimilar.setNestedScrollingEnabled(false);
            LinearLayoutPagerManager layoutManager = new LinearLayoutPagerManager(this, LinearLayoutManager.HORIZONTAL, false, 1.3);
            viewBinding.rvSimilar.setLayoutManager(layoutManager);
            SimilarJobAdapter mAdapter = new SimilarJobAdapter(this, jobDetail.getSimilarJobList());
            viewBinding.rvSimilar.setAdapter(mAdapter);
            viewBinding.rvSimilar.addItemDecoration(GeneralUtils.listItemDecoration(this, R.dimen.item_space));
            mAdapter.setOnItemClickListener((item, position) -> {
                Intent intentDetail = new Intent(this, JobDetailActivity.class);
                intentDetail.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intentDetail.putExtra("jobId", item.getJobId());
                startActivity(intentDetail);
            });
        }

        hideIfEmpty(jobDetail.getJobDescription().isEmpty(), viewBinding.cvDesc);
        hideIfEmpty(jobDetail.getJobQualification().isEmpty(), viewBinding.cvQualification);
        hideIfEmpty(jobDetail.getJobSalary().isEmpty(), viewBinding.cvSalary);
        hideIfEmpty(jobDetail.getJobWorkDays().isEmpty(), viewBinding.cvDay);
        hideIfEmpty(jobDetail.getJobWorkTime().isEmpty(), viewBinding.cvTime);
        viewBinding.appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    isShow = true;
                    viewBinding.tvToolbarTitle.animate().alpha(1.0f);
                } else if (isShow) {
                    isShow = false;
                    viewBinding.tvToolbarTitle.animate().alpha(0.0f);
                }
            }
        });
    }

    private void hideIfEmpty(boolean isEmpty, View view) {
        view.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void changeSave(boolean isSave) {
        viewBinding.fabSave.setImageResource(isSave ? R.drawable.ic_bookmark_select : R.drawable.ic_bookmark);
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

    private void shareJob() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT,
                jobDetail.getJobTitle() + "\n" +
                        getString(R.string.share_location, jobDetail.getJobLocation()) + "\n" +
                        getString(R.string.share_designation, jobDetail.getJobDesignation()) + "\n" +
                        getString(R.string.share_phone, jobDetail.getJobPhone()) + "\n" +
                        getString(R.string.share_email, jobDetail.getJobEmail()) + "\n" +
                        getString(R.string.share_website, jobDetail.getJobWebsite()) + "\n" +
                        getString(R.string.share_address, jobDetail.getJobAddress()) + "\n" +
                        getString(R.string.share_salary, AppUtil.currencyCode, GeneralUtils.viewFormat(jobDetail.getJobSalary())) + "\n\n" +
                        getString(R.string.share_app_link) + getPackageName());
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }


    @Override
    public void onBackPressed() {
        if (isFromNotification) {
            Intent intent = new Intent(JobDetailActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else {
            super.onBackPressed();
        }
    }
}
