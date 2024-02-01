package com.example.adapter;

import android.app.Activity;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobs.R;
import com.example.jobs.databinding.RowLatestJobBinding;
import com.example.model.Job;
import com.example.util.AppUtil;
import com.example.util.ApplyJob;
import com.example.util.GeneralUtils;
import com.example.util.GlideApp;
import com.example.util.PopUpAds;
import com.example.util.SaveJob;


public class SavedJobAdapter extends WrapperRecyclerAdapter<Job> {

    public SavedJobAdapter(Activity activity) {
        super(activity);
    }

    @Override
    protected RecyclerView.ViewHolder onCreateVH(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(RowLatestJobBinding.inflate(activity.getLayoutInflater()));
    }

    @Override
    protected void onBindVH(@NonNull RecyclerView.ViewHolder holder, int position, Job item) {
        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.viewBinding.tvJobTitle.setText(item.getJobTitle());
        viewHolder.viewBinding.tvJobLocation.setText(item.getJobLocation());
        viewHolder.viewBinding.tvJobDesignation.setText(item.getJobDesignation());
        viewHolder.viewBinding.tvSalary.setText(activity.getString(R.string.salary_currency, AppUtil.currencyCode, GeneralUtils.viewFormat(item.getJobSalary())));
        GlideApp.with(activity).load(item.getJobImage()).into(viewHolder.viewBinding.ivJob);
        viewHolder.viewBinding.rvSkill.setHasFixedSize(true);
        SkillAdapter skillAdapter = new SkillAdapter(activity, GeneralUtils.getSkills(item.getJobSkills()), false);
        viewHolder.viewBinding.rvSkill.setAdapter(skillAdapter);
        viewHolder.viewBinding.rootView.setOnClickListener(view ->
                PopUpAds.showInterstitialAds(activity, holder.getBindingAdapterPosition(), item, rvOnClickListener)
        );
        viewHolder.viewBinding.btnJobApply.setOnClickListener(view -> new ApplyJob(activity, item.getJobId()));

        viewHolder.viewBinding.ivSaveJob.setImageResource(item.isJobSaved() ? R.drawable.ic_bookmark_select : R.drawable.ic_bookmark);
        viewHolder.viewBinding.ivSaveJob.setOnClickListener(view -> new SaveJob(activity, item.getJobId(), isSave -> {
        }));
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        RowLatestJobBinding viewBinding;

        public ViewHolder(@NonNull RowLatestJobBinding viewBinding) {
            super(viewBinding.getRoot());
            this.viewBinding = viewBinding;
        }
    }
}
