package com.example.jobs;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.adapter.IntroductionAdapter;
import com.example.jobs.databinding.ActivityIntroductionBinding;
import com.example.util.Events;
import com.example.util.GlobalBus;
import com.example.util.IsRTL;
import com.example.util.StatusBarUtil;
import com.example.util.ZoomOutPageTransformer;

import org.greenrobot.eventbus.Subscribe;

public class IntroductionActivity extends AppCompatActivity {

    ActivityIntroductionBinding viewBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityIntroductionBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        StatusBarUtil.setFullScreen(this, viewBinding.getRoot());
        IsRTL.ifSupported(this);
        GlobalBus.getBus().register(this);
        int totalIntro = getResources().getStringArray(R.array.intro_title).length;
        viewBinding.vpIntro.setAdapter(new IntroductionAdapter(this, totalIntro));
        viewBinding.vpIntro.setPageTransformer(new ZoomOutPageTransformer());
        MyApplication.getInstance().setIntroSeen(true);
    }

    @Subscribe
    public void onEvent(Events.Intro intro) {
        viewBinding.vpIntro.setCurrentItem(getItem());
    }

    private int getItem() {
        return viewBinding.vpIntro.getCurrentItem() + 1;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GlobalBus.getBus().unregister(this);
    }
}
