package com.example.jobs;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.model.Plan;
import com.example.util.GeneralUtils;
import com.flutterwave.raveandroid.RavePayActivity;
import com.flutterwave.raveandroid.RaveUiManager;
import com.flutterwave.raveandroid.rave_java_commons.RaveConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.Random;

public class FlutterWaveActivity extends PaymentBaseActivity {
    Plan plan;
    String planGateway = "Flutterwave", fwPublicKey, fwEncryptionKey;
    MyApplication myApplication;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        plan = intent.getParcelableExtra("planInfo");
        fwPublicKey = intent.getStringExtra("fwPublicKey");
        fwEncryptionKey = intent.getStringExtra("fwEncryptionKey");
        myApplication = MyApplication.getInstance();
        String payString = getString(R.string.pay_via, plan.getPlanPrice(), plan.planCurrencyCode, planGateway);
        viewBinding.btnPay.setText(payString);
        showProgress(false);
        viewBinding.btnPay.setVisibility(View.VISIBLE);
        viewBinding.btnPay.setOnClickListener(view -> startPayment());
        startPayment();
    }

    public void startPayment() {
        RaveUiManager raveUiManager = new RaveUiManager(FlutterWaveActivity.this);
        raveUiManager.setAmount(Double.parseDouble(plan.getPlanPrice()))
                .setCurrency(plan.getPlanCurrencyCode()) //NGN
                .setEmail(myApplication.getLoginInfo().getUserEmail())
                .setfName(myApplication.getLoginInfo().getUserName())
                .setPublicKey(fwPublicKey)
                .setEncryptionKey(fwEncryptionKey)
                .setTxRef(getTransactionId())
                .acceptAccountPayments(true)
                .shouldDisplayFee(true)
                .acceptCardPayments(true)
                .acceptMpesaPayments(true)
                .acceptAchPayments(true)
                .acceptGHMobileMoneyPayments(true)
                .acceptUgMobileMoneyPayments(true)
                .acceptZmMobileMoneyPayments(true)
                .acceptRwfMobileMoneyPayments(true)
                .acceptSaBankPayments(true)
                .acceptUkPayments(true)
                .acceptBankTransferPayments(true)
                .acceptUssdPayments(true)
                .acceptBarterPayments(true)
                .acceptFrancMobileMoneyPayments(true, "NG")
                .allowSaveCardFeature(true)
                .onStagingEnv(false)
                .isPreAuth(true)
                .showStagingLabel(true);
        raveUiManager.initialize();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == RaveConstants.RAVE_REQUEST_CODE && data != null) {
            String message = data.getStringExtra("response");
            if (resultCode == RavePayActivity.RESULT_SUCCESS) {
                try {
                    assert message != null;
                    JSONObject jsonObject = new JSONObject(message);
                    JSONObject jsonData = jsonObject.getJSONObject("data");
                    String status = jsonData.getString("status");
                    if (status.equals("successful")) {
                        String paymentID = jsonData.getString("flwRef");
                        GeneralUtils.addTransaction(FlutterWaveActivity.this, plan.getPlanId(), paymentID, planGateway);
                    } else {
                        showError(planGateway, "Failed " + jsonData.getString("vbvrespmessage"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else if (resultCode == RavePayActivity.RESULT_ERROR) {
                showError(planGateway, getString(R.string.payment_failed));
            } else if (resultCode == RavePayActivity.RESULT_CANCELLED) {
                showError(planGateway, getString(R.string.payment_cancel));
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public String getTransactionId() {
        Random rnd = new Random();
        int number = rnd.nextInt(999999);
        String orderId = String.format(Locale.getDefault(), "%06d", number);
        return "fW" + myApplication.getLoginInfo().getUserId() + orderId;
    }
}
