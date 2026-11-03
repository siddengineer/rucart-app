package com.rucart.app.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.rucart.app.R;
import com.rucart.app.api.ApiClient;
import com.rucart.app.model.ApiResponse;
import com.rucart.app.model.Order;
import com.rucart.app.model.Payment;
import com.rucart.app.model.PaymentOrderResult;
import com.rucart.app.model.PaymentVerificationRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentProcessingActivity extends AppCompatActivity {

    private static final String TAG = "PaymentProcessing";

    public static final String EXTRA_ORDER = "extra_order";
    public static final String EXTRA_PAYMENT_ORDER = "extra_payment_order";

    private Order order;
    private PaymentOrderResult paymentOrder;

    private ImageView btnCancelPayment;
    private TextView tvOrderNumber, tvAmountPayable, tvGatewayOrderId, tvModeBadge, tvGatewayStateMessage;
    private Button btnCompletePayment, btnSwitchToCOD, btnCancelReturn;
    private FrameLayout flProcessingOverlay;
    private TextView tvOverlayStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_processing);

        order = (Order) getIntent().getSerializableExtra(EXTRA_ORDER);
        paymentOrder = (PaymentOrderResult) getIntent().getSerializableExtra(EXTRA_PAYMENT_ORDER);

        if (order == null || paymentOrder == null) {
            Toast.makeText(this, "Missing payment session details", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        bindData();
        setupListeners();
    }

    private void initViews() {
        btnCancelPayment = findViewById(R.id.btnCancelPayment);
        tvOrderNumber = findViewById(R.id.tvOrderNumber);
        tvAmountPayable = findViewById(R.id.tvAmountPayable);
        tvGatewayOrderId = findViewById(R.id.tvGatewayOrderId);
        tvModeBadge = findViewById(R.id.tvModeBadge);
        tvGatewayStateMessage = findViewById(R.id.tvGatewayStateMessage);
        btnCompletePayment = findViewById(R.id.btnCompletePayment);
        btnSwitchToCOD = findViewById(R.id.btnSwitchToCOD);
        btnCancelReturn = findViewById(R.id.btnCancelReturn);
        flProcessingOverlay = findViewById(R.id.flProcessingOverlay);
        tvOverlayStatus = findViewById(R.id.tvOverlayStatus);
    }

    private void bindData() {
        String formattedAmt = "₹" + (paymentOrder.getAmount() != null ? paymentOrder.getAmount().toPlainString() : order.getGrandTotal().toPlainString());
        tvOrderNumber.setText("Order #" + (paymentOrder.getOrderNumber() != null ? paymentOrder.getOrderNumber() : order.getOrderNumber()));
        tvAmountPayable.setText(formattedAmt);
        tvGatewayOrderId.setText("Ref: " + paymentOrder.getGatewayOrderId());
        btnCompletePayment.setText("💳 Pay " + formattedAmt + " Now");

        if (paymentOrder.isSandbox()) {
            tvModeBadge.setText("SANDBOX");
        } else {
            tvModeBadge.setText("ONLINE SECURE");
        }

        tvGatewayStateMessage.setText("Tap 'Pay Now' to complete your secure online transaction.");
    }

    private void setupListeners() {
        btnCancelPayment.setOnClickListener(v -> handleCancelPayment());
        btnCancelReturn.setOnClickListener(v -> handleCancelPayment());
        btnCompletePayment.setOnClickListener(v -> processOnlinePayment());
        btnSwitchToCOD.setOnClickListener(v -> handleSwitchToCOD());
    }

    private void processOnlinePayment() {
        showLoading("Connecting to secure payment gateway...");

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            String gatewayOrderId = paymentOrder.getGatewayOrderId();
            String paymentId = "pay_gw_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
            String signature = "mock_sig_" + gatewayOrderId;

            showLoading("Payment authorized! Verifying with backend...");
            verifyPaymentWithBackend(gatewayOrderId, paymentId, signature, "ONLINE");
        }, 1200);
    }

    private void handleSwitchToCOD() {
        showLoading("Switching payment method to Cash on Delivery...");
        Intent resultIntent = new Intent(PaymentProcessingActivity.this, PaymentResultActivity.class);
        resultIntent.putExtra(PaymentResultActivity.EXTRA_STATUS, PaymentResultActivity.STATUS_COD);
        resultIntent.putExtra(PaymentResultActivity.EXTRA_ORDER, order);
        startActivity(resultIntent);
        finish();
    }

    private void verifyPaymentWithBackend(String gatewayOrderId, String gatewayPaymentId, String signature, String paymentMethod) {
        PaymentVerificationRequest req = new PaymentVerificationRequest(
                order.getId(),
                gatewayOrderId,
                gatewayPaymentId,
                signature
        );
        req.setPaymentMethod(paymentMethod);
        req.setIdempotencyKey(UUID.randomUUID().toString());

        ApiClient.getApiService().verifyPayment(req).enqueue(new Callback<ApiResponse<Payment>>() {
            @Override
            public void onResponse(Call<ApiResponse<Payment>> call, Response<ApiResponse<Payment>> response) {
                hideLoading();
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    Payment payment = response.body().getData();
                    Intent resultIntent = new Intent(PaymentProcessingActivity.this, PaymentResultActivity.class);
                    resultIntent.putExtra(PaymentResultActivity.EXTRA_STATUS, PaymentResultActivity.STATUS_SUCCESS);
                    resultIntent.putExtra(PaymentResultActivity.EXTRA_ORDER, order);
                    resultIntent.putExtra(PaymentResultActivity.EXTRA_PAYMENT, payment);
                    startActivity(resultIntent);
                    finish();
                } else {
                    String errorMsg = "Payment verification failed: ";
                    if (response.body() != null && response.body().getMessage() != null) {
                        errorMsg += response.body().getMessage();
                    } else {
                        errorMsg += "HTTP " + response.code();
                    }
                    navigateToFailure(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Payment>> call, Throwable t) {
                hideLoading();
                navigateToFailure("Network error during verification: " + t.getMessage());
            }
        });
    }

    private void showLoading(String statusText) {
        tvOverlayStatus.setText(statusText);
        flProcessingOverlay.setVisibility(View.VISIBLE);
        btnCompletePayment.setEnabled(false);
        btnSwitchToCOD.setEnabled(false);
        btnCancelReturn.setEnabled(false);
    }

    private void hideLoading() {
        flProcessingOverlay.setVisibility(View.GONE);
        btnCompletePayment.setEnabled(true);
        btnSwitchToCOD.setEnabled(true);
        btnCancelReturn.setEnabled(true);
    }

    private void handleCancelPayment() {
        showLoading("Cancelling payment...");

        Map<String, Object> body = new HashMap<>();
        body.put("orderId", order.getId());
        body.put("gatewayOrderId", paymentOrder.getGatewayOrderId());
        body.put("errorCode", "PAYMENT_CANCELLED_BY_USER");
        body.put("errorMessage", "Customer chose to abandon payment session");

        ApiClient.getApiService().recordFailedPayment(body).enqueue(new Callback<ApiResponse<Payment>>() {
            @Override
            public void onResponse(Call<ApiResponse<Payment>> call, Response<ApiResponse<Payment>> response) {
                hideLoading();
                Payment payment = (response.isSuccessful() && response.body() != null) ? response.body().getData() : null;
                Intent resultIntent = new Intent(PaymentProcessingActivity.this, PaymentResultActivity.class);
                resultIntent.putExtra(PaymentResultActivity.EXTRA_STATUS, PaymentResultActivity.STATUS_CANCELLED);
                resultIntent.putExtra(PaymentResultActivity.EXTRA_ORDER, order);
                resultIntent.putExtra(PaymentResultActivity.EXTRA_PAYMENT, payment);
                resultIntent.putExtra(PaymentResultActivity.EXTRA_ERROR_MESSAGE, "Payment was cancelled. Your items are saved in the order.");
                startActivity(resultIntent);
                finish();
            }

            @Override
            public void onFailure(Call<ApiResponse<Payment>> call, Throwable t) {
                hideLoading();
                navigateToFailure("Payment cancelled");
            }
        });
    }

    private void navigateToFailure(String message) {
        Intent resultIntent = new Intent(this, PaymentResultActivity.class);
        resultIntent.putExtra(PaymentResultActivity.EXTRA_STATUS, PaymentResultActivity.STATUS_FAILED);
        resultIntent.putExtra(PaymentResultActivity.EXTRA_ORDER, order);
        resultIntent.putExtra(PaymentResultActivity.EXTRA_ERROR_MESSAGE, message);
        startActivity(resultIntent);
        finish();
    }
}
