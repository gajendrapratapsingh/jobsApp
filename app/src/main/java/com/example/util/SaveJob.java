package com.example.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.example.callback.JobSaveCallback;
import com.example.jobs.LoginActivity;
import com.example.jobs.MyApplication;
import com.example.jobs.R;
import com.example.rest.ApiInterface;
import com.example.rest.RestAdapter;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SaveJob {

    ProgressDialog pDialog;
    Activity mActivity;
    MyApplication myApplication;
    SaveClickListener saveClickListener;

    public SaveJob(Activity activity, String jobId, SaveClickListener saveClickListener) {
        this.mActivity = activity;
        this.saveClickListener = saveClickListener;
        myApplication = MyApplication.getInstance();
        pDialog = new ProgressDialog(mActivity, R.style.AlertDialogStyle);
        if (myApplication.isLogin()) {
            if (NetworkUtils.isConnected(activity)) {
                userSave(jobId);
            } else {
                GeneralUtils.showNoNetwork(activity);
            }
        } else {
            GeneralUtils.showWarningToast(activity, activity.getString(R.string.need_login));
            Intent intentLogin = new Intent(activity, LoginActivity.class);
            intentLogin.putExtra("isOtherScreen", true);
            intentLogin.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            activity.startActivity(intentLogin);
        }
    }

    private void userSave(String jobId) {
        showProgressDialog();
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("post_type", "Jobs");
        jsObj.addProperty("post_id", jobId);
        jsObj.addProperty("user_id", myApplication.getLoginInfo().getUserId());
        ApiInterface apiInterface = RestAdapter.createAPI();
        Call<JobSaveCallback> callback = apiInterface.userSaveJob(API.toBase64(jsObj.toString()));
        callback.enqueue(new Callback<JobSaveCallback>() {
            @Override
            public void onResponse(@NonNull Call<JobSaveCallback> call, @NonNull Response<JobSaveCallback> response) {
                dismissProgressDialog();
                JobSaveCallback resp = response.body();
                if (resp != null) {
                    saveClickListener.onItemClick(resp.success);
                    if (resp.success) {
                        GeneralUtils.showSuccessToast(mActivity, resp.message);
                    } else {
                        GeneralUtils.showWarningToast(mActivity, resp.message);
                    }
                    Events.SaveJob saveJob = new Events.SaveJob();
                    saveJob.setJobId(jobId);
                    saveJob.setSave(resp.success);
                    GlobalBus.getBus().post(saveJob);
                } else {
                    GeneralUtils.showSomethingWrong(mActivity);
                }
            }

            @Override
            public void onFailure(@NonNull Call<JobSaveCallback> call, @NonNull Throwable t) {
                if (!call.isCanceled()) {
                    dismissProgressDialog();
                    GeneralUtils.showSomethingWrong(mActivity);
                }
            }
        });
    }

    private void showProgressDialog() {
        pDialog.setMessage(mActivity.getString(R.string.loading));
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();
    }

    private void dismissProgressDialog() {
        if (pDialog != null && pDialog.isShowing())
            pDialog.dismiss();
    }
}
