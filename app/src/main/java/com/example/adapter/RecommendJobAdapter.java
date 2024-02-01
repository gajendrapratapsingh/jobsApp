package com.example.adapter;

import android.app.Activity;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobs.R;
import com.example.jobs.databinding.RowRecommendedJobBinding;
import com.example.model.Job;
import com.example.util.AppUtil;
import com.example.util.GeneralUtils;
import com.example.util.GlideApp;
import com.example.util.PopUpAds;


public class RecommendJobAdapter extends WrapperRecyclerAdapter<Job> {

    public RecommendJobAdapter(Activity activity) {
        super(activity);
    }

    @Override
    protected RecyclerView.ViewHolder onCreateVH(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(RowRecommendedJobBinding.inflate(activity.getLayoutInflater()));
    }

    @Override
    protected void onBindVH(@NonNull RecyclerView.ViewHolder holder, int position, Job item) {
        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.viewBinding.tvJobTitle.setText(item.getJobTitle());
        viewHolder.viewBinding.tvJobLocation.setText(item.getJobLocation());
        viewHolder.viewBinding.tvSalary.setText(activity.getString(R.string.salary_currency, AppUtil.currencyCode, GeneralUtils.viewFormat(item.getJobSalary())));
        GlideApp.with(activity).load(item.getJobImage()).into(viewHolder.viewBinding.ivJob);
        viewHolder.viewBinding.rootView.setOnClickListener(view ->
                PopUpAds.showInterstitialAds(activity, holder.getBindingAdapterPosition(), item, rvOnClickListener)
        );
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        RowRecommendedJobBinding viewBinding;

        public ViewHolder(@NonNull RowRecommendedJobBinding viewBinding) {
            super(viewBinding.getRoot());
            this.viewBinding = viewBinding;
        }
    }
}
