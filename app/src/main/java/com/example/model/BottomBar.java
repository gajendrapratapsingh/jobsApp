package com.example.model;

import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jobs.databinding.ActivityMainBinding;

import java.util.ArrayList;

public class BottomBar {
    FrameLayout frameLayout;
    TextView textView;
    ImageView imageView;

    public FrameLayout getFrameLayout() {
        return frameLayout;
    }

    public TextView getTextView() {
        return textView;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public BottomBar(FrameLayout frameLayout, TextView textView, ImageView imageView) {
        this.frameLayout = frameLayout;
        this.textView = textView;
        this.imageView = imageView;
    }

    public static ArrayList<BottomBar> listOfBottomBarItem(ActivityMainBinding mainBinding) {
        ArrayList<BottomBar> list = new ArrayList<>();
        list.add(new BottomBar(mainBinding.bottomBar.flHome, mainBinding.bottomBar.tvHome, mainBinding.bottomBar.ivHome));
        list.add(new BottomBar(mainBinding.bottomBar.flLatest, mainBinding.bottomBar.tvLatestJob, mainBinding.bottomBar.ivLatestJob));
        list.add(new BottomBar(mainBinding.bottomBar.flCategory, mainBinding.bottomBar.tvCategory, mainBinding.bottomBar.ivCategory));
        list.add(new BottomBar(mainBinding.bottomBar.flSetting, mainBinding.bottomBar.tvSetting, mainBinding.bottomBar.ivSetting));
        return list;
    }
}
