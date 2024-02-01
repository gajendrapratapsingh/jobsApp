package com.example.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobs.R;
import com.example.jobs.databinding.RowPaymentGatewayBinding;
import com.example.model.Gateway;
import com.example.util.GlideApp;
import com.example.util.RvOnClickListener;

import java.util.ArrayList;

public class GatewayAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Activity activity;
    ArrayList<Gateway> listGateway;
    RvOnClickListener<Gateway> clickListener;
    private int row_index = -1;

    public GatewayAdapter(Activity activity, ArrayList<Gateway> listGateway) {
        this.activity = activity;
        this.listGateway = listGateway;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(RowPaymentGatewayBinding.inflate(activity.getLayoutInflater()));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder) holder;
        Gateway gateway = listGateway.get(position);
        viewHolder.viewBinding.tvPaymentGatewayName.setText(gateway.getGatewayName());
        GlideApp.with(activity).load(gateway.getGatewayLogo()).into(viewHolder.viewBinding.ivPaymentGateway);
        viewHolder.viewBinding.rootView.setOnClickListener(view -> clickListener.onItemClick(gateway, position));

        if (row_index > -1) {
            if (row_index == position) {
                viewHolder.viewBinding.rootView.setCardBackgroundColor(activity.getResources().getColor(R.color.colorPrimary));
                viewHolder.viewBinding.rdCheck.setBackgroundResource(R.drawable.plan_circle_select);
                viewHolder.viewBinding.tvPaymentGatewayName.setTextColor(activity.getResources().getColor(R.color.white));
            } else {
                viewHolder.viewBinding.rootView.setCardBackgroundColor(activity.getResources().getColor(R.color.white));
                viewHolder.viewBinding.rdCheck.setBackgroundResource(R.drawable.plan_circle_unselect);
                viewHolder.viewBinding.tvPaymentGatewayName.setTextColor(activity.getResources().getColor(R.color.title));
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
        return listGateway.size();
    }

    public void setOnItemClickListener(RvOnClickListener<Gateway> clickListener) {
        this.clickListener = clickListener;
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        RowPaymentGatewayBinding viewBinding;

        public ViewHolder(@NonNull RowPaymentGatewayBinding viewBinding) {
            super(viewBinding.getRoot());
            this.viewBinding = viewBinding;
        }
    }
}
