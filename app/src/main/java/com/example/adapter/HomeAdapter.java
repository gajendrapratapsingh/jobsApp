package com.example.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobs.JobDetailActivity;
import com.example.jobs.JobListByCatActivity;
import com.example.jobs.JobListByCompanyActivity;
import com.example.jobs.R;
import com.example.jobs.databinding.RowHomeItemBinding;
import com.example.model.Category;
import com.example.model.Company;
import com.example.model.Home;
import com.example.model.Job;
import com.example.util.GeneralUtils;
import com.example.util.LinearLayoutPagerManager;
import com.example.util.RvOnClickListener;

import java.util.ArrayList;


public class HomeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Activity activity;
    ArrayList<Home> listHome;
    RvOnClickListener<Home> rvOnClickListener;
    private final RecyclerView.RecycledViewPool mRvViewPool = new RecyclerView.RecycledViewPool();

    public HomeAdapter(Activity activity, ArrayList<Home> list) {
        this.activity = activity;
        this.listHome = list;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(RowHomeItemBinding.inflate(activity.getLayoutInflater()));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder) holder;
        Home item = listHome.get(position);
        viewHolder.viewBinding.tvHomeTitle.setText(item.getHomeTitle());
        viewHolder.viewBinding.rvContent.setHasFixedSize(true);
        viewHolder.viewBinding.rvContent.setNestedScrollingEnabled(false);
        viewHolder.viewBinding.rvContent.setRecycledViewPool(mRvViewPool);
        if (item.getHomeType() == Home.CATEGORY) {
            viewHolder.viewBinding.rvContent.addItemDecoration(GeneralUtils.gridItemDecoration(activity, R.dimen.item_space));
            viewHolder.viewBinding.rvContent.setLayoutManager(new GridLayoutManager(activity, 2));
            viewHolder.viewBinding.llContent.setBackgroundColor(activity.getResources().getColor(R.color.white_60));
            viewHolder.viewBinding.vCateFakeTop.setVisibility(View.VISIBLE);
            viewHolder.viewBinding.vCateFakeTopOne.setVisibility(View.VISIBLE);
            viewHolder.viewBinding.vCateFakeBottom.setVisibility(View.VISIBLE);
            viewHolder.viewBinding.vCateFakeBottomOne.setVisibility(View.VISIBLE);
        } else {
            viewHolder.viewBinding.rvContent.addItemDecoration(GeneralUtils.listItemDecoration(activity, R.dimen.item_space));
            viewHolder.viewBinding.rvContent.setLayoutManager(new LinearLayoutPagerManager(activity, LinearLayoutManager.HORIZONTAL, false, setItemPerPage(item.getHomeType())));
            viewHolder.viewBinding.vCateFakeTop.setVisibility(View.GONE);
            viewHolder.viewBinding.vCateFakeTopOne.setVisibility(View.GONE);
            viewHolder.viewBinding.vCateFakeBottom.setVisibility(View.GONE);
            viewHolder.viewBinding.vCateFakeBottomOne.setVisibility(View.GONE);
        }

        HomeContentAdapter homeContentAdapter = new HomeContentAdapter(activity, item.getItemHomeContents(), item.getHomeType());
        viewHolder.viewBinding.rvContent.setAdapter(homeContentAdapter);
        homeContentAdapter.setOnItemClickListener((itemContent, positionContent) -> {
            Intent intent = new Intent();
            switch (item.getHomeType()) {
                case Home.CATEGORY:
                    Category category = (Category) itemContent;
                    intent.putExtra("categoryId", category.getCategoryId());
                    intent.putExtra("categoryName", category.getCategoryTitle());
                    intent.setClass(activity, JobListByCatActivity.class);
                    break;
                case Home.COMPANY:
                    Company company = (Company) itemContent;
                    intent.putExtra("companyId", company.getCompanyId());
                    intent.putExtra("companyName", company.getCompanyTitle());
                    intent.setClass(activity, JobListByCompanyActivity.class);
                    break;
                case Home.RECENT:
                case Home.LATEST:
                case Home.RECOMMEND:
                    Job job = (Job) itemContent;
                    intent.putExtra("jobId", job.getJobId());
                    intent.setClass(activity, JobDetailActivity.class);
                    break;
            }
            activity.startActivity(intent);
        });
        viewHolder.viewBinding.tvHomeTitleViewAll.setOnClickListener(view ->
                rvOnClickListener.onItemClick(item, holder.getBindingAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return listHome.size();
    }

    public void setOnItemClickListener(RvOnClickListener<Home> clickListener) {
        this.rvOnClickListener = clickListener;
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        RowHomeItemBinding viewBinding;

        public ViewHolder(@NonNull RowHomeItemBinding viewBinding) {
            super(viewBinding.getRoot());
            this.viewBinding = viewBinding;
        }
    }

    private double setItemPerPage(int homeType) {
        switch (homeType) {
            case Home.LATEST:
            case Home.RECENT:
            default:
                return 1.3;
            case Home.COMPANY:
            case Home.RECOMMEND:
                return 2.5;
        }
    }
}
