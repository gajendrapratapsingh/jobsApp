package com.example.jobs;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.jobs.databinding.ActivityPaystackBinding;
import com.example.model.Plan;
import com.example.util.GeneralUtils;
import com.example.util.IsRTL;
import com.example.util.NetworkUtils;
import com.example.util.StatusBarUtil;
import com.tenbis.library.consts.CardType;
import com.tenbis.library.listeners.OnCreditCardStateChanged;
import com.tenbis.library.models.CreditCard;

import org.jetbrains.annotations.NotNull;

import co.paystack.android.Paystack;
import co.paystack.android.PaystackSdk;
import co.paystack.android.Transaction;
import co.paystack.android.model.Card;
import co.paystack.android.model.Charge;

public class PayStackActivity extends AppCompatActivity implements OnCreditCardStateChanged {

    ActivityPaystackBinding viewBinding;
    Plan plan;
    String planGateway = "Paystack", payStackPublicKey;
    MyApplication myApplication;
    ProgressDialog pDialog;
    CreditCard creditCard;
    boolean isCardValid = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PaystackSdk.initialize(this);
        viewBinding = ActivityPaystackBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        StatusBarUtil.setFullScreen(this, viewBinding.getRoot());
        IsRTL.ifSupported(this);
        myApplication = MyApplication.getInstance();
        pDialog = new ProgressDialog(this, R.style.AlertDialogStyle);

        Intent intent = getIntent();
        plan = intent.getParcelableExtra("planInfo");
        payStackPublicKey = intent.getStringExtra("payStackPublicKey");

        viewBinding.toolbar.includeImage.getRoot().setVisibility(View.GONE);
        viewBinding.toolbar.tvName.setText(getString(R.string.payment));
        viewBinding.toolbar.fabBack.setOnClickListener(view -> onBackPressed());

        String payString = getString(R.string.pay_via, plan.getPlanPrice(), plan.planCurrencyCode, planGateway);
        viewBinding.btnPay.setText(payString);
        viewBinding.btnPay.setOnClickListener(view -> {
            if (NetworkUtils.isConnected(PayStackActivity.this)) {
                if (isCardValid && creditCard != null) {
                    performCharge();
                }
            } else {
                GeneralUtils.showNoNetwork(this);
            }
        });

        viewBinding.ccInout.addOnCreditCardStateChangedListener(this);
        PaystackSdk.setPublicKey(payStackPublicKey);
    }

    public void performCharge() {
        showProgressDialog();
        Charge charge = new Charge();
        charge.setCard(loadCardFromForm());
        charge.setEmail(myApplication.getLoginInfo().getUserEmail());
        double amount = Double.parseDouble(plan.getPlanPrice());
        charge.setAmount((int) amount * 100);
        PaystackSdk.chargeCard(PayStackActivity.this, charge, new Paystack.TransactionCallback() {
            @Override
            public void onSuccess(Transaction transaction) {
                dismissProgressDialog();
                GeneralUtils.addTransaction(PayStackActivity.this, plan.getPlanId(), transaction.getReference(), planGateway);
            }

            @Override
            public void beforeValidate(Transaction transaction) {

            }

            @Override
            public void showLoading(Boolean isProcessing) {

            }

            @Override
            public void onError(Throwable error, Transaction transaction) {
                dismissProgressDialog();
                showError(error.getMessage());
            }
        });
    }

    public void showProgressDialog() {
        pDialog.setMessage(getString(R.string.loading));
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(true);
        pDialog.show();
    }

    public void dismissProgressDialog() {
        if (pDialog != null && pDialog.isShowing())
            pDialog.dismiss();
    }

    private void showError(String Title) {
        new AlertDialog.Builder(PayStackActivity.this)
                .setTitle(planGateway)
                .setMessage(Title)
                .setIcon(R.mipmap.ic_launcher)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {

                })
                .setNegativeButton(android.R.string.no, (dialog, which) -> {
                    // do nothing
                })
                .show();
    }

    private Card loadCardFromForm() {
        return new Card.Builder(creditCard.getCardNumber(), creditCard.getExpiryMonth(), creditCard.getExpiryYear(), creditCard.getCvv()).build();
    }

    @Override
    public void onCreditCardCvvValid(@NotNull String s) {

    }

    @Override
    public void onCreditCardExpirationDateValid(int i, int i1) {

    }

    @Override
    public void onCreditCardNumberValid(@NotNull String s) {

    }

    @Override
    public void onCreditCardTypeFound(@NotNull CardType cardType) {

    }

    @Override
    public void onCreditCardValid(@NotNull CreditCard creditCard) {
        isCardValid = true;
        this.creditCard = creditCard;
    }

    @Override
    public void onInvalidCardTyped() {
        isCardValid = false;
    }
}
