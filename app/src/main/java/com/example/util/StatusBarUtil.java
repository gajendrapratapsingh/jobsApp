package com.example.util;

import android.app.Activity;
import android.view.View;

import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import dev.chrisbanes.insetter.Insetter;

/*
    Light status have black icon and text in status bar
 */
public class StatusBarUtil {

    public static void setFullScreen(Activity activity, View view) {
        WindowCompat.setDecorFitsSystemWindows(activity.getWindow(), false);
        Insetter.builder().padding(WindowInsetsCompat.Type.navigationBars()).applyToView(view);
    }

    public static void setFullScreenWithLightStatusBars(Activity activity, View view, boolean isLight) {
        WindowCompat.setDecorFitsSystemWindows(activity.getWindow(), false);
        new WindowInsetsControllerCompat(activity.getWindow(), activity.getWindow().getDecorView()).setAppearanceLightStatusBars(isLight);
        Insetter.builder().padding(WindowInsetsCompat.Type.navigationBars()).applyToView(view);
    }

    public static void setLightStatusBars(Activity activity, boolean isLight) {
        new WindowInsetsControllerCompat(activity.getWindow(), activity.getWindow().getDecorView()).setAppearanceLightStatusBars(isLight);
    }
}
