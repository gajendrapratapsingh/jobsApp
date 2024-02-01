package com.example.adapter;

import android.app.Activity;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobs.R;
import com.example.jobs.databinding.RowRecentJobBinding;
import com.example.model.Job;
import com.example.util.GlideApp;
import com.example.util.PopUpAds;
import com.example.util.SaveJob;

public class RecentJobAdapter extends WrapperRecyclerAdapter<Job> {

    public RecentJobAdapter(Activity activity) {
        super(activity);
    }

    @Override
    protected RecyclerView.ViewHolder onCreateVH(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(RowRecentJobBinding.inflate(activity.getLayoutInflater()));
    }

    @Override
    protected void onBindVH(@NonNull RecyclerView.ViewHolder holder, int position, Job item) {
        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.viewBinding.tvJobTitle.setText(item.getJobTitle());
        viewHolder.viewBinding.tvJobLocation.setText(item.getJobAddress());
        GlideApp.with(activity).load(item.getJobImage()).into(viewHolder.viewBinding.ivJob);
        viewHolder.viewBinding.rootView.setOnClickListener(view ->
                PopUpAds.showInterstitialAds(activity, holder.getBindingAdapterPosition(), item, rvOnClickListener)
        );

        viewHolder.viewBinding.ivSaveJob.setImageResource(item.isJobSaved() ? R.drawable.ic_bookmark_select : R.drawable.ic_bookmark);
        viewHolder.viewBinding.ivSaveJob.setOnClickListener(view -> new SaveJob(activity, item.getJobId(), isSave -> {
//            item.setJobSaved(isSave);
//            notifyItemChanged(position, item);
        }));
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        RowRecentJobBinding viewBinding;

        public ViewHolder(@NonNull RowRecentJobBinding viewBinding) {
            super(viewBinding.getRoot());
            this.viewBinding = viewBinding;
        }
    }
}
