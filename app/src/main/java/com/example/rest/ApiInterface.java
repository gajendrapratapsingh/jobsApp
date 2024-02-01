package com.example.rest;

import com.example.callback.BraintreeCheckoutCallback;
import com.example.callback.CategoryListCallback;
import com.example.callback.CommonCallback;
import com.example.callback.CompanyListCallback;
import com.example.callback.FilterDataCallback;
import com.example.callback.GatewayListCallback;
import com.example.callback.HomeCallback;
import com.example.callback.JobDetailCallback;
import com.example.callback.JobListCallback;
import com.example.callback.JobSaveCallback;
import com.example.callback.LoginCallback;
import com.example.callback.PaymentTokenCallback;
import com.example.callback.PlanListCallback;
import com.example.callback.ProfileCallback;
import com.google.gson.JsonObject;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface ApiInterface {

    @POST("home")
    @FormUrlEncoded
    Call<HomeCallback> getHome(@Field("data") String data);

    @POST("category")
    @FormUrlEncoded
    Call<CategoryListCallback> getCategoryList(@Field("data") String data, @Query("page") int page);

    @POST("recently_view")
    @FormUrlEncoded
    Call<JobListCallback> getRecentJobList(@Field("data") String data, @Query("page") int page);

    @POST("recommend_jobs")
    @FormUrlEncoded
    Call<JobListCallback> getRecommendJobList(@Field("data") String data, @Query("page") int page);

    @POST("latest_jobs")
    @FormUrlEncoded
    Call<JobListCallback> getLatestJobList(@Field("data") String data);

    @POST("jobs_by_cat")
    @FormUrlEncoded
    Call<JobListCallback> getJobListByCat(@Field("data") String data, @Query("page") int page);

    @POST("jobs_by_company")
    @FormUrlEncoded
    Call<JobListCallback> getJobListByCompany(@Field("data") String data, @Query("page") int page);

    @POST("jobs_search")
    @FormUrlEncoded
    Call<JobListCallback> getJobSearch(@Field("data") String data, @Query("page") int page);

    @POST("cat_loc_comp_list")
    @FormUrlEncoded
    Call<FilterDataCallback> getFilterData(@Field("data") String data);

    @POST("jobs_filter")
    @FormUrlEncoded
    Call<JobListCallback> getJobFilter(@Field("data") String data, @Query("page") int page);

    @POST("company_list")
    @FormUrlEncoded
    Call<CompanyListCallback> getCompanyList(@Field("data") String data, @Query("page") int page);

    @POST("user_favourite_post_list")
    @FormUrlEncoded
    Call<JobListCallback> getSavedJobList(@Field("data") String data, @Query("page") int page);

    @POST("user_applied_job_list")
    @FormUrlEncoded
    Call<JobListCallback> getAppliedJobList(@Field("data") String data, @Query("page") int page);

    @POST("app_details")
    @FormUrlEncoded
    Call<JsonObject> getAppDetail(@Field("data") String data);

    @POST("signup")
    @FormUrlEncoded
    Call<CommonCallback> userRegister(@Field("data") String data);

    @POST("login")
    @FormUrlEncoded
    Call<LoginCallback> userLogin(@Field("data") String data);

    @POST("social_login")
    @FormUrlEncoded
    Call<LoginCallback> userSocialLogin(@Field("data") String data);

    @POST("forgot_password")
    @FormUrlEncoded
    Call<CommonCallback> userForgotPassword(@Field("data") String data);

    @POST("profile")
    @FormUrlEncoded
    Call<ProfileCallback> userProfile(@Field("data") String data);

    @POST("profile_update")
    @Multipart
    Call<LoginCallback> userProfileUpdate(@Part("data") RequestBody data, @Part MultipartBody.Part profilePart, @Part MultipartBody.Part resumePart);

    @POST("password_update")
    @FormUrlEncoded
    Call<CommonCallback> userChangePassword(@Field("data") String data);

    @POST("user_applied_job")
    @FormUrlEncoded
    Call<CommonCallback> userApplyJob(@Field("data") String data);

    @POST("post_favourite")
    @FormUrlEncoded
    Call<JobSaveCallback> userSaveJob(@Field("data") String data);

    @POST("jobs_details")
    @FormUrlEncoded
    Call<JobDetailCallback> getJobDetail(@Field("data") String data);

    @POST("subscription_plan")
    @FormUrlEncoded
    Call<PlanListCallback> getPlanList(@Field("data") String data);

    @POST("payment_settings")
    @FormUrlEncoded
    Call<GatewayListCallback> getPaymentGatewayList(@Field("data") String data);

    @POST("get_braintree_token")
    @FormUrlEncoded
    Call<PaymentTokenCallback> getBraintreeToken(@Field("data") String data);

    @POST("braintree_checkout")
    @FormUrlEncoded
    Call<BraintreeCheckoutCallback> braintreeCheckout(@Field("data") String data);

    @POST("stripe_token_get")
    @FormUrlEncoded
    Call<PaymentTokenCallback> getStripeToken(@Field("data") String data);

    @POST("razorpay_order_id_get")
    @FormUrlEncoded
    Call<PaymentTokenCallback> getRazorPayOrderId(@Field("data") String data);

    @POST("get_payu_hash")
    @FormUrlEncoded
    Call<PaymentTokenCallback> getPayUHash(@Field("data") String data);

    @POST("transaction_add")
    @FormUrlEncoded
    Call<CommonCallback> addTransaction(@Field("data") String data);

    @POST("user_reports")
    @FormUrlEncoded
    Call<CommonCallback> reportJob(@Field("data") String data);

}
