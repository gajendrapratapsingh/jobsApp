package com.example.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.view.View;

import androidx.annotation.NonNull;

import com.example.callback.CommonCallback;
import com.example.jobs.LoginActivity;
import com.example.jobs.MyApplication;
import com.example.jobs.R;
import com.example.jobs.databinding.LayoutReportSheetBinding;
import com.example.rest.ApiInterface;
import com.example.rest.RestAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportJob {
    ProgressDialog pDialog;
    Activity mActivity;
    MyApplication myApplication;
    BottomSheetDialog sheetDialog;
    String jobId;

    public ReportJob(Activity activity, String jobId) {
        this.mActivity = activity;
        this.jobId = jobId;
        myApplication = MyApplication.getInstance();
        pDialog = new ProgressDialog(mActivity, R.style.AlertDialogStyle);
        sheetDialog = new BottomSheetDialog(mActivity, R.style.BottomSheetDialog);

        if (myApplication.isLogin()) {
            showReportSheet();
        } else {
            GeneralUtils.showWarningToast(activity, activity.getString(R.string.need_login));
            Intent intentLogin = new Intent(activity, LoginActivity.class);
            intentLogin.putExtra("isOtherScreen", true);
            intentLogin.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            activity.startActivity(intentLogin);
        }
    }

    private void showReportSheet() {
        LayoutReportSheetBinding sheetBinding = LayoutReportSheetBinding.inflate(mActivity.getLayoutInflater());
        sheetDialog.setContentView(sheetBinding.getRoot());
        boolean isRTL = Boolean.parseBoolean(mActivity.getString(R.string.isRTL));
        if (isRTL) {
            sheetDialog.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
        sheetBinding.btnLater.setOnClickListener(view -> sheetDialog.dismiss());
        sheetBinding.ivClose.setOnClickListener(view -> sheetDialog.dismiss());
        sheetBinding.btnSubmit.setOnClickListener(view -> {
            String reportMsg = sheetBinding.etReportReason.getText().toString();
            if (!reportMsg.isEmpty()) {
                if (NetworkUtils.isConnected(mActivity)) {
                    reportJob(reportMsg);
                } else {
                    GeneralUtils.showNoNetwork(mActivity);
                }
            }
        });

        sheetDialog.show();
    }

    private void reportJob(String reportMsg) {
        showProgressDialog();
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("post_id", jobId);
        jsObj.addProperty("user_id", myApplication.getLoginInfo().getUserId());
        jsObj.addProperty("post_type", "Jobs");
        jsObj.addProperty("message", reportMsg);
        ApiInterface apiInterface = RestAdapter.createAPI();
        Call<CommonCallback> callback = apiInterface.reportJob(API.toBase64(jsObj.toString()));
        callback.enqueue(new Callback<CommonCallback>() {
            @Override
            public void onResponse(@NonNull Call<CommonCallback> call, @NonNull Response<CommonCallback> response) {
                dismissProgressDialog();
                CommonCallback resp = response.body();
                if (resp != null && resp.success == 1) {
                    GeneralUtils.showSuccessToast(mActivity, resp.message);
                    sheetDialog.dismiss();
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
}
