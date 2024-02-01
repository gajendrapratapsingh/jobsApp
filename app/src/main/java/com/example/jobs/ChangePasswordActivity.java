package com.example.jobs;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.callback.CommonCallback;
import com.example.jobs.databinding.ActivityChangePasswordBinding;
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
import com.mobsandgeeks.saripaar.annotation.ConfirmPassword;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Password;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordActivity extends AppCompatActivity implements Validator.ValidationListener {
    ActivityChangePasswordBinding viewBinding;
    Validator validator;
    @NotEmpty
    EditText edtCurrentPassword;
    @Password
    EditText edtNewPassword;
    @ConfirmPassword
    EditText edtPasswordConfirm;
    ProgressDialog pDialog;
    MyApplication myApplication;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        StatusBarUtil.setFullScreen(this, viewBinding.getRoot());
        IsRTL.ifSupported(this);
        myApplication = MyApplication.getInstance();
        pDialog = new ProgressDialog(this, R.style.AlertDialogStyle);

        edtCurrentPassword = viewBinding.edtOldPassword;
        edtNewPassword = viewBinding.edtNewPassword;
        edtPasswordConfirm = viewBinding.edtConfirmPassword;

        viewBinding.btnChangePassword.setOnClickListener(view -> validator.validate());

        viewBinding.toolbar.includeImage.getRoot().setVisibility(View.GONE);
        viewBinding.toolbar.tvName.setText(getString(R.string.change_password));
        viewBinding.toolbar.fabBack.setOnClickListener(view -> onBackPressed());

        validator = new Validator(this);
        validator.setValidationListener(this);
    }

    private void changePassword() {
        showProgressDialog();

        String currentPassword = Objects.requireNonNull(edtCurrentPassword.getText()).toString();
        String newPassword = Objects.requireNonNull(edtNewPassword.getText()).toString();
        String confirmPassword = Objects.requireNonNull(edtPasswordConfirm.getText()).toString();
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("user_id", myApplication.getLoginInfo().getUserId());
        jsObj.addProperty("old_password", currentPassword);
        jsObj.addProperty("new_password", newPassword);
        jsObj.addProperty("confirm_password", confirmPassword);

        ApiInterface apiInterface = RestAdapter.createAPI();
        Call<CommonCallback> callback = apiInterface.userChangePassword(API.toBase64(jsObj.toString()));
        callback.enqueue(new Callback<CommonCallback>() {
            @Override
            public void onResponse(@NotNull Call<CommonCallback> call, @NotNull Response<CommonCallback> response) {
                dismissProgressDialog();
                CommonCallback resp = response.body();
                if (resp != null && resp.success == 1) {
                    GeneralUtils.showSuccessToast(ChangePasswordActivity.this, resp.message);
                    finish();
                } else if (resp != null && resp.success == 0) {
                    GeneralUtils.showWarningToast(ChangePasswordActivity.this, resp.message);
                } else {
                    GeneralUtils.showSomethingWrong(ChangePasswordActivity.this);
                }
            }

            @Override
            public void onFailure(@NotNull Call<CommonCallback> call, @NotNull Throwable t) {
                if (!call.isCanceled()) {
                    dismissProgressDialog();
                    GeneralUtils.showSomethingWrong(ChangePasswordActivity.this);
                }
            }
        });
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

    @Override
    public void onValidationSucceeded() {
        if (NetworkUtils.isConnected(this)) {
            changePassword();
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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
