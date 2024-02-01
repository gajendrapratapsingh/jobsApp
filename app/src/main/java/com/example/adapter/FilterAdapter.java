package com.example.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobs.databinding.RowFilterBinding;
import com.example.model.Filter;

import java.util.ArrayList;


public class FilterAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Activity activity;
    ArrayList<Filter> listFilter;

    public FilterAdapter(Activity activity) {
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
        return new ViewHolder(RowFilterBinding.inflate(activity.getLayoutInflater()));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder) holder;
        Filter item = listFilter.get(position);
        viewHolder.viewBinding.cbFilter.setText(item.getFilterName());
        viewHolder.viewBinding.cbFilter.setChecked(item.isSelected());
        viewHolder.viewBinding.cbFilter.setTag(item);

        viewHolder.viewBinding.cbFilter.setOnClickListener(v -> {
            CheckBox cb = (CheckBox) v;
            Filter itemFilter = (Filter) cb.getTag();
            itemFilter.setSelected(cb.isChecked());
        });

    }

    @Override
    public int getItemCount() {
        return (null != listFilter ? listFilter.size() : 0);
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        RowFilterBinding viewBinding;

        public ViewHolder(@NonNull RowFilterBinding viewBinding) {
            super(viewBinding.getRoot());
            this.viewBinding = viewBinding;
        }
    }
}
