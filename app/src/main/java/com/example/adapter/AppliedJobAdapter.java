package com.example.adapter;

import android.app.Activity;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobs.R;
import com.example.jobs.databinding.RowAppliedJobBinding;
import com.example.model.Job;
import com.example.util.GlideApp;
import com.example.util.PopUpAds;


public class AppliedJobAdapter extends WrapperRecyclerAdapter<Job> {

    public AppliedJobAdapter(Activity activity) {
        super(activity);
    }

    @Override
    protected RecyclerView.ViewHolder onCreateVH(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(RowAppliedJobBinding.inflate(activity.getLayoutInflater()));
    }

    @Override
    protected void onBindVH(@NonNull RecyclerView.ViewHolder holder, int position, Job item) {
        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.viewBinding.tvJobTitle.setText(item.getJobTitle());
        viewHolder.viewBinding.tvJobLocation.setText(item.getJobLocation());
        viewHolder.viewBinding.tvJobAppliedDate.setText(activity.getString(R.string.applied_on, item.getJobAppliedDate()));
        if (item.isJobAppliedSeen()) {
            viewHolder.viewBinding.tvAppliedStatus.setText(activity.getString(R.string.job_seen));
            viewHolder.viewBinding.tvAppliedStatus.setTextColor(activity.getResources().getColor(R.color.colorPrimary));
            viewHolder.viewBinding.cvAppliedStatus.setCardBackgroundColor(activity.getResources().getColor(R.color.job_skill_bg));
        } else {
            viewHolder.viewBinding.tvAppliedStatus.setText(activity.getString(R.string.job_applied));
            viewHolder.viewBinding.tvAppliedStatus.setTextColor(activity.getResources().getColor(R.color.job_apply));
            viewHolder.viewBinding.cvAppliedStatus.setCardBackgroundColor(activity.getResources().getColor(R.color.job_applied_bg));
        }
        GlideApp.with(activity).load(item.getJobImage()).into(viewHolder.viewBinding.ivJob);
        viewHolder.viewBinding.rootView.setOnClickListener(view ->
                PopUpAds.showInterstitialAds(activity, holder.getBindingAdapterPosition(), item, rvOnClickListener)
        );
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        RowAppliedJobBinding viewBinding;

        public ViewHolder(@NonNull RowAppliedJobBinding viewBinding) {
            super(viewBinding.getRoot());
            this.viewBinding = viewBinding;
        }
    }
}
