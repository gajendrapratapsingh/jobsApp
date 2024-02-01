package com.example.jobs;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.callback.CommonCallback;
import com.example.jobs.databinding.ActivityForgotPasswordBinding;
import com.example.rest.ApiInterface;
import com.example.rest.RestAdapter;
import com.example.util.API;
import com.example.util.GeneralUtils;
import com.example.util.IsRTL;
import com.example.util.NetworkUtils;
import com.example.util.StatusBarUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Email;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity implements Validator.ValidationListener {

    ActivityForgotPasswordBinding viewBinding;
    Validator validator;
    @Email
    EditText etEmail;
    ProgressDialog pDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        StatusBarUtil.setFullScreen(this, viewBinding.getRoot());
        IsRTL.ifSupported(this);
        pDialog = new ProgressDialog(this, R.style.AlertDialogStyle);
        etEmail = viewBinding.etEmail;

        viewBinding.btnSendEmail.setOnClickListener(view -> validator.validate());
        viewBinding.toolbar.includeImage.getRoot().setVisibility(View.GONE);
        viewBinding.toolbar.tvName.setText(getString(R.string.lbl_forgot_password));
        viewBinding.toolbar.fabBack.setOnClickListener(view -> onBackPressed());
        validator = new Validator(this);
        validator.setValidationListener(this);

    }

    private void callForgotPassword() {
        showProgressDialog();

        String email = etEmail.getText().toString();

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("email", email);

        ApiInterface apiInterface = RestAdapter.createAPI();
        Call<CommonCallback> callback = apiInterface.userForgotPassword(API.toBase64(jsObj.toString()));
        callback.enqueue(new Callback<CommonCallback>() {
            @Override
            public void onResponse(@NonNull Call<CommonCallback> call, @NonNull Response<CommonCallback> response) {
                dismissProgressDialog();
                CommonCallback resp = response.body();
                if (resp != null) {
                    if (resp.success == 1) {
                        GeneralUtils.showSuccessToast(ForgotPasswordActivity.this, resp.message);
                    } else {
                        GeneralUtils.showWarningToast(ForgotPasswordActivity.this, resp.message);
                        etEmail.setText("");
                        etEmail.requestFocus();
                    }
                } else {
                    GeneralUtils.showSomethingWrong(ForgotPasswordActivity.this);
                }
            }

            @Override
            public void onFailure(@NonNull Call<CommonCallback> call, @NonNull Throwable t) {
                if (!call.isCanceled()) {
                    dismissProgressDialog();
                    GeneralUtils.showSomethingWrong(ForgotPasswordActivity.this);
                }
            }
        });
    }

    @Override
    public void onValidationSucceeded() {
        if (NetworkUtils.isConnected(this)) {
            callForgotPassword();
        } else {
            GeneralUtils.showNoNetwork(this);
        }
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(this);
            if (view instanceof EditText) {
                ((EditText) view).setError(message);
                view.requestFocus();
            } else {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void showProgressDialog() {
        pDialog.setMessage(getString(R.string.loading));
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();
    }

    private void dismissProgressDialog() {
        if (pDialog != null && pDialog.isShowing()) {
            pDialog.dismiss();
        }
    }
}
