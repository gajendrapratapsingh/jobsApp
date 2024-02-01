package com.example.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobs.databinding.RowFilterSelectBinding;
import com.example.model.Filter;
import com.example.util.RvOnClickListener;

import java.util.ArrayList;


public class FilterSelectAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Activity activity;
    ArrayList<Filter> listFilter;
    RvOnClickListener<Filter> clickListener;

    public FilterSelectAdapter(Activity activity) {
        this.activity = activity;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setList(ArrayList<Filter> list) {
        this.listFilter = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(RowFilterSelectBinding.inflate(activity.getLayoutInflater()));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder) holder;
        Filter item = listFilter.get(position);
        viewHolder.viewBinding.tvName.setText(item.getFilterName());
        viewHolder.viewBinding.ivClose.setOnClickListener(view -> clickListener.onItemClick(item, position));
    }

    @Override
    public int getItemCount() {
        return (null != listFilter ? listFilter.size() : 0);
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        RowFilterSelectBinding viewBinding;

        public ViewHolder(@NonNull RowFilterSelectBinding viewBinding) {
            super(viewBinding.getRoot());
            this.viewBinding = viewBinding;
        }
    }

    public void setOnItemClickListener(RvOnClickListener<Filter> clickListener) {
        this.clickListener = clickListener;
    }
}
