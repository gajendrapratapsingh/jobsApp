package com.example.util;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.example.jobs.ChangePasswordActivity;
import com.example.jobs.DashboardActivity;
import com.example.jobs.EditProfileActivity;
import com.example.jobs.LoginActivity;
import com.example.jobs.MyApplication;
import com.example.jobs.R;
import com.example.jobs.databinding.LayoutLogoutSheetBinding;
import com.example.jobs.databinding.LayoutProfilePopupBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class ProfilePopUp {
    Activity activity;
    View anchorView;
    MyApplication myApplication;

    public ProfilePopUp(Activity activity, View anchorView) {
        this.activity = activity;
        this.anchorView = anchorView;
        myApplication = MyApplication.getInstance();
        if (myApplication.isLogin()) {
            show();
        } else {
            Intent intent = new Intent(activity, LoginActivity.class);
            intent.putExtra("isOtherScreen", true);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            activity.startActivity(intent);
        }
    }

    private void show() {
        LayoutProfilePopupBinding viewBinding = LayoutProfilePopupBinding.inflate(activity.getLayoutInflater());
        PopupWindow popupWindow = new PopupWindow(viewBinding.getRoot(), RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT, true);
        popupWindow.showAsDropDown(anchorView, -153, 0);

        viewBinding.tvEditProfile.setOnClickListener(view -> {
            goNextActivity(EditProfileActivity.class);
            popupWindow.dismiss();
        });

        viewBinding.tvDashboard.setOnClickListener(view -> {
            goNextActivity(DashboardActivity.class);
            popupWindow.dismiss();
        });
        viewBinding.tvChanePassword.setSelected(true);
        viewBinding.tvChanePassword.setOnClickListener(view -> {
            goNextActivity(ChangePasswordActivity.class);
            popupWindow.dismiss();
        });

        viewBinding.tvLogout.setOnClickListener(view -> {
            logoutSheet();
            popupWindow.dismiss();
        });
    }

    private void goNextActivity(Class<?> aClass) {
        Intent intent = new Intent(activity, aClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
    }

    private void logoutSheet() {
        BottomSheetDialog sheetDialog = new BottomSheetDialog(activity, R.style.BottomSheetDialog);
        LayoutLogoutSheetBinding sheetBinding = LayoutLogoutSheetBinding.inflate(activity.getLayoutInflater());
        sheetDialog.setContentView(sheetBinding.getRoot());
        boolean isRTL = Boolean.parseBoolean(activity.getString(R.string.isRTL));
        if (isRTL) {
            sheetDialog.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
        sheetBinding.btnLogoutCancel.setOnClickListener(view -> sheetDialog.dismiss());
        sheetBinding.btnLogout.setOnClickListener(view -> {
            myApplication.setLogin(false);
            Intent intentSignIn = new Intent(activity, LoginActivity.class);
            intentSignIn.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intentSignIn.putExtra("isLogout", true);
            activity.startActivity(intentSignIn);
            sheetDialog.dismiss();
            activity.finishAffinity();
        });

        sheetDialog.show();
    }
}
