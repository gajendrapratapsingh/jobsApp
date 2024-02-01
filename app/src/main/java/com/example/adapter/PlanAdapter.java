package com.example.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobs.MyApplication;
import com.example.jobs.R;
import com.example.jobs.databinding.RowPlanBinding;
import com.example.model.Plan;
import com.example.util.RvOnClickListener;

import java.util.ArrayList;

public class PlanAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Activity activity;
    ArrayList<Plan> listPlan;
    RvOnClickListener<Plan> clickListener;
    MyApplication myApplication;
    private int row_index = -1;

    public PlanAdapter(Activity activity, ArrayList<Plan> listPlan) {
        this.activity = activity;
        this.listPlan = listPlan;
        myApplication = MyApplication.getInstance();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(RowPlanBinding.inflate(activity.getLayoutInflater()));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder) holder;
        Plan plan = listPlan.get(position);
        viewHolder.viewBinding.tvPlanName.setText(plan.getPlanName());
        viewHolder.viewBinding.tvNoApplied.setText(activity.getString(myApplication.isProvider()
                ? R.string.plan_num_job_add : R.string.plan_num_job, plan.getPlanJobLimit()));
        viewHolder.viewBinding.tvAmount.setText(activity.getString(R.string.plan_price, plan.getPlanPrice()));
        viewHolder.viewBinding.tvPlanDuration.setText(activity.getString(R.string.plan_duration, plan.getPlanDuration()));
        viewHolder.viewBinding.tvCurrency.setText(plan.getPlanCurrencyCode());
        viewHolder.viewBinding.rootView.setOnClickListener(view -> clickListener.onItemClick(plan, position));

        if (row_index > -1) {
            if (row_index == position) {
                viewHolder.viewBinding.rootView.setCardBackgroundColor(activity.getResources().getColor(R.color.colorPrimary));
                viewHolder.viewBinding.rdCheck.setBackgroundResource(R.drawable.plan_circle_select);
                viewHolder.viewBinding.tvPlanName.setTextColor(activity.getResources().getColor(R.color.white));
                viewHolder.viewBinding.tvNoApplied.setTextColor(activity.getResources().getColor(R.color.white));
                viewHolder.viewBinding.tvAmount.setTextColor(activity.getResources().getColor(R.color.white));
                viewHolder.viewBinding.tvCurrency.setTextColor(activity.getResources().getColor(R.color.white));
                viewHolder.viewBinding.tvPlanDuration.setTextColor(activity.getResources().getColor(R.color.about_sec_bg));
                viewHolder.viewBinding.cvNoApplied.setCardBackgroundColor(activity.getResources().getColor(R.color.plan_select_apply_bg));
            } else {
                viewHolder.viewBinding.rootView.setCardBackgroundColor(activity.getResources().getColor(R.color.white));
                viewHolder.viewBinding.rdCheck.setBackgroundResource(R.drawable.plan_circle_unselect);
                viewHolder.viewBinding.tvPlanName.setTextColor(activity.getResources().getColor(R.color.title));
                viewHolder.viewBinding.tvNoApplied.setTextColor(activity.getResources().getColor(R.color.colorPrimary));
                viewHolder.viewBinding.tvAmount.setTextColor(activity.getResources().getColor(R.color.title));
                viewHolder.viewBinding.tvCurrency.setTextColor(activity.getResources().getColor(R.color.title));
                viewHolder.viewBinding.tvPlanDuration.setTextColor(activity.getResources().getColor(R.color.subTitle_80));
                viewHolder.viewBinding.cvNoApplied.setCardBackgroundColor(activity.getResources().getColor(R.color.job_skill_bg));
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void select(int position) {
        row_index = position;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return listPlan.size();
    }

    public void setOnItemClickListener(RvOnClickListener<Plan> clickListener) {
        this.clickListener = clickListener;
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        RowPlanBinding viewBinding;

        public ViewHolder(@NonNull RowPlanBinding viewBinding) {
            super(viewBinding.getRoot());
            this.viewBinding = viewBinding;
        }
    }
}
