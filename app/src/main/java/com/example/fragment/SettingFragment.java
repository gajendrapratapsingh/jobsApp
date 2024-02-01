package com.example.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.adapter.PageAdapter;
import com.example.jobs.AppliedJobActivity;
import com.example.jobs.ChangePasswordActivity;
import com.example.jobs.DashboardActivity;
import com.example.jobs.EditProfileActivity;
import com.example.jobs.LoginActivity;
import com.example.jobs.MyApplication;
import com.example.jobs.R;
import com.example.jobs.SavedJobActivity;
import com.example.jobs.databinding.FragmentSettingBinding;
import com.example.jobs.databinding.LayoutLogoutSheetBinding;
import com.example.model.Page;
import com.example.rest.ApiInterface;
import com.example.rest.RestAdapter;
import com.example.util.API;
import com.example.util.GeneralUtils;
import com.example.util.NetworkUtils;
import com.example.util.NotificationTiramisu;
import com.example.util.StatusBarUtil;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.onesignal.OneSignal;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingFragment extends Fragment {

    FragmentSettingBinding viewBinding;
    MyApplication myApplication;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewBinding = FragmentSettingBinding.inflate(inflater, container, false);
        StatusBarUtil.setLightStatusBars(requireActivity(), true);
        myApplication = MyApplication.getInstance();
        onRequest();
        viewBinding.swNotification.setChecked(NotificationTiramisu.isNotificationChecked(requireContext()));
        viewBinding.swNotification.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                NotificationTiramisu.takePermissionSettings(requireActivity(), viewBinding.swNotification, activityResultLauncher);
            } else {
                myApplication.setNotification(false);
                OneSignal.disablePush(true);
                OneSignal.unsubscribeWhenNotificationsAreDisabled(false);
            }
        });
        viewBinding.rlMoreApp.setOnClickListener(view -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.play_more_apps)))));
        viewBinding.rlRateApp.setOnClickListener(view -> rateApp());
        viewBinding.rlDashboard.setOnClickListener(view -> goNextActivity(DashboardActivity.class));
        viewBinding.rlEditProfile.setOnClickListener(view -> goNextActivity(EditProfileActivity.class));
        viewBinding.rlSavedJob.setOnClickListener(view -> goNextActivity(SavedJobActivity.class));
        viewBinding.rlAppliedJob.setOnClickListener(view -> goNextActivity(AppliedJobActivity.class));
        viewBinding.rlChangePassword.setOnClickListener(view -> goNextActivity(ChangePasswordActivity.class));
        viewBinding.rlLogout.setOnClickListener(view -> logoutSheet());
        return viewBinding.getRoot();
    }

    private void goNextActivity(Class<?> aClass) {
        if (myApplication.isLogin()) {
            Intent intent = new Intent(requireActivity(), aClass);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else {
            GeneralUtils.showWarningToast(requireActivity(), getString(R.string.need_login));
            Intent intentLogin = new Intent(requireActivity(), LoginActivity.class);
            intentLogin.putExtra("isOtherScreen", true);
            intentLogin.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intentLogin);
        }
    }

    private void onRequest() {
        if (NetworkUtils.isConnected(requireActivity())) {
            getPageList();
        } else {
            showProgress(false);
        }
    }

    private void getPageList() {
        showProgress(true);
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        ApiInterface apiInterface = RestAdapter.createAPI();
        Call<JsonObject> callback = apiInterface.getAppDetail(API.toBase64(jsObj.toString()));
        callback.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                showProgress(false);
                JsonObject resp = response.body();
                if (resp != null) {
                    try {
                        JsonObject joRoot = resp.getAsJsonObject("JOBS_APP");
                        JsonArray jaPages = joRoot.getAsJsonArray("page_list");
                        ArrayList<Page> listPage = new ArrayList<>();
                        for (int i = 0; i < jaPages.size(); i++) {
                            JsonObject joPage = jaPages.get(i).getAsJsonObject();
                            Page page = new Page();
                            page.setPageId(joPage.get("page_id").getAsString());
                            page.setPageTitle(joPage.get("page_title").getAsString());
                            listPage.add(page);
                        }
                        viewBinding.rvPage.setHasFixedSize(true);
                        viewBinding.rvPage.setAdapter(new PageAdapter(requireActivity(), listPage));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (!call.isCanceled()) {
                    showProgress(false);
                }
            }
        });
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

    private void rateApp() {
        final String appName = requireActivity().getPackageName();
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id="
                            + appName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id="
                            + appName)));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (myApplication.isLogin()) {
            viewBinding.rlLogout.setVisibility(View.VISIBLE);
            viewBinding.lineLogout.setVisibility(View.VISIBLE);
        } else {
            viewBinding.rlLogout.setVisibility(View.GONE);
            viewBinding.lineLogout.setVisibility(View.GONE);
        }
    }

    private void logoutSheet() {
        BottomSheetDialog sheetDialog = new BottomSheetDialog(requireActivity(), R.style.BottomSheetDialog);
        LayoutLogoutSheetBinding sheetBinding = LayoutLogoutSheetBinding.inflate(getLayoutInflater());
        sheetDialog.setContentView(sheetBinding.getRoot());
        boolean isRTL = Boolean.parseBoolean(getString(R.string.isRTL));
        if (isRTL) {
            sheetDialog.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
        sheetBinding.btnLogoutCancel.setOnClickListener(view -> sheetDialog.dismiss());
        sheetBinding.btnLogout.setOnClickListener(view -> {
            myApplication.setLogin(false);
            Intent intentSignIn = new Intent(requireActivity(), LoginActivity.class);
            intentSignIn.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intentSignIn.putExtra("isLogout", true);
            startActivity(intentSignIn);
            sheetDialog.dismiss();
        });

        sheetDialog.show();
    }

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> NotificationTiramisu.setCheckedFromSetting(requireActivity(), viewBinding.swNotification));
}
