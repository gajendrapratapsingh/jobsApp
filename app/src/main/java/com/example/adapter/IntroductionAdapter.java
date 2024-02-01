package com.example.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.fragment.IntroductionFragment;

public class IntroductionAdapter extends FragmentStateAdapter {

    int itemCount;

    public IntroductionAdapter(@NonNull FragmentActivity fragmentActivity, int itemCount) {
        super(fragmentActivity);
        this.itemCount = itemCount;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return IntroductionFragment.newInstance(position);
    }

    @Override
    public int getItemCount() {
        return itemCount;
    }
}
