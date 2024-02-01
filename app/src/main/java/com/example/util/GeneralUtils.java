package com.example.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DimenRes;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobs.AddTransactionActivity;
import com.example.jobs.DashboardActivity;
import com.example.jobs.LoginActivity;
import com.example.jobs.MyApplication;
import com.example.jobs.R;
import com.littlejerk.rvdivider.builder.XGridBuilder;
import com.littlejerk.rvdivider.builder.XLinearBuilder;
import com.littlejerk.rvdivider.decoration.LDecoration;
import com.mrntlu.toastie.Toastie;

import org.jsoup.Jsoup;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

public class GeneralUtils {

    public static ArrayList<String> getSkills(String skill) {
        String[] skills = skill.split(",");
        return new ArrayList<>(Arrays.asList(skills));
    }

    public static RecyclerView.ItemDecoration gridItemDecoration(Activity activity, @DimenRes int dimenResId) {
        return new XGridBuilder(activity)
                .setSpacing(ResourcesCompat.getFloat(activity.getResources(), dimenResId))
                .setIncludeEdge(true)
                .build();
    }

    public static RecyclerView.ItemDecoration listItemDecoration(Activity activity, @DimenRes int dimenResId) {
        return new XLinearBuilder(activity)
                .setSpacing(ResourcesCompat.getFloat(activity.getResources(), dimenResId))
                .setShowFirstTopLine(true)
                .setShowLastLine(true)
                .setOnItemDividerDecoration(position -> new LDecoration(activity)
                        .setAroundEdge(true, true, true, true))
                .build();
    }

    public static void showSuccessToast(Context context, String message) {
        //Toastie.success(context, message, Toast.LENGTH_SHORT).show();
        Typeface typeface = ResourcesCompat.getFont(context, R.font.inter);
        Toastie.allCustom(context).setIcon(com.mrntlu.toastie.R.drawable.ic_check_circle_black_24dp)
                .setMessage(message)
                .setCardBackgroundColor(com.mrntlu.toastie.R.color.successColor)
                .setTypeFace(typeface)
                .createToast(Toast.LENGTH_SHORT).show();
    }

    public static void showWarningToast(Context context, String message) {
        //  Toastie.warning(context, message, Toast.LENGTH_SHORT).show();
        Typeface typeface = ResourcesCompat.getFont(context, R.font.inter);
        Toastie.allCustom(context).setIcon(com.mrntlu.toastie.R.drawable.ic_warning_white_24dp)
                .setMessage(message)
                .setCardBackgroundColor(com.mrntlu.toastie.R.color.warningColor)
                .setTypeFace(typeface)
                .createToast(Toast.LENGTH_SHORT).show();
    }

    public static void showNoNetwork(Context context) {
        //   Toastie.warning(context, context.getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
        Typeface typeface = ResourcesCompat.getFont(context, R.font.inter);
        Toastie.allCustom(context).setIcon(com.mrntlu.toastie.R.drawable.ic_warning_white_24dp)
                .setMessage(context.getString(R.string.no_internet))
                .setCardBackgroundColor(com.mrntlu.toastie.R.color.warningColor)
                .setTypeFace(typeface)
                .createToast(Toast.LENGTH_SHORT).show();
    }

    //this is called when response from api does not
    public static void showSomethingWrong(Context context) {
        //  Toastie.warning(context, context.getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
        //Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/custom.ttf");
        Typeface typeface = ResourcesCompat.getFont(context, R.font.inter);
        Toastie.allCustom(context).setIcon(com.mrntlu.toastie.R.drawable.ic_warning_white_24dp)
                .setMessage(context.getString(R.string.something_wrong))
                .setCardBackgroundColor(com.mrntlu.toastie.R.color.warningColor)
                .setTypeFace(typeface)
                .createToast(Toast.LENGTH_SHORT).show();
    }

    public static String convertHtml(String content, boolean isRtl) {
        String direction = isRtl ? "rtl" : "ltr";
        return "<html dir=" + direction + "><head>"
                + "<style type=\"text/css\">@font-face {font-family: MyFont;src: url(\"file:///android_res/font/inter.ttf\")}body{font-family: MyFont;color: #3F3D56;font-size:16px;margin-left:0px;line-height:1.3}"
                + "</style></head>"
                + "<body>"
                + content
                + "</body></html>";
    }

    public static String html2text(String html) {
        if (html != null) {
            return Jsoup.parse(html).text();
        } else {
            return "";
        }
    }

    public static void addTransaction(Activity mActivity, String planId, String paymentId, String paymentGateway) {
        Intent intent = new Intent(mActivity, AddTransactionActivity.class);
        intent.putExtra("planId", planId);
        intent.putExtra("paymentId", paymentId);
        intent.putExtra("paymentGateway", paymentGateway);
        mActivity.startActivity(intent);
    }

    public static void changeStateInfo(Activity mActivity, ImageView ivState, TextView tvState, TextView tvStateMsg) {
        if (NetworkUtils.isConnected(mActivity)) {
            ivState.setImageResource(R.drawable.img_state_error);
            tvState.setText(mActivity.getString(R.string.no_error));
            tvStateMsg.setText(mActivity.getString(R.string.something_wrong));
        } else {
            ivState.setImageResource(R.drawable.img_state_internet);
            tvState.setText(mActivity.getString(R.string.no_internet));
            tvStateMsg.setText(mActivity.getString(R.string.no_internet_msg));
        }
    }

    public static void changeStateInfo(Activity mActivity, int state, ImageView ivState, TextView tvState, TextView tvStateMsg) {
        switch (state) {
            case State.STATE_NO_INTERNET:
                ivState.setImageResource(R.drawable.img_state_internet);
                tvState.setText(mActivity.getString(R.string.no_internet));
                tvStateMsg.setText(mActivity.getString(R.string.no_internet_msg));
                break;
            case State.STATE_ERROR_IN_API:
                ivState.setImageResource(R.drawable.img_state_error);
                tvState.setText(mActivity.getString(R.string.no_error));
                tvStateMsg.setText(mActivity.getString(R.string.something_wrong));
                break;
            case State.STATE_EMPTY:
                ivState.setImageResource(R.drawable.img_state_empty);
                tvState.setText(mActivity.getString(R.string.no_data));
                tvStateMsg.setText(mActivity.getString(R.string.no_data_msg));
                break;
            case State.STATE_NO_GATEWAY:
                ivState.setImageResource(R.drawable.img_state_empty);
                tvState.setText(mActivity.getString(R.string.payment));
                tvStateMsg.setText(mActivity.getString(R.string.no_gateway));
                break;
            case State.STATE_PAYMENT_TOKEN_ERROR:
                ivState.setImageResource(R.drawable.img_state_error);
                tvState.setText(mActivity.getString(R.string.payment));
                tvStateMsg.setText(mActivity.getString(R.string.payment_token_error));
                break;
        }
    }

    public static String viewFormat(String salary) {
        Number number = Double.parseDouble(salary);
        char[] suffix = {' ', 'k', 'M', 'B', 'T', 'P', 'E'};
        long numValue = number.longValue();
        int value = (int) Math.floor(Math.log10(numValue));
        int base = value / 3;
        if (value >= 3 && base < suffix.length) {
            return new DecimalFormat().format(numValue / Math.pow(10, base * 3)) + suffix[base]; //"#0.00"
        } else {
            return new DecimalFormat().format(numValue);
        }
    }
}
