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
import androidx.core.app.ActivityCompat;

import com.example.callback.LoginCallback;
import com.example.jobs.databinding.ActivityLoginBinding;
import com.example.rest.ApiInterface;
import com.example.rest.RestAdapter;
import com.example.util.API;
import com.example.util.AppUtil;
import com.example.util.Events;
import com.example.util.GeneralUtils;
import com.example.util.GlobalBus;
import com.example.util.IsRTL;
import com.example.util.NetworkUtils;
import com.example.util.StatusBarUtil;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.Password;
import com.tuyenmonkey.textdecorator.TextDecorator;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity implements Validator.ValidationListener {

    ActivityLoginBinding viewBinding;
    Validator validator;
    @Email
    EditText etEmail;
    @Password
    EditText etPassword;
    ProgressDialog pDialog;
    MyApplication myApplication;
    CallbackManager callbackManager;
    GoogleSignInClient mGoogleSignInClient;
    static final int RC_SIGN_IN = 1000;
    boolean isLogout = false, isFromOtherScreen = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        StatusBarUtil.setFullScreen(this, viewBinding.getRoot());
        IsRTL.ifSupported(this);
        myApplication = MyApplication.getInstance();
        pDialog = new ProgressDialog(this, R.style.AlertDialogStyle);
        callbackManager = CallbackManager.Factory.create();

        Intent intent = getIntent();
        if (intent.hasExtra("isLogout")) {
            isLogout = intent.getBooleanExtra("isLogout", false);
        }
        if (intent.hasExtra("isOtherScreen")) {
            isFromOtherScreen = intent.getBooleanExtra("isOtherScreen", false);
        }

        etEmail = viewBinding.etEmail;
        etPassword = viewBinding.etPassword;

        viewBinding.btnLogin.setOnClickListener(view -> validator.validate());
        viewBinding.tvForgotPassword.setOnClickListener(view -> {
            Intent intentPassword = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            intentPassword.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intentPassword);
        });
        viewBinding.btnSkip.setOnClickListener(view -> {
            Intent intentSkip = new Intent(LoginActivity.this, MainActivity.class);
            intentSkip.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intentSkip);
            finish();
        });

        validator = new Validator(this);
        validator.setValidationListener(this);

        if (myApplication.isRemember()) {
            viewBinding.cbRememberMe.setChecked(true);
            viewBinding.etEmail.setText(myApplication.getRememberEmail());
            viewBinding.etPassword.setText(myApplication.getRememberPassword());
        }
        setPrivacy();
        setSignUp();
        initFacebookLogin();
        initGoogleLogin();
        if (isLogout) {
            logoutSocial(myApplication.getLoginType());
        }
    }

    private void callLogin() {
        showProgressDialog();

        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();

        if (viewBinding.cbRememberMe.isChecked()) {
            myApplication.setRemember(true);
            myApplication.saveRemember(email, password);
        } else {
            myApplication.setRemember(false);
        }

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("email", email);
        jsObj.addProperty("password", password);

        ApiInterface apiInterface = RestAdapter.createAPI();
        Call<LoginCallback> callback = apiInterface.userLogin(API.toBase64(jsObj.toString()));
        callback.enqueue(new Callback<LoginCallback>() {
            @Override
            public void onResponse(@NonNull Call<LoginCallback> call, @NonNull Response<LoginCallback> response) {
                dismissProgressDialog();
                LoginCallback resp = response.body();
                if (resp != null) {
                    goNextActivity("normal", resp);
                } else {
                    GeneralUtils.showSomethingWrong(LoginActivity.this);
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginCallback> call, @NonNull Throwable t) {
                if (!call.isCanceled()) {
                    dismissProgressDialog();
                    GeneralUtils.showSomethingWrong(LoginActivity.this);
                }
            }
        });
    }

    private void callSocialLogin(String authId, String name, String email, String type) {
        showProgressDialog();

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("social_id", authId);
        jsObj.addProperty("name", name);
        jsObj.addProperty("email", email);
        jsObj.addProperty("login_type", type);

        ApiInterface apiInterface = RestAdapter.createAPI();
        Call<LoginCallback> callback = apiInterface.userSocialLogin(API.toBase64(jsObj.toString()));
        callback.enqueue(new Callback<LoginCallback>() {
            @Override
            public void onResponse(@NonNull Call<LoginCallback> call, @NonNull Response<LoginCallback> response) {
                dismissProgressDialog();
                LoginCallback resp = response.body();
                if (resp != null) {
                    goNextActivity(type, resp);
                } else {
                    GeneralUtils.showSomethingWrong(LoginActivity.this);
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginCallback> call, @NonNull Throwable t) {
                if (!call.isCanceled()) {
                    dismissProgressDialog();
                    GeneralUtils.showSomethingWrong(LoginActivity.this);
                    t.printStackTrace();
                }
            }
        });
    }

    private void goNextActivity(String type, LoginCallback resp) {
        if (resp.success == 1) {
            myApplication.setLogin(true);
            myApplication.setLoginType(type);
            myApplication.setLoginInfo(resp.user);
            myApplication.setProvider(resp.user.getUserType().equals(AppUtil.USER_TYPE_COMPANY));
            GeneralUtils.showSuccessToast(LoginActivity.this, resp.message);
            GlobalBus.getBus().post(new Events.ProfileUpdate());
            if (isFromOtherScreen && !myApplication.isProvider()) {
                onBackPressed();
            } else {
                ActivityCompat.finishAffinity(LoginActivity.this);
                Intent i = new Intent(LoginActivity.this, myApplication.isProvider() ? ProviderMainActivity.class : MainActivity.class);
                startActivity(i);
                finish();
            }
        } else {
            logoutSocial(type);
            GeneralUtils.showWarningToast(LoginActivity.this, resp.message);
        }
    }

    @Override
    public void onValidationSucceeded() {
        if (NetworkUtils.isConnected(this)) {
            if (viewBinding.cbPrivacy.isChecked()) {
                callLogin();
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
                .decorate(viewBinding.tvPrivacy, getString(R.string.msg_by_signing_in, getString(R.string.privacy_policy)))
                .makeTextClickable((view, text) -> {
                    Intent intent = new Intent(LoginActivity.this, PageActivity.class);
                    intent.putExtra("pageId", "3");
                    intent.putExtra("pageTitle", getString(R.string.privacy_policy));
                    startActivity(intent);
                }, true, getString(R.string.privacy_policy))
                .setTextColor(R.color.colorPrimary, getString(R.string.privacy_policy))
                .build();
    }

    private void setSignUp() {
        TextDecorator
                .decorate(viewBinding.tvSignUp, getString(R.string.msg_don_t_have_an_account, getString(R.string.lbl_sign_up)))
                .makeTextClickable((view, text) -> {
                    Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }, true, getString(R.string.lbl_sign_up))
                .setTextColor(R.color.colorPrimary, getString(R.string.lbl_sign_up))
                .build();
    }

    private void initFacebookLogin() {

        viewBinding.llFacebook.setOnClickListener(view -> {
            if (NetworkUtils.isConnected(this)) {
                if (viewBinding.cbPrivacy.isChecked()) {
                    LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("email"));
                } else {
                    GeneralUtils.showWarningToast(this, getString(R.string.please_accept));
                }
            } else {
                GeneralUtils.showNoNetwork(this);
            }
        });

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                getDetailsFromFacebook(loginResult);
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(@NonNull FacebookException error) {
                GeneralUtils.showWarningToast(LoginActivity.this, error.toString());
            }
        });
    }

    private void getDetailsFromFacebook(LoginResult loginResult) {
        GraphRequest graphRequest = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                try {
                    String id = object.getString("id");
                    String name = object.getString("name");
                    String email = object.getString("email");
                    callSocialLogin(id, name, email, "facebook");
                } catch (JSONException e) {
                    try {
                        String id = object.getString("id");
                        String name = object.getString("name");
                        callSocialLogin(id, name, "", "facebook");
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email"); // Parameters that we ask for facebook
        graphRequest.setParameters(parameters);
        graphRequest.executeAsync();
    }

    private void initGoogleLogin() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        viewBinding.llGoogle.setOnClickListener(view -> {
            if (NetworkUtils.isConnected(this)) {
                if (viewBinding.cbPrivacy.isChecked()) {
                    Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                    startActivityForResult(signInIntent, RC_SIGN_IN);
                } else {
                    GeneralUtils.showWarningToast(this, getString(R.string.please_accept));
                }
            } else {
                GeneralUtils.showNoNetwork(this);
            }
        });
    }

    private void getDetailsFromGoogle(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            assert account != null;
            String id = account.getId();
            String name = account.getDisplayName();
            String email = account.getEmail();
            callSocialLogin(id, name, email, "google");
        } catch (ApiException e) {
            GeneralUtils.showWarningToast(LoginActivity.this, e.toString());
        }
    }

    private void logoutSocial(String type) {
        if (type.equals("google")) {
            mGoogleSignInClient.signOut()
                    .addOnCompleteListener(LoginActivity.this, task -> myApplication.setLogin(false));
        } else if (type.equals("facebook")) {
            LoginManager.getInstance().logOut();
            myApplication.setLogin(false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            getDetailsFromGoogle(task);
        }
    }
}
