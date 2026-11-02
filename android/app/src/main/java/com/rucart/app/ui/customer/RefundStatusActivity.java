package com.rucart.app.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.rucart.app.R;
import com.rucart.app.model.Payment;

public class RefundStatusActivity extends AppCompatActivity {

    public static final String EXTRA_PAYMENT = "extra_payment";

    private Payment payment;

    private ImageView btnBack;
    private TextView tvRefundTitle, tvRefundBadge, tvRefundAmount, tvRefundSubtitle;
    private TextView tvOriginalAmount, tvRefundRefId, tvRefundReasonText, tvRefundDate;
    private Button btnDownloadRefundReceipt, btnBackToHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refund_status);

        payment = (Payment) getIntent().getSerializableExtra(EXTRA_PAYMENT);
        if (payment == null) {
            Toast.makeText(this, "Refund details not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        bindData();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBackRefund);
        tvRefundTitle = findViewById(R.id.tvRefundTitle);
        tvRefundBadge = findViewById(R.id.tvRefundBadge);
        tvRefundAmount = findViewById(R.id.tvRefundAmount);
        tvRefundSubtitle = findViewById(R.id.tvRefundSubtitle);

        tvOriginalAmount = findViewById(R.id.tvOriginalAmount);
        tvRefundRefId = findViewById(R.id.tvRefundRefId);
        tvRefundReasonText = findViewById(R.id.tvRefundReasonText);
        tvRefundDate = findViewById(R.id.tvRefundDate);

        btnDownloadRefundReceipt = findViewById(R.id.btnDownloadRefundReceipt);
        btnBackToHistory = findViewById(R.id.btnBackToHistory);
    }

    private void bindData() {
        if (payment.getRefundAmount() != null) {
            tvRefundAmount.setText("₹" + payment.getRefundAmount().toPlainString());
        } else if (payment.getAmount() != null) {
            tvRefundAmount.setText("₹" + payment.getAmount().toPlainString());
        }

        tvOriginalAmount.setText("₹" + (payment.getAmount() != null ? payment.getAmount().toPlainString() : "0.00"));

        String refId = payment.getGatewayRefundId() != null ? payment.getGatewayRefundId() :
                (payment.getTransactionId() != null ? "RFND_" + payment.getTransactionId() : "RFND_PROCESSED");
        tvRefundRefId.setText(refId);

        String reason = payment.getRefundReason() != null ? payment.getRefundReason() : "Customer order cancellation";
        tvRefundReasonText.setText(reason);

        String date = payment.getRefundedAt() != null ? payment.getRefundedAt() :
                (payment.getUpdatedAt() != null ? payment.getUpdatedAt() : "Recently");
        tvRefundDate.setText(date);

        String status = payment.getPaymentStatus() != null ? payment.getPaymentStatus() : "REFUNDED";
        tvRefundBadge.setText(status);
        if ("PARTIALLY_REFUNDED".equalsIgnoreCase(status)) {
            tvRefundTitle.setText("Partial Refund Completed");
            tvRefundSubtitle.setText("A portion of your payment was credited back to your account.");
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnBackToHistory.setOnClickListener(v -> finish());

        btnDownloadRefundReceipt.setOnClickListener(v -> {
            Intent receiptIntent = new Intent(RefundStatusActivity.this, ReceiptViewActivity.class);
            receiptIntent.putExtra(ReceiptViewActivity.EXTRA_PAYMENT_ID, payment.getId());
            if (payment.getOrder() != null) {
                receiptIntent.putExtra(ReceiptViewActivity.EXTRA_ORDER_ID, payment.getOrder().getId());
            }
            startActivity(receiptIntent);
        });
    }
}
