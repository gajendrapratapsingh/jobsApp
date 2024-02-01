package com.example.jobs;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;

import com.example.model.User;
import com.facebook.FacebookSdk;
import com.google.gson.Gson;
import com.onesignal.OSNotificationOpenedResult;
import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;

public class MyApplication extends Application {

    private static MyApplication mInstance;
    public SharedPreferences preferences;
    public String prefName = "jobApp";


    public MyApplication() {
        mInstance = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FacebookSdk.sdkInitialize(getApplicationContext());
        OneSignal.initWithContext(this);
        OneSignal.setAppId(getString(R.string.onesignal_app_id));
        OneSignal.setNotificationOpenedHandler(new ExampleNotificationOpenedHandler());
        mInstance = this;
    }

    public static synchronized MyApplication getInstance() {
        return mInstance;
    }

    public boolean isLogin() {
        preferences = this.getSharedPreferences(prefName, 0);
        return preferences.getBoolean("IsLoggedIn", false);
    }

    public void setLogin(boolean flag) {
        preferences = this.getSharedPreferences(prefName, 0);
        Editor editor = preferences.edit();
        editor.putBoolean("IsLoggedIn", flag);
        editor.apply();
        if (!flag) {
            setLoginInfo(new User());
        }
    }

    public void setLoginInfo(User user) {
        Gson gson = new Gson();
        String loginJson = gson.toJson(user);
        preferences = this.getSharedPreferences(prefName, 0);
        Editor editor = preferences.edit();
        editor.putString("loginInfo", loginJson);
        editor.apply();
    }

    public User getLoginInfo() {
        preferences = this.getSharedPreferences(prefName, 0);
        Gson gson = new Gson();
        String loginInfo = preferences.getString("loginInfo", gson.toJson(new User())); // default all tag blank
        return gson.fromJson(loginInfo, User.class);
    }

    public String getLoginType() {
        preferences = this.getSharedPreferences(prefName, 0);
        return preferences.getString("login_type", "");
    }

    public void setLoginType(String type) {
        preferences = this.getSharedPreferences(prefName, 0);
        Editor editor = preferences.edit();
        editor.putString("login_type", type);
        editor.apply();
    }

    public void setRemember(boolean flag) {
        preferences = this.getSharedPreferences(prefName, 0);
        Editor editor = preferences.edit();
        editor.putBoolean("IsLoggedRemember", flag);
        editor.apply();
    }

    public boolean isRemember() {
        preferences = this.getSharedPreferences(prefName, 0);
        return preferences.getBoolean("IsLoggedRemember", false);
    }

    public void saveRemember(String email, String password) {
        preferences = this.getSharedPreferences(prefName, 0);
        Editor editor = preferences.edit();
        editor.putString("remember_email", email);
        editor.putString("remember_password", password);
        editor.apply();
    }

    public String getRememberEmail() {
        preferences = this.getSharedPreferences(prefName, 0);
        return preferences.getString("remember_email", "");
    }

    public String getRememberPassword() {
        preferences = this.getSharedPreferences(prefName, 0);
        return preferences.getString("remember_password", "");
    }

    public void setIntroSeen(boolean flag) {
        preferences = this.getSharedPreferences(prefName, 0);
        Editor editor = preferences.edit();
        editor.putBoolean("IsIntroSeen", flag);
        editor.apply();
    }

    public boolean isIntroSeen() {
        preferences = this.getSharedPreferences(prefName, 0);
        return preferences.getBoolean("IsIntroSeen", false);
    }

    public void setProvider(boolean flag) {
        preferences = this.getSharedPreferences(prefName, 0);
        Editor editor = preferences.edit();
        editor.putBoolean("IsProvider", flag);
        editor.apply();
    }

    public boolean isProvider() {
        preferences = this.getSharedPreferences(prefName, 0);
        return preferences.getBoolean("IsProvider", false);
    }

    public void setNotification(boolean flag) {
        preferences = this.getSharedPreferences(prefName, 0);
        Editor editor = preferences.edit();
        editor.putBoolean("IsNotification", flag);
        editor.apply();
    }

    public boolean isNotification() {
        preferences = this.getSharedPreferences(prefName, 0);
        return preferences.getBoolean("IsNotification", true);
    }

    private class ExampleNotificationOpenedHandler implements OneSignal.OSNotificationOpenedHandler {
        @Override
        public void notificationOpened(OSNotificationOpenedResult result) {
            JSONObject data = result.getNotification().getAdditionalData();
            String customKey, isExternalLink;
            try {
                customKey = data.getString("post_id");
                isExternalLink = data.getString("external_link");
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (!customKey.isEmpty()) {
                    intent.setClass(MyApplication.this, isProvider() ? SplashActivity.class : JobDetailActivity.class);
                    intent.putExtra("jobId", customKey);
                    intent.putExtra("isNotification", true);
                } else if (!isExternalLink.equals("false")) {
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(isExternalLink));
                } else {
                    intent.setClass(MyApplication.this, SplashActivity.class);
                }
                startActivity(intent);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
