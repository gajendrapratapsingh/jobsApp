package com.example.adapter;

import android.app.Activity;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobs.R;
import com.example.jobs.databinding.RowSkillsBinding;

import java.util.ArrayList;

public class SkillAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Activity activity;
    ArrayList<String> listSkills;
    boolean isDetail;

    public SkillAdapter(Activity activity, ArrayList<String> listSkills, boolean isDetail) {
        this.activity = activity;
        this.listSkills = listSkills;
        this.isDetail = isDetail;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(RowSkillsBinding.inflate(activity.getLayoutInflater()));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.viewBinding.tvSkills.setText(listSkills.get(holder.getBindingAdapterPosition()));
        if (isDetail) {
            viewHolder.viewBinding.tvSkills.setTextColor(activity.getResources().getColor(R.color.bottom_sheet_title));
            viewHolder.viewBinding.cardView.setCardBackgroundColor(activity.getResources().getColor(R.color.job_work_day_bg));
        }
    }

    @Override
    public int getItemCount() {
        return listSkills.size();
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        RowSkillsBinding viewBinding;

        public ViewHolder(@NonNull RowSkillsBinding viewBinding) {
            super(viewBinding.getRoot());
            this.viewBinding = viewBinding;
        }
    }
}
