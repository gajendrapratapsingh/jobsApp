package com.example.util;

import java.io.Serializable;

public class AppUtil implements Serializable {

    public static boolean isBanner = false, isInterstitial = false, isNative = false;
    public static String adNetworkType;
    public static String bannerId, interstitialId, nativeId, appIdOrPublisherId;
    public static int interstitialAdCount, nativeAdCount;

    public static final String admobAd = "1", startAppAd = "2", facebookAd = "3", appLovinMaxAd = "4", wortiseAd = "5";
    public static int adCountIncrement = 0;

    public static boolean isAppUpdate = false, isAppUpdateCancel = false;
    public static int appUpdateVersion;
    public static String appUpdateUrl, appUpdateDesc;
    public static String currencyCode = "$";

    public static final String USER_TYPE_USER = "User", USER_TYPE_COMPANY = "Company";
    public static final String PLAN_TYPE_SEEKER = "Seeker", PLAN_TYPE_PROVIDER = "Provider";

}
