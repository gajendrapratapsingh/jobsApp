package com.example.adapter;

import android.app.Activity;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobs.R;
import com.example.jobs.databinding.RowCategoryBinding;
import com.example.model.Category;
import com.example.util.GlideApp;
import com.example.util.PopUpAds;

public class CategoryAdapter extends WrapperRecyclerAdapter<Category> {

    private final int[] colors;

    public CategoryAdapter(Activity activity) {
        super(activity);
        colors = activity.getResources().getIntArray(R.array.category_colors);
    }

    @Override
    protected RecyclerView.ViewHolder onCreateVH(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(RowCategoryBinding.inflate(activity.getLayoutInflater()));
    }

    @Override
    protected void onBindVH(@NonNull RecyclerView.ViewHolder holder, int position, Category item) {
        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.viewBinding.tvCategoryTitle.setText(item.getCategoryTitle());
        viewHolder.viewBinding.tvNumJobs.setText(activity.getString(R.string.num_plus_job, item.getCategoryNoOfJobs()));
        GlideApp.with(activity).load(item.getCategoryImage()).into(viewHolder.viewBinding.ivCategory);
        viewHolder.viewBinding.cardView.setCardBackgroundColor(colors[position % colors.length]);
        viewHolder.viewBinding.rootView.setOnClickListener(view ->
                PopUpAds.showInterstitialAds(activity, holder.getBindingAdapterPosition(), item, rvOnClickListener)
        );
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        RowCategoryBinding viewBinding;

        public ViewHolder(@NonNull RowCategoryBinding viewBinding) {
            super(viewBinding.getRoot());
            this.viewBinding = viewBinding;
        }
    }
}
