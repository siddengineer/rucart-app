package com.rucart.app.ui.customer;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.rucart.app.R;
import com.rucart.app.api.ApiClient;
import com.rucart.app.model.ApiResponse;
import com.rucart.app.model.Order;
import com.rucart.app.model.Payment;
import com.rucart.app.model.PaymentOrderResult;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentResultActivity extends AppCompatActivity {

    public static final String EXTRA_STATUS = "extra_status";
    public static final String EXTRA_ORDER = "extra_order";
    public static final String EXTRA_PAYMENT = "extra_payment";
    public static final String EXTRA_ERROR_MESSAGE = "extra_error_message";

    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_COD = "COD";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    private String status;
    private Order order;
    private Payment payment;
    private String errorMessage;

    private FrameLayout flStatusIconBg;
    private ImageView ivStatusIcon;
    private TextView tvStatusTitle, tvStatusSubtitle;
    private TextView tvResultAmount, tvResultOrderNumber, tvResultTxnId, tvResultReceiptNumber, tvResultPaymentMethod, tvResultDateTime;
    private LinearLayout rowTxnId, rowReceiptNumber, rowErrorReason;
    private TextView tvResultErrorReason;
    private Button btnDownloadReceipt, btnRetryPayment, btnViewOrder, btnContinueShopping;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_result);

        status = getIntent().getStringExtra(EXTRA_STATUS);
        if (status == null) status = STATUS_SUCCESS;
        order = (Order) getIntent().getSerializableExtra(EXTRA_ORDER);
        payment = (Payment) getIntent().getSerializableExtra(EXTRA_PAYMENT);
        errorMessage = getIntent().getStringExtra(EXTRA_ERROR_MESSAGE);

        initViews();
        setupUI();
        setupListeners();
    }

    private void initViews() {
        flStatusIconBg = findViewById(R.id.flStatusIconBg);
        ivStatusIcon = findViewById(R.id.ivStatusIcon);
        tvStatusTitle = findViewById(R.id.tvStatusTitle);
        tvStatusSubtitle = findViewById(R.id.tvStatusSubtitle);

        tvResultAmount = findViewById(R.id.tvResultAmount);
        tvResultOrderNumber = findViewById(R.id.tvResultOrderNumber);
        tvResultTxnId = findViewById(R.id.tvResultTxnId);
        tvResultReceiptNumber = findViewById(R.id.tvResultReceiptNumber);
        tvResultPaymentMethod = findViewById(R.id.tvResultPaymentMethod);
        tvResultDateTime = findViewById(R.id.tvResultDateTime);

        rowTxnId = findViewById(R.id.rowTxnId);
        rowReceiptNumber = findViewById(R.id.rowReceiptNumber);
        rowErrorReason = findViewById(R.id.rowErrorReason);
        tvResultErrorReason = findViewById(R.id.tvResultErrorReason);

        btnDownloadReceipt = findViewById(R.id.btnDownloadReceipt);
        btnRetryPayment = findViewById(R.id.btnRetryPayment);
        btnViewOrder = findViewById(R.id.btnViewOrder);
        btnContinueShopping = findViewById(R.id.btnContinueShopping);
    }

    private void setupUI() {
        String nowFormatted = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(new Date());
        tvResultDateTime.setText(nowFormatted);

        if (order != null) {
            tvResultOrderNumber.setText("#" + order.getOrderNumber());
            tvResultAmount.setText("₹" + order.getGrandTotal().toPlainString());
        }

        if (payment != null) {
            if (payment.getAmount() != null) {
                tvResultAmount.setText("₹" + payment.getAmount().toPlainString());
            }
            if (payment.getTransactionId() != null) {
                tvResultTxnId.setText(payment.getTransactionId());
            }
            if (payment.getReceiptNumber() != null) {
                tvResultReceiptNumber.setText(payment.getReceiptNumber());
            }
            if (payment.getPaymentMethod() != null) {
                tvResultPaymentMethod.setText(payment.getPaymentMethod());
            }
        }

        switch (status) {
            case STATUS_SUCCESS:
                flStatusIconBg.setBackgroundResource(R.drawable.bg_badge_green);
                ivStatusIcon.setImageResource(R.drawable.ic_check);
                ivStatusIcon.setColorFilter(Color.parseColor("#1B5E20"));
                tvStatusTitle.setText("Payment Confirmed!");
                tvStatusSubtitle.setText("Your payment was securely verified and order has been confirmed.");
                btnDownloadReceipt.setVisibility(View.VISIBLE);
                btnRetryPayment.setVisibility(View.GONE);
                rowErrorReason.setVisibility(View.GONE);
                break;

            case STATUS_COD:
                flStatusIconBg.setBackgroundResource(R.drawable.bg_badge_green);
                ivStatusIcon.setImageResource(R.drawable.ic_check);
                ivStatusIcon.setColorFilter(Color.parseColor("#1B5E20"));
                tvStatusTitle.setText("Order Confirmed!");
                tvStatusSubtitle.setText("Cash on Delivery selected. Please keep ₹" + (order != null ? order.getGrandTotal().toPlainString() : "") + " cash ready.");
                tvResultPaymentMethod.setText("CASH_ON_DELIVERY");
                btnDownloadReceipt.setVisibility(View.VISIBLE);
                btnRetryPayment.setVisibility(View.GONE);
                rowErrorReason.setVisibility(View.GONE);
                break;

            case STATUS_FAILED:
                flStatusIconBg.setBackgroundResource(R.drawable.bg_badge_red);
                ivStatusIcon.setImageResource(R.drawable.ic_close);
                ivStatusIcon.setColorFilter(Color.parseColor("#B71C1C"));
                tvStatusTitle.setText("Payment Failed");
                tvStatusSubtitle.setText("We couldn't process your transaction. No money was deducted.");
                btnDownloadReceipt.setVisibility(View.GONE);
                btnRetryPayment.setVisibility(View.VISIBLE);
                rowErrorReason.setVisibility(View.VISIBLE);
                tvResultErrorReason.setText(errorMessage != null ? errorMessage : "Payment was rejected by the gateway or bank.");
                break;

            case STATUS_CANCELLED:
                flStatusIconBg.setBackgroundResource(R.drawable.bg_badge_orange);
                ivStatusIcon.setImageResource(R.drawable.ic_close);
                ivStatusIcon.setColorFilter(Color.parseColor("#E65100"));
                tvStatusTitle.setText("Payment Incomplete");
                tvStatusSubtitle.setText("The payment process was cancelled before completion.");
                btnDownloadReceipt.setVisibility(View.GONE);
                btnRetryPayment.setVisibility(View.VISIBLE);
                rowErrorReason.setVisibility(View.VISIBLE);
                tvResultErrorReason.setText(errorMessage != null ? errorMessage : "Checkout sheet was dismissed.");
                break;
        }
    }

    private void setupListeners() {
        btnDownloadReceipt.setOnClickListener(v -> {
            Intent receiptIntent = new Intent(PaymentResultActivity.this, ReceiptViewActivity.class);
            if (order != null) receiptIntent.putExtra(ReceiptViewActivity.EXTRA_ORDER_ID, order.getId());
            if (payment != null) receiptIntent.putExtra(ReceiptViewActivity.EXTRA_PAYMENT_ID, payment.getId());
            startActivity(receiptIntent);
        });

        btnRetryPayment.setOnClickListener(v -> {
            if (order == null) {
                Toast.makeText(this, "Order information missing for retry", Toast.LENGTH_SHORT).show();
                return;
            }
            btnRetryPayment.setEnabled(false);
            Map<String, Object> body = new HashMap<>();
            body.put("orderId", order.getId());

            ApiClient.getApiService().initiateOnlinePayment(body).enqueue(new Callback<ApiResponse<PaymentOrderResult>>() {
                @Override
                public void onResponse(Call<ApiResponse<PaymentOrderResult>> call, Response<ApiResponse<PaymentOrderResult>> response) {
                    btnRetryPayment.setEnabled(true);
                    if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                        PaymentOrderResult orderResult = response.body().getData();
                        Intent procIntent = new Intent(PaymentResultActivity.this, PaymentProcessingActivity.class);
                        procIntent.putExtra(PaymentProcessingActivity.EXTRA_ORDER, order);
                        procIntent.putExtra(PaymentProcessingActivity.EXTRA_PAYMENT_ORDER, orderResult);
                        startActivity(procIntent);
                        finish();
                    } else {
                        Toast.makeText(PaymentResultActivity.this, "Unable to re-initiate payment gateway session", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<PaymentOrderResult>> call, Throwable t) {
                    btnRetryPayment.setEnabled(true);
                    Toast.makeText(PaymentResultActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnViewOrder.setOnClickListener(v -> {
            if (order != null) {
                Intent orderIntent = new Intent(PaymentResultActivity.this, OrderDetailActivity.class);
                orderIntent.putExtra("order_id", order.getId());
                startActivity(orderIntent);
                finish();
            } else {
                navigateToHome();
            }
        });

        btnContinueShopping.setOnClickListener(v -> navigateToHome());
    }

    private void navigateToHome() {
        Intent homeIntent = new Intent(PaymentResultActivity.this, CustomerMainActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(homeIntent);
        finish();
    }
}
