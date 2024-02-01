package com.example.jobs;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.fragment.CategoryFragment;
import com.example.fragment.HomeFragment;
import com.example.fragment.LatestJobFragment;
import com.example.fragment.SettingFragment;
import com.example.jobs.databinding.ActivityMainBinding;
import com.example.jobs.databinding.LayoutUpdateAppSheetBinding;
import com.example.model.BottomBar;
import com.example.model.Home;
import com.example.util.AppUtil;
import com.example.util.BannerAds;
import com.example.util.IsRTL;
import com.example.util.NotificationTiramisu;
import com.example.util.StatusBarUtil;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.ixidev.gdpr.GDPRChecker;
import com.wortise.ads.WortiseSdk;
import com.wortise.ads.consent.ConsentManager;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import kotlin.Unit;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding viewBinding;
    private FragmentManager fragmentManager;
    ArrayList<BottomBar> bottomBarList;
    boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        StatusBarUtil.setFullScreen(this, viewBinding.getRoot());
        IsRTL.ifSupported(this);
        initGdpr();
        BannerAds.showBannerAds(this, viewBinding.adView);
        fragmentManager = getSupportFragmentManager();
        bottomBarList = BottomBar.listOfBottomBarItem(viewBinding);

        goHome();

        viewBinding.bottomBar.llHome.setOnClickListener(view -> goHome());

        viewBinding.bottomBar.llLatestJob.setOnClickListener(view -> goLatest());

        viewBinding.bottomBar.llCategory.setOnClickListener(view -> goCategory());

        viewBinding.bottomBar.llSetting.setOnClickListener(view -> {
            selectBottomNav(3);
            SettingFragment settingFragment = new SettingFragment();
            loadFrag(settingFragment, getString(R.string.setting), fragmentManager);
        });

        initAppUpdate();
        //     getFbKeyHash(); // comment this method after generating key hash
        NotificationTiramisu.takePermission(this);
    }

    private void goHome() {
        selectBottomNav(0);
        HomeFragment homeFragment = new HomeFragment();
        homeFragment.setOnViewAllClickListener((item, position) -> {
            switch (item.getHomeType()) {
                case Home.CATEGORY:
                    goCategory();
                    break;
                case Home.LATEST:
                    goLatest();
                    break;
                case Home.RECENT:
                    Intent intent = new Intent(this, RecentJobActivity.class);
                    startActivity(intent);
                    break;
                case Home.COMPANY:
                    Intent intentCompany = new Intent(this, CompanyListActivity.class);
                    startActivity(intentCompany);
                    break;
                case Home.RECOMMEND:
                    Intent intentRecommend = new Intent(this, RecommendJobActivity.class);
                    startActivity(intentRecommend);
                    break;
            }
        });
        loadFrag(homeFragment, getString(R.string.home), fragmentManager);
    }

    private void goLatest() {
        selectBottomNav(1);
        LatestJobFragment latestJobFragment = new LatestJobFragment();
        loadFrag(latestJobFragment, getString(R.string.latest_job), fragmentManager);
    }

    private void goCategory() {
        selectBottomNav(2);
        CategoryFragment categoryFragment = new CategoryFragment();
        loadFrag(categoryFragment, getString(R.string.category), fragmentManager);
    }

    public void loadFrag(Fragment f1, String name, FragmentManager fm) {
        for (int i = 0; i < fm.getBackStackEntryCount(); ++i) {
            fm.popBackStack();
        }
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragment_container_view, f1, name);
        ft.commit();
    }

    private void selectBottomNav(int posClick) {
        for (int i = 0; i < bottomBarList.size(); i++) {
            BottomBar bottomBar = bottomBarList.get(i);
            if (posClick == i) {
                bottomBar.getFrameLayout().setBackgroundResource(R.drawable.bottom_bar_select_bg);
                bottomBar.getTextView().setTextColor(getResources().getColor(R.color.colorPrimary));
                bottomBar.getImageView().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
            } else {
                bottomBar.getFrameLayout().setBackgroundResource(R.drawable.bottom_bar_normal_bg);
                bottomBar.getTextView().setTextColor(getResources().getColor(R.color.bottom_bar_normal_title));
                bottomBar.getImageView().setColorFilter(getResources().getColor(R.color.bottom_bar_normal_title), PorterDuff.Mode.SRC_IN);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, getString(R.string.back_key), Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
    }

    private void initAppUpdate() {
        int versionCode = 0;
        try {
            versionCode = getPackageManager()
                    .getPackageInfo(getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (versionCode != AppUtil.appUpdateVersion && AppUtil.isAppUpdate) {
            updateAppSheet();
        }
    }

    private void updateAppSheet() {
        BottomSheetDialog sheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialog);
        LayoutUpdateAppSheetBinding sheetBinding = LayoutUpdateAppSheetBinding.inflate(getLayoutInflater());
        sheetDialog.setContentView(sheetBinding.getRoot());
        boolean isRTL = Boolean.parseBoolean(getString(R.string.isRTL));
        if (isRTL) {
            sheetDialog.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
        sheetBinding.tvUpdateMsg.setText(AppUtil.appUpdateDesc);
        sheetBinding.btnCancel.setOnClickListener(view -> sheetDialog.dismiss());
        sheetBinding.btnUpdate.setOnClickListener(view -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(AppUtil.appUpdateUrl)));
            sheetDialog.dismiss();
        });
        if (AppUtil.isAppUpdateCancel) {
            sheetBinding.btnCancel.setVisibility(View.VISIBLE);
        } else {
            sheetBinding.btnCancel.setVisibility(View.GONE);
            sheetDialog.setCancelable(false);
            sheetDialog.setCanceledOnTouchOutside(false);
        }
        sheetDialog.show();
    }

    private void initGdpr() {
        if (AppUtil.adNetworkType != null && AppUtil.adNetworkType.equals(AppUtil.admobAd)) {
            new GDPRChecker()
                    .withContext(MainActivity.this)
                    .withPrivacyUrl(BuildConfig.SERVER_URL + "page/3/privacy-policy") // your privacy url
                    .withPublisherIds(AppUtil.appIdOrPublisherId) // your admob account Publisher id
                    // .withTestMode("9424DF76F06983D1392E609FC074596C") // remove this on real project
                    .check();
        } else if (AppUtil.adNetworkType != null && AppUtil.adNetworkType.equals(AppUtil.wortiseAd)) {
            WortiseSdk.initialize(this, AppUtil.appIdOrPublisherId, true, () -> {
                ConsentManager.requestOnce(MainActivity.this);
                return Unit.INSTANCE;
            });
        }
    }

    private void getFbKeyHash() {
        try {
            @SuppressLint("PackageManagerGetSignatures") PackageInfo info = getPackageManager().getPackageInfo(
                    getPackageName(), //Insert your own package name.
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException ignored) {

        }
    }
}