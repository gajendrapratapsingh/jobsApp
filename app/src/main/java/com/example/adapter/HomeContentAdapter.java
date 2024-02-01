package com.example.adapter;

import android.app.Activity;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobs.R;
import com.example.jobs.databinding.RowCategoryBinding;
import com.example.jobs.databinding.RowCompanyBinding;
import com.example.jobs.databinding.RowLatestJobBinding;
import com.example.jobs.databinding.RowRecentJobBinding;
import com.example.jobs.databinding.RowRecommendedJobBinding;
import com.example.model.Category;
import com.example.model.Company;
import com.example.model.Home;
import com.example.model.Job;
import com.example.util.AppUtil;
import com.example.util.ApplyJob;
import com.example.util.GeneralUtils;
import com.example.util.GlideApp;
import com.example.util.PopUpAds;
import com.example.util.RvOnClickListener;
import com.example.util.SaveJob;

import java.util.ArrayList;


public class HomeContentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Activity activity;
    ArrayList<Object> listHomeContent;
    int homeType;
    private final int[] categoryColors, companyColors;
    RvOnClickListener<Object> rvOnClickListener;

    public HomeContentAdapter(Activity activity, ArrayList<Object> list, int homeType) {
        this.activity = activity;
        this.listHomeContent = list;
        this.homeType = homeType;
        categoryColors = activity.getResources().getIntArray(R.array.category_colors);
        companyColors = activity.getResources().getIntArray(R.array.company_colors);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case Home.RECENT:
                return new RecentRowHolder(RowRecentJobBinding.inflate(activity.getLayoutInflater()));
            case Home.CATEGORY:
                return new CategoryRowHolder(RowCategoryBinding.inflate(activity.getLayoutInflater()));
            case Home.RECOMMEND:
                return new RecommendRowHolder(RowRecommendedJobBinding.inflate(activity.getLayoutInflater()));
            case Home.COMPANY:
                return new CompanyRowHolder(RowCompanyBinding.inflate(activity.getLayoutInflater()));
            case Home.LATEST:
            default:
                return new LatestRowHolder(RowLatestJobBinding.inflate(activity.getLayoutInflater()));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == Home.RECENT) {
            RecentRowHolder recentRowHolder = (RecentRowHolder) holder;
            Job item = (Job) listHomeContent.get(position);
            recentRowHolder.viewBinding.tvJobTitle.setText(item.getJobTitle());
            recentRowHolder.viewBinding.tvJobLocation.setText(item.getJobAddress());
            GlideApp.with(activity).load(item.getJobImage()).into(recentRowHolder.viewBinding.ivJob);

            recentRowHolder.viewBinding.rootView.setOnClickListener(view ->
                    PopUpAds.showInterstitialAds(activity, holder.getBindingAdapterPosition(), item, rvOnClickListener)
            );
            recentRowHolder.viewBinding.ivSaveJob.setImageResource(item.isJobSaved() ? R.drawable.ic_bookmark_select : R.drawable.ic_bookmark);
            recentRowHolder.viewBinding.ivSaveJob.setOnClickListener(view -> new SaveJob(activity, item.getJobId(), isSave -> {
                item.setJobSaved(isSave);
                notifyItemChanged(position, item);
            }));

        } else if (holder.getItemViewType() == Home.CATEGORY) {
            CategoryRowHolder categoryRowHolder = (CategoryRowHolder) holder;
            Category item = (Category) listHomeContent.get(position);
            categoryRowHolder.viewBinding.tvCategoryTitle.setText(item.getCategoryTitle());
            categoryRowHolder.viewBinding.tvNumJobs.setText(activity.getString(R.string.num_plus_job, item.getCategoryNoOfJobs()));
            GlideApp.with(activity).load(item.getCategoryImage()).into(categoryRowHolder.viewBinding.ivCategory);
            categoryRowHolder.viewBinding.cardView.setCardBackgroundColor(categoryColors[position % categoryColors.length]);
            categoryRowHolder.viewBinding.rootView.setOnClickListener(view ->
                    PopUpAds.showInterstitialAds(activity, holder.getBindingAdapterPosition(), item, rvOnClickListener)
            );

        } else if (holder.getItemViewType() == Home.RECOMMEND) {
            RecommendRowHolder recommendRowHolder = (RecommendRowHolder) holder;
            Job item = (Job) listHomeContent.get(position);
            recommendRowHolder.viewBinding.tvJobTitle.setText(item.getJobTitle());
            recommendRowHolder.viewBinding.tvJobLocation.setText(item.getJobLocation());
            GlideApp.with(activity).load(item.getJobImage()).into(recommendRowHolder.viewBinding.ivJob);
            recommendRowHolder.viewBinding.tvSalary.setText(activity.getString(R.string.salary_currency, AppUtil.currencyCode, GeneralUtils.viewFormat(item.getJobSalary())));
            recommendRowHolder.viewBinding.rootView.setOnClickListener(view ->
                    PopUpAds.showInterstitialAds(activity, holder.getBindingAdapterPosition(), item, rvOnClickListener)
            );

        } else if (holder.getItemViewType() == Home.COMPANY) {
            CompanyRowHolder companyRowHolder = (CompanyRowHolder) holder;
            Company item = (Company) listHomeContent.get(position);
            companyRowHolder.viewBinding.tvCompanyTitle.setText(item.getCompanyTitle());
            companyRowHolder.viewBinding.tvCompanyLocation.setText(item.getCompanyLocation());
            GlideApp.with(activity).load(item.getCompanyImage()).into(companyRowHolder.viewBinding.ivCompany);
            companyRowHolder.viewBinding.rootView.setCardBackgroundColor(companyColors[position % companyColors.length]);
            companyRowHolder.viewBinding.rootView.setOnClickListener(view ->
                    PopUpAds.showInterstitialAds(activity, holder.getBindingAdapterPosition(), item, rvOnClickListener)
            );

        } else if (holder.getItemViewType() == Home.LATEST) {
            LatestRowHolder latestRowHolder = (LatestRowHolder) holder;
            Job item = (Job) listHomeContent.get(position);
            latestRowHolder.viewBinding.tvJobTitle.setText(item.getJobTitle());
            latestRowHolder.viewBinding.tvJobLocation.setText(item.getJobLocation());
            latestRowHolder.viewBinding.tvJobDesignation.setText(item.getJobDesignation());
            latestRowHolder.viewBinding.tvSalary.setText(activity.getString(R.string.salary_currency, AppUtil.currencyCode, GeneralUtils.viewFormat(item.getJobSalary())));
            GlideApp.with(activity).load(item.getJobImage()).into(latestRowHolder.viewBinding.ivJob);
            latestRowHolder.viewBinding.rvSkill.setHasFixedSize(true);
            SkillAdapter skillAdapter = new SkillAdapter(activity, GeneralUtils.getSkills(item.getJobSkills()), false);
            latestRowHolder.viewBinding.rvSkill.setAdapter(skillAdapter);

            latestRowHolder.viewBinding.rootView.setOnClickListener(view ->
                    PopUpAds.showInterstitialAds(activity, holder.getBindingAdapterPosition(), item, rvOnClickListener)
            );
            latestRowHolder.viewBinding.btnJobApply.setOnClickListener(view -> new ApplyJob(activity, item.getJobId()));
            latestRowHolder.viewBinding.ivSaveJob.setImageResource(item.isJobSaved() ? R.drawable.ic_bookmark_select : R.drawable.ic_bookmark);
            latestRowHolder.viewBinding.ivSaveJob.setOnClickListener(view -> new SaveJob(activity, item.getJobId(), isSave -> {
//                item.setJobSaved(isSave);
//                notifyItemChanged(position, item);
            }));

        }
    }

    @Override
    public int getItemCount() {
        return listHomeContent.size();
    }

    @Override
    public int getItemViewType(int position) {
        return homeType;
    }

    public void setOnItemClickListener(RvOnClickListener<Object> clickListener) {
        this.rvOnClickListener = clickListener;
    }

    private static class CategoryRowHolder extends RecyclerView.ViewHolder {
        RowCategoryBinding viewBinding;

        public CategoryRowHolder(@NonNull RowCategoryBinding viewBinding) {
            super(viewBinding.getRoot());
            this.viewBinding = viewBinding;
        }
    }

    private static class RecentRowHolder extends RecyclerView.ViewHolder {
        RowRecentJobBinding viewBinding;

        public RecentRowHolder(@NonNull RowRecentJobBinding viewBinding) {
            super(viewBinding.getRoot());
            this.viewBinding = viewBinding;
        }
    }

    private static class LatestRowHolder extends RecyclerView.ViewHolder {
        RowLatestJobBinding viewBinding;

        public LatestRowHolder(@NonNull RowLatestJobBinding viewBinding) {
            super(viewBinding.getRoot());
            this.viewBinding = viewBinding;
        }
    }

    private static class CompanyRowHolder extends RecyclerView.ViewHolder {
        RowCompanyBinding viewBinding;

        public CompanyRowHolder(@NonNull RowCompanyBinding viewBinding) {
            super(viewBinding.getRoot());
            this.viewBinding = viewBinding;
        }
    }

    private static class RecommendRowHolder extends RecyclerView.ViewHolder {
        RowRecommendedJobBinding viewBinding;

        public RecommendRowHolder(@NonNull RowRecommendedJobBinding viewBinding) {
            super(viewBinding.getRoot());
            this.viewBinding = viewBinding;
        }
    }
}
