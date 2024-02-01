package com.example.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobs.AboutUsActivity;
import com.example.jobs.PageActivity;
import com.example.jobs.R;
import com.example.jobs.databinding.RowPageBinding;
import com.example.model.Page;

import java.util.ArrayList;

public class PageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Activity activity;
    ArrayList<Page> listPages;

    public PageAdapter(Activity activity, ArrayList<Page> listPages) {
        this.activity = activity;
        this.listPages = listPages;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(RowPageBinding.inflate(activity.getLayoutInflater()));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder) holder;
        Page page = listPages.get(holder.getBindingAdapterPosition());
        viewHolder.viewBinding.tvPage.setText(page.getPageTitle());
        viewHolder.viewBinding.rlPage.setOnClickListener(view -> {
            Intent intent;
            if (page.getPageId().equals("1")) {
                intent = new Intent(activity, AboutUsActivity.class);
            } else {
                intent = new Intent(activity, PageActivity.class);
                intent.putExtra("pageId", page.getPageId());
                intent.putExtra("pageTitle", page.getPageTitle());
            }
            activity.startActivity(intent);
        });
        switch (page.getPageId()) {
            case "1":
            default:
                viewHolder.viewBinding.ivPage.setImageResource(R.drawable.ic_setting_about);
                break;
            case "2":
                viewHolder.viewBinding.ivPage.setImageResource(R.drawable.ic_setting_terms);
                break;
            case "3":
                viewHolder.viewBinding.ivPage.setImageResource(R.drawable.ic_setting_privacy);
                break;
            case "4":
                viewHolder.viewBinding.ivPage.setImageResource(R.drawable.ic_setting_ins);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return listPages.size();
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        RowPageBinding viewBinding;

        public ViewHolder(@NonNull RowPageBinding viewBinding) {
            super(viewBinding.getRoot());
            this.viewBinding = viewBinding;
        }
    }
}
