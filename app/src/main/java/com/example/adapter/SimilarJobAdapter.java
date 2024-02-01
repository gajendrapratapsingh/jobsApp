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
import com.example.util.RvOnClickListener;
import com.example.util.SaveJob;

import java.util.ArrayList;


public class SimilarJobAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Activity activity;
    ArrayList<Job> listJobs;
    RvOnClickListener<Job> rvOnClickListener;

    public SimilarJobAdapter(Activity activity, ArrayList<Job> list) {
        this.activity = activity;
        this.listJobs = list;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(RowLatestJobBinding.inflate(activity.getLayoutInflater()));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder) holder;
        Job item = listJobs.get(position);
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
//            item.setJobSaved(isSave);
//            notifyItemChanged(position, item);
        }));
    }

    @Override
    public int getItemCount() {
        return listJobs.size();
    }

    public void setOnItemClickListener(RvOnClickListener<Job> clickListener) {
        this.rvOnClickListener = clickListener;
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        RowLatestJobBinding viewBinding;

        public ViewHolder(@NonNull RowLatestJobBinding viewBinding) {
            super(viewBinding.getRoot());
            this.viewBinding = viewBinding;
        }
    }
}
