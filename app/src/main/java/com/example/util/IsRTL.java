package com.example.util;

import android.app.Activity;
import android.view.View;

import com.example.jobs.R;

public class IsRTL {
    public static void ifSupported(Activity activity) {
        boolean isRTL = Boolean.parseBoolean(activity.getString(R.string.isRTL));
        if (isRTL) {
            activity.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
    }
}
