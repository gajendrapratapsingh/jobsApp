package com.example.jobs;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.callback.LoginCallback;
import com.example.callback.ProfileCallback;
import com.example.jobs.databinding.ActivityEditProfileBinding;
import com.example.jobs.databinding.LayoutAddSkillSheetBinding;
import com.example.model.Profile;
import com.example.rest.ApiInterface;
import com.example.rest.RestAdapter;
import com.example.util.API;
import com.example.util.Events;
import com.example.util.GeneralUtils;
import com.example.util.GlideApp;
import com.example.util.GlobalBus;
import com.example.util.IsRTL;
import com.example.util.NetworkUtils;
import com.example.util.Picker;
import com.example.util.StatusBarUtil;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.Length;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity implements Validator.ValidationListener {

    ActivityEditProfileBinding viewBinding;
    Validator validator;
    MyApplication myApplication;
    boolean isMale = true;
    ProgressDialog pDialog;
    @NotEmpty
    EditText etName;
    @Email
    EditText etEmail;
    @Length(max = 14, min = 10, message = "Enter valid Phone Number")
    EditText etPhone;
    Picker picker = new Picker(this);
    boolean isProfile = false, isResume = false, isFromPayment = false;
    File fileProfile, fileResume;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        StatusBarUtil.setFullScreenWithLightStatusBars(this, viewBinding.getRoot(), false);
        IsRTL.ifSupported(this);
        pDialog = new ProgressDialog(this, R.style.AlertDialogStyle);
        myApplication = MyApplication.getInstance();
        etEmail = viewBinding.etEmail;
        etName = viewBinding.etName;
        etPhone = viewBinding.etPhone;
        Intent intent = getIntent();
        if (intent.hasExtra("isFromPayment")) {
            isFromPayment = intent.getBooleanExtra("isFromPayment", false);
        }
        viewBinding.fabBack.setOnClickListener(view -> onBackPressed());
        viewBinding.btnSave.setOnClickListener(view -> validator.validate());

        onRequest();

        validator = new Validator(this);
        validator.setValidationListener(this);

    }

    private void onRequest() {
        if (NetworkUtils.isConnected(EditProfileActivity.this)) {
            getProfile();
        } else {
            showErrorState();
        }
    }

    private void getProfile() {
        showProgress(true);
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("user_id", myApplication.getLoginInfo().getUserId());
        ApiInterface apiInterface = RestAdapter.createAPI();
        Call<ProfileCallback> callback = apiInterface.userProfile(API.toBase64(jsObj.toString()));
        callback.enqueue(new Callback<ProfileCallback>() {
            @Override
            public void onResponse(@NonNull Call<ProfileCallback> call, @NonNull Response<ProfileCallback> response) {
                showProgress(false);
                ProfileCallback resp = response.body();
                if (resp != null && resp.success == 1) {
                    setDataToView(resp.profile);
                } else {
                    showErrorState();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProfileCallback> call, @NonNull Throwable t) {
                if (!call.isCanceled()) {
                    showProgress(false);
                    showErrorState();
                }
            }
        });
    }

    private void setDataToView(Profile profile) {
        viewBinding.etName.setText(profile.getUserName());
        viewBinding.etEmail.setText(profile.getUserEmail());
        viewBinding.etPhone.setText(profile.getUserPhone());
        viewBinding.etCity.setText(profile.getUserCity());
        viewBinding.etAddress.setText(profile.getUserAddress());
        viewBinding.etCurrentCompany.setText(profile.getUserCurrentCompany());
        viewBinding.etExperience.setText(profile.getUserExp());
        viewBinding.etCity.setText(profile.getUserCity());
        viewBinding.tvSaveJobCount.setText(String.valueOf(profile.getSavedJobCount()));
        viewBinding.tvAppliedJobCount.setText(String.valueOf(profile.getAppliedJobCount()));
        if (!profile.getUserDob().isEmpty()) {
            viewBinding.etDob.setText(profile.getUserDob());
            setBirthDate(profile.getUserDob());
        }

        viewBinding.tvName.setText(profile.getUserName());
        viewBinding.tvCurrentExp.setText(getString(R.string.current_exp, profile.getUserExp().isEmpty()
                ? getString(R.string.n_a) : profile.getUserExp()));
        if (!profile.getUserCurrentCompany().isEmpty()) {
            viewBinding.tvCurrentCompany.setText(profile.getUserCurrentCompany());
        }
        if (!profile.getUserSkills().isEmpty()) {
            viewBinding.tvSkill.setText(profile.getUserSkills());
        }
        if (!profile.getUserResume().isEmpty()) {
            viewBinding.tvResumeFileName.setText(profile.getUserResume().substring(profile.getUserResume().lastIndexOf("/") + 1));
        }

        GlideApp.with(EditProfileActivity.this).load(profile.getUserImage()).into(viewBinding.ivUserImage);

        if (profile.getUserGender() != null) {
            isMale = profile.getUserGender().equals("male");
        }
        setGender(isMale);
        viewBinding.llGenderMale.setOnClickListener(view -> setGender(true));
        viewBinding.llGenderFemale.setOnClickListener(view -> setGender(false));

        viewBinding.btnAddSkill.setOnClickListener(view -> addSkillSheet());
        viewBinding.llSaveJob.setOnClickListener(view -> {
            Intent intent = new Intent(this, SavedJobActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
        viewBinding.llAppliedJob.setOnClickListener(view -> {
            Intent intent = new Intent(this, AppliedJobActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
        viewBinding.ivUpload.setOnClickListener(view -> chooseProfilePic());
        viewBinding.rlUploadResume.setOnClickListener(view -> chooseResume());
    }

    private void setBirthDate(String dob) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(Objects.requireNonNull(sdf.parse(dob)));
            viewBinding.etDob.setDate(calendar);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void setGender(boolean isMale) {
        this.isMale = isMale;
        viewBinding.llGenderMale.setBackgroundColor(isMale ?
                getResources().getColor(R.color.profile_gender_selected_bg) : getResources().getColor(android.R.color.transparent));
        viewBinding.rdMale.setBackgroundResource(isMale ? R.drawable.profile_gender_select : R.drawable.profile_gender_unselect);

        viewBinding.llGenderFemale.setBackgroundColor(isMale ?
                getResources().getColor(android.R.color.transparent) : getResources().getColor(R.color.profile_gender_selected_bg));
        viewBinding.rdFemale.setBackgroundResource(isMale ? R.drawable.profile_gender_unselect : R.drawable.profile_gender_select);
    }

    private void addSkillSheet() {
        BottomSheetDialog sheetDialog = new BottomSheetDialog(EditProfileActivity.this, R.style.BottomSheetDialog);
        LayoutAddSkillSheetBinding sheetBinding = LayoutAddSkillSheetBinding.inflate(getLayoutInflater());
        sheetDialog.setContentView(sheetBinding.getRoot());
        boolean isRTL = Boolean.parseBoolean(getString(R.string.isRTL));
        if (isRTL) {
            sheetDialog.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
        String defaultSkill = viewBinding.tvSkill.getText().toString();
        if (!defaultSkill.equals(getString(R.string.your_skill))) {
            sheetBinding.etSkill.setText(defaultSkill);
        }

        sheetBinding.ivClose.setOnClickListener(view -> sheetDialog.dismiss());
        sheetBinding.btnSubmit.setOnClickListener(view -> {
            String skill = sheetBinding.etSkill.getText().toString();
            if (skill.isEmpty()) {
                GeneralUtils.showWarningToast(EditProfileActivity.this, getString(R.string.enter_skill));
            } else {
                viewBinding.tvSkill.setText(skill);
            }

            sheetDialog.dismiss();
        });

        sheetDialog.show();
    }

    private void showProgress(boolean show) {
        if (show) {
            viewBinding.progressBar.setVisibility(View.VISIBLE);
            viewBinding.parent.setVisibility(View.GONE);
        } else {
            viewBinding.progressBar.setVisibility(View.GONE);
            viewBinding.parent.setVisibility(View.VISIBLE);
        }
    }

    private void showErrorState() {
        viewBinding.progressBar.setVisibility(View.GONE);
        viewBinding.parent.setVisibility(View.GONE);
        viewBinding.incState.errorState.setVisibility(View.VISIBLE);
        GeneralUtils.changeStateInfo(this, viewBinding.incState.ivState, viewBinding.incState.tvError, viewBinding.incState.tvErrorMsg);
        viewBinding.incState.btnTryAgain.setOnClickListener(view -> {
            viewBinding.incState.errorState.setVisibility(View.GONE);
            onRequest();
        });
    }

    @Override
    public void onValidationSucceeded() {
        if (NetworkUtils.isConnected(this)) {
            callUpdateProfile();
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

    private void callUpdateProfile() {
        showProgressDialog();

        String name = etName.getText().toString();
        String email = etEmail.getText().toString();
        String phone = etPhone.getText().toString();
        String city = viewBinding.etCity.getText().toString();
        String address = viewBinding.etAddress.getText().toString();
        String dob = Objects.requireNonNull(viewBinding.etDob.getText()).toString();
        String gender = isMale ? "male" : "female";
        String currentCompany = viewBinding.etCurrentCompany.getText().toString();
        String skills = viewBinding.tvSkill.getText().toString();
        String experience = viewBinding.etExperience.getText().toString();


        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("user_id", myApplication.getLoginInfo().getUserId());
        jsObj.addProperty("name", name);
        jsObj.addProperty("email", email);
        jsObj.addProperty("password", "");
        jsObj.addProperty("phone", phone);
        jsObj.addProperty("city", city);
        jsObj.addProperty("address", address);
        jsObj.addProperty("date_of_birth", dob);
        jsObj.addProperty("gender", gender);
        jsObj.addProperty("current_company", currentCompany);
        jsObj.addProperty("skills", skills);
        jsObj.addProperty("experience", experience);
        MediaType contentType = MediaType.parse("multipart/form-data");
        MultipartBody.Part profilepart = null, resumePart = null;
        if (isProfile && fileProfile != null) {
            profilepart = MultipartBody.Part.createFormData("user_image", fileProfile.getName(), RequestBody.create(fileProfile, contentType));
        }
        if (isResume && fileResume != null) {
            resumePart = MultipartBody.Part.createFormData("resume", fileResume.getName(), RequestBody.create(fileResume, contentType));
        }
        RequestBody dataBody = RequestBody.create(API.toBase64(jsObj.toString()), contentType);

        ApiInterface apiInterface = RestAdapter.createAPI();
        Call<LoginCallback> callback = apiInterface.userProfileUpdate(dataBody, profilepart, resumePart);
        callback.enqueue(new Callback<LoginCallback>() {
            @Override
            public void onResponse(@NonNull Call<LoginCallback> call, @NonNull Response<LoginCallback> response) {
                dismissProgressDialog();
                LoginCallback resp = response.body();
                if (resp != null) {
                    if (resp.success == 1) {
                        myApplication.setLoginInfo(resp.user);
                        GeneralUtils.showSuccessToast(EditProfileActivity.this, resp.message);
                        GlobalBus.getBus().post(new Events.ProfileUpdate());
                        onBackPressed();
                    } else {
                        GeneralUtils.showWarningToast(EditProfileActivity.this, resp.message);
                    }
                } else {
                    GeneralUtils.showSomethingWrong(EditProfileActivity.this);
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginCallback> call, @NonNull Throwable t) {
                if (!call.isCanceled()) {
                    dismissProgressDialog();
                    GeneralUtils.showSomethingWrong(EditProfileActivity.this);
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

    private void chooseProfilePic() {
        picker.pickGalleryOnly(file -> {
            if (file != null) {
                isProfile = true;
                fileProfile = file;
                GlideApp.with(EditProfileActivity.this).load(file).into(viewBinding.ivUserImage);
            } else {
                GeneralUtils.showSomethingWrong(EditProfileActivity.this);
            }
        });
    }

    private void chooseResume() {
        picker.pickFile(file -> {
            if (file != null) {
                isResume = true;
                fileResume = file;
                String path = file.getAbsolutePath();
                viewBinding.tvResumeFileName.setText(path.substring(path.lastIndexOf("/") + 1));
            } else {
                GeneralUtils.showSomethingWrong(EditProfileActivity.this);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (isProfile && fileProfile != null) {
            if (fileProfile.delete()) Log.d("--", "file");
        }
        if (isResume && fileResume != null) {
            if (fileResume.delete()) Log.d("--", "file");
        }

        if (isFromPayment) {
            Intent intent = new Intent();
            setResult(1187, intent);
            finish();
        } else {
            super.onBackPressed();
        }
    }
}
