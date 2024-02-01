package com.example.jobs;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.callback.CommonCallback;
import com.example.jobs.databinding.ActivityRegisterBinding;
import com.example.rest.ApiInterface;
import com.example.rest.RestAdapter;
import com.example.util.API;
import com.example.util.AppUtil;
import com.example.util.GeneralUtils;
import com.example.util.IsRTL;
import com.example.util.NetworkUtils;
import com.example.util.StatusBarUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.Length;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Password;
import com.tuyenmonkey.textdecorator.TextDecorator;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity implements Validator.ValidationListener {

    ActivityRegisterBinding viewBinding;
    Validator validator;
    @NotEmpty
    EditText etName;
    @Email
    EditText etEmail;
    @Password
    EditText etPassword;
    @Length(max = 14, min = 10, message = "Enter valid Phone Number")
    EditText etPhone;
    ProgressDialog pDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        StatusBarUtil.setFullScreen(this, viewBinding.getRoot());
        IsRTL.ifSupported(this);
        pDialog = new ProgressDialog(this, R.style.AlertDialogStyle);
        etEmail = viewBinding.etEmail;
        etName = viewBinding.etName;
        etPassword = viewBinding.etPassword;
        etPhone = viewBinding.etPhone;

        viewBinding.btnRegister.setOnClickListener(view -> validator.validate());

        validator = new Validator(this);
        validator.setValidationListener(this);

        setPrivacy();
        setAlreadyLogin();
    }

    private void callRegister() {
        showProgressDialog();

        String name = etName.getText().toString();
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();
        String phone = etPhone.getText().toString();

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("name", name);
        jsObj.addProperty("email", email);
        jsObj.addProperty("password", password);
        jsObj.addProperty("phone", phone);
        jsObj.addProperty("usertype", viewBinding.rbJobProvider.isChecked() ? AppUtil.USER_TYPE_COMPANY : AppUtil.USER_TYPE_USER);

        ApiInterface apiInterface = RestAdapter.createAPI();
        Call<CommonCallback> callback = apiInterface.userRegister(API.toBase64(jsObj.toString()));
        callback.enqueue(new Callback<CommonCallback>() {
            @Override
            public void onResponse(@NonNull Call<CommonCallback> call, @NonNull Response<CommonCallback> response) {
                dismissProgressDialog();
                CommonCallback resp = response.body();
                if (resp != null) {
                    if (resp.success == 1) {
                        GeneralUtils.showSuccessToast(RegisterActivity.this, resp.message);
                        finish();
                    } else {
                        GeneralUtils.showWarningToast(RegisterActivity.this, resp.message);
                        etEmail.setText("");
                        etEmail.requestFocus();
                    }
                } else {
                    GeneralUtils.showSomethingWrong(RegisterActivity.this);
                }
            }

            @Override
            public void onFailure(@NonNull Call<CommonCallback> call, @NonNull Throwable t) {
                if (!call.isCanceled()) {
                    dismissProgressDialog();
                    GeneralUtils.showSomethingWrong(RegisterActivity.this);
                }
            }
        });
    }

    @Override
    public void onValidationSucceeded() {
        if (NetworkUtils.isConnected(this)) {
            if (viewBinding.cbPrivacy.isChecked()) {
                callRegister();
            } else {
                GeneralUtils.showWarningToast(this, getString(R.string.please_accept));
            }
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

    private void setPrivacy() {
        TextDecorator
                .decorate(viewBinding.tvPrivacy, getString(R.string.msg_by_signing_up, getString(R.string.privacy_policy)))
                .makeTextClickable((view, text) -> {
                    Intent intent = new Intent(RegisterActivity.this, PageActivity.class);
                    intent.putExtra("pageId", "3");
                    intent.putExtra("pageTitle", getString(R.string.privacy_policy));
                    startActivity(intent);
                }, true, getString(R.string.privacy_policy))
                .setTextColor(R.color.colorPrimary, getString(R.string.privacy_policy))
                .build();
    }

    private void setAlreadyLogin() {
        TextDecorator
                .decorate(viewBinding.tvSignIn, getString(R.string.msg_already_have_an_account, getString(R.string.lbl_already_login)))
                .makeTextClickable((view, text) -> {
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }, true, getString(R.string.lbl_already_login))
                .setTextColor(R.color.colorPrimary, getString(R.string.lbl_already_login))
                .build();
    }
}
