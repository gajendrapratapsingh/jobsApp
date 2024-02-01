package com.example.jobs;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.jobs.databinding.ActivityPaymentBinding;
import com.example.util.GeneralUtils;
import com.example.util.IsRTL;
import com.example.util.StatusBarUtil;
import com.example.util.TryAgainListener;

public class PaymentBaseActivity extends AppCompatActivity {

    ProgressDialog pDialog;
    ActivityPaymentBinding viewBinding;
    TryAgainListener tryAgainListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityPaymentBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        StatusBarUtil.setFullScreen(this, viewBinding.getRoot());
        IsRTL.ifSupported(this);
        pDialog = new ProgressDialog(this, R.style.AlertDialogStyle);

        viewBinding.toolbar.includeImage.getRoot().setVisibility(View.GONE);
        viewBinding.toolbar.tvName.setText(getString(R.string.payment));
        viewBinding.toolbar.fabBack.setOnClickListener(view -> onBackPressed());
    }

    public void showProgress(boolean show) {
        if (show) {
            viewBinding.progressBar.setVisibility(View.VISIBLE);
        } else {
            viewBinding.progressBar.setVisibility(View.GONE);
        }
    }

    public void showErrorState(int state) {
        viewBinding.progressBar.setVisibility(View.GONE);
        viewBinding.incState.errorState.setVisibility(View.VISIBLE);
        GeneralUtils.changeStateInfo(this, state, viewBinding.incState.ivState, viewBinding.incState.tvError, viewBinding.incState.tvErrorMsg);
        viewBinding.incState.btnTryAgain.setOnClickListener(view -> {
            viewBinding.incState.errorState.setVisibility(View.GONE);
            if (tryAgainListener != null) {
                tryAgainListener.onTryAgain();
            }
        });
    }

    public void setOnTryAgainListener(TryAgainListener listener) {
        this.tryAgainListener = listener;
    }

    public void showProgressDialog() {
        pDialog.setMessage(getString(R.string.loading));
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();
    }

    public void dismissProgressDialog() {
        if (pDialog != null && pDialog.isShowing()) {
            pDialog.dismiss();
        }
    }

    public void showError(String title, String message) {
        new AlertDialog.Builder(PaymentBaseActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setIcon(R.mipmap.ic_launcher)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {

                })
                .setNegativeButton(android.R.string.no, (dialog, which) -> {
                    // do nothing
                })
                .show();
    }
}
