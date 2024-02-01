package com.example.fragment;

import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.jobs.LoginActivity;
import com.example.jobs.R;
import com.example.jobs.databinding.FragmentIntroductionBinding;
import com.example.util.Events;
import com.example.util.GlobalBus;

public class IntroductionFragment extends Fragment {

    FragmentIntroductionBinding viewBinding;
    int itemPosition;
    String[] listTitle, listDesc;
    TypedArray listImage;
    ImageView[] ivDots;

    public static IntroductionFragment newInstance(int layoutId) {
        IntroductionFragment pane = new IntroductionFragment();
        Bundle args = new Bundle();
        args.putInt("itemPosition", layoutId);
        pane.setArguments(args);
        return pane;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewBinding = FragmentIntroductionBinding.inflate(inflater, container, false);
        if (getArguments() != null) {
            itemPosition = getArguments().getInt("itemPosition", 0);
        }
        listTitle = getResources().getStringArray(R.array.intro_title);
        listDesc = getResources().getStringArray(R.array.intro_desc);
        listImage = getResources().obtainTypedArray(R.array.intro_image);

        viewBinding.tvIntroTitle.setText(listTitle[itemPosition]);
        viewBinding.tvIntroDesc.setText(listDesc[itemPosition]);
        viewBinding.ivImage.setImageResource(listImage.getResourceId(itemPosition, -1));

        if (isLast()) {
            viewBinding.btnNext.setText(getString(R.string.lbl_get_started));
        } else {
            viewBinding.btnNext.setText(getString(R.string.lbl_next));
        }

        viewBinding.btnNext.setOnClickListener(view -> {
            if (isLast()) {
                Intent intent = new Intent(requireActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                requireActivity().finish();
            } else {
                GlobalBus.getBus().post(new Events.Intro());
            }
        });

        setUpPageDots();
        return viewBinding.getRoot();
    }

    private boolean isLast() {
        return listTitle.length - 1 == itemPosition;
    }

    private void setUpPageDots() {
        ivDots = new ImageView[listTitle.length];
        for (int i = 0; i < ivDots.length; i++) {
            ivDots[i] = new ImageView(getActivity());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(15, 0, 15, 0);
            params.gravity = Gravity.CENTER_VERTICAL;
            ivDots[i].setLayoutParams(params);
            ivDots[i].setImageResource(itemPosition == i ? R.drawable.intro_dot_select : R.drawable.intro_dot_unselect);
            viewBinding.llPageDots.addView(ivDots[i]);
        }
    }
}
