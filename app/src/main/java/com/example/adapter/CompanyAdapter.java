package com.example.adapter;

import android.app.Activity;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobs.R;
import com.example.jobs.databinding.RowCompanyBinding;
import com.example.model.Company;
import com.example.util.GlideApp;
import com.example.util.PopUpAds;

public class CompanyAdapter extends WrapperRecyclerAdapter<Company> {

    private final int[] colors;

    public CompanyAdapter(Activity activity) {
        super(activity);
        colors = activity.getResources().getIntArray(R.array.company_colors);
    }

    @Override
    protected RecyclerView.ViewHolder onCreateVH(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(RowCompanyBinding.inflate(activity.getLayoutInflater()));
    }

    @Override
    protected void onBindVH(@NonNull RecyclerView.ViewHolder holder, int position, Company item) {
        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.viewBinding.tvCompanyTitle.setText(item.getCompanyTitle());
        viewHolder.viewBinding.tvCompanyLocation.setText(item.getCompanyLocation());
        GlideApp.with(activity).load(item.getCompanyImage()).into(viewHolder.viewBinding.ivCompany);
        viewHolder.viewBinding.rootView.setCardBackgroundColor(colors[position % colors.length]);
        viewHolder.viewBinding.rootView.setOnClickListener(view ->
                PopUpAds.showInterstitialAds(activity, holder.getBindingAdapterPosition(), item, rvOnClickListener)
        );
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        RowCompanyBinding viewBinding;

        public ViewHolder(@NonNull RowCompanyBinding viewBinding) {
            super(viewBinding.getRoot());
            this.viewBinding = viewBinding;
        }
    }
}
