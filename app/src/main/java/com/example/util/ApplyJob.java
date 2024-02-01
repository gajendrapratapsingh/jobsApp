package com.example.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.example.callback.CommonCallback;
import com.example.jobs.LoginActivity;
import com.example.jobs.MyApplication;
import com.example.jobs.PlanActivity;
import com.example.jobs.R;
import com.example.jobs.databinding.LayoutInvalidLicenseSheetBinding;
import com.example.rest.ApiInterface;
import com.example.rest.RestAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApplyJob {

    ProgressDialog pDialog;
    Activity mActivity;
    MyApplication myApplication;

    public ApplyJob(Activity activity, String jobId) {
        this.mActivity = activity;
        myApplication = MyApplication.getInstance();
        pDialog = new ProgressDialog(mActivity, R.style.AlertDialogStyle);
        if (myApplication.isLogin()) {
            if (NetworkUtils.isConnected(activity)) {
                userApply(jobId);
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

    private void userApply(String jobId) {
        showProgressDialog();
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("post_id", jobId);
        jsObj.addProperty("user_id", myApplication.getLoginInfo().getUserId());
        ApiInterface apiInterface = RestAdapter.createAPI();
        Call<CommonCallback> callback = apiInterface.userApplyJob(API.toBase64(jsObj.toString()));
        callback.enqueue(new Callback<CommonCallback>() {
            @Override
            public void onResponse(@NonNull Call<CommonCallback> call, @NonNull Response<CommonCallback> response) {
                dismissProgressDialog();
                CommonCallback resp = response.body();
                if (resp != null) {
                    if (resp.success == 1) {
                        GeneralUtils.showSuccessToast(mActivity, resp.message);
                    } else if (resp.success == 2) {
                        GeneralUtils.showWarningToast(mActivity, resp.message);
                    } else {
                        subscribeAgainSheet(resp.message);
                    }
                } else {
                    GeneralUtils.showSomethingWrong(mActivity);
                }
            }

            @Override
            public void onFailure(@NonNull Call<CommonCallback> call, @NonNull Throwable t) {
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

    private void subscribeAgainSheet(String message) {
        BottomSheetDialog sheetDialog = new BottomSheetDialog(mActivity, R.style.BottomSheetDialog);
        LayoutInvalidLicenseSheetBinding sheetBinding = LayoutInvalidLicenseSheetBinding.inflate(mActivity.getLayoutInflater());
        sheetDialog.setContentView(sheetBinding.getRoot());
        sheetBinding.tvTitle.setText(mActivity.getString(R.string.attention));
        sheetBinding.tvUpdateMsg.setText(message);
        sheetBinding.btnOkay.setText(mActivity.getString(R.string.subscribe));
        sheetBinding.btnOkay.setOnClickListener(view -> {
            Intent intent = new Intent(mActivity, PlanActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mActivity.startActivity(intent);
            sheetDialog.dismiss();
        });
        sheetDialog.show();
    }
}
