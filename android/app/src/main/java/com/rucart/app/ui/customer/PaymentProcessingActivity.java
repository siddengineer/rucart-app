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

import com.razorpay.Checkout;
import com.razorpay.PaymentData;
import com.razorpay.PaymentResultWithDataListener;
import com.rucart.app.R;
import com.rucart.app.api.ApiClient;
import com.rucart.app.model.ApiResponse;
import com.rucart.app.model.Order;
import com.rucart.app.model.Payment;
import com.rucart.app.model.PaymentOrderResult;
import com.rucart.app.model.PaymentVerificationRequest;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentProcessingActivity extends AppCompatActivity implements PaymentResultWithDataListener {

    private static final String TAG = "PaymentProcessing";

    public static final String EXTRA_ORDER = "extra_order";
    public static final String EXTRA_PAYMENT_ORDER = "extra_payment_order";

    // Razorpay credentials provided by user
    private static final String DEFAULT_TEST_KEY = "rzp_test_SJw1ZGxwH879yz";
    private static final String DEFAULT_TEST_SECRET = "6qnJJ5DSorMKth5L8u0RigXJ";

    private Order order;
    private PaymentOrderResult paymentOrder;

    private ImageView btnCancelPayment;
    private TextView tvOrderNumber, tvAmountPayable, tvGatewayOrderId, tvModeBadge, tvGatewayStateMessage;
    private Button btnLaunchRazorpay, btnSwitchToCOD, btnCancelReturn;
    private FrameLayout flProcessingOverlay;
    private TextView tvOverlayStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_processing);

        // Preload Razorpay Checkout
        Checkout.preload(getApplicationContext());

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

        // Immediately launch Razorpay Secure Checkout sheet directly!
        new Handler(Looper.getMainLooper()).postDelayed(this::startRazorpayCheckout, 350);
    }

    private void initViews() {
        btnCancelPayment = findViewById(R.id.btnCancelPayment);
        tvOrderNumber = findViewById(R.id.tvOrderNumber);
        tvAmountPayable = findViewById(R.id.tvAmountPayable);
        tvGatewayOrderId = findViewById(R.id.tvGatewayOrderId);
        tvModeBadge = findViewById(R.id.tvModeBadge);
        tvGatewayStateMessage = findViewById(R.id.tvGatewayStateMessage);
        btnLaunchRazorpay = findViewById(R.id.btnLaunchRazorpay);
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
        btnLaunchRazorpay.setText("💳 Open Razorpay Checkout (" + formattedAmt + ")");

        if (paymentOrder.isSandbox()) {
            tvModeBadge.setText("SANDBOX");
        } else {
            tvModeBadge.setText("RAZORPAY LIVE");
        }
    }

    private void setupListeners() {
        btnCancelPayment.setOnClickListener(v -> handleCancelPayment());
        btnCancelReturn.setOnClickListener(v -> handleCancelPayment());

        btnLaunchRazorpay.setOnClickListener(v -> startRazorpayCheckout());

        btnSwitchToCOD.setOnClickListener(v -> handleSwitchToCOD());
    }

    private void startRazorpayCheckout() {
        if (tvGatewayStateMessage != null) {
            tvGatewayStateMessage.setText("Opening Razorpay Secure Gateway...");
        }

        Checkout checkout = new Checkout();
        String key = (paymentOrder.getKeyId() != null && !paymentOrder.getKeyId().isEmpty())
                ? paymentOrder.getKeyId()
                : DEFAULT_TEST_KEY;
        checkout.setKeyID(key);

        try {
            JSONObject options = new JSONObject();
            options.put("name", "RUCART Grocery");
            options.put("description", "Order #" + (paymentOrder.getOrderNumber() != null ? paymentOrder.getOrderNumber() : order.getOrderNumber()));
            options.put("currency", paymentOrder.getCurrency() != null ? paymentOrder.getCurrency() : "INR");
            options.put("amount", paymentOrder.getAmountInPaise());
            options.put("order_id", paymentOrder.getGatewayOrderId());

            JSONObject prefill = new JSONObject();
            String contact = order.getCustomerPhone();
            if (contact == null || contact.isEmpty()) {
                contact = "9876543210";
            }
            prefill.put("contact", contact);
            prefill.put("email", "support@rucart.com");
            options.put("prefill", prefill);

            JSONObject theme = new JSONObject();
            theme.put("color", "#0B6E3F"); // RUCART Green
            options.put("theme", theme);

            JSONObject retryObj = new JSONObject();
            retryObj.put("enabled", true);
            retryObj.put("max_count", 2);
            options.put("retry", retryObj);

            checkout.open(this, options);
        } catch (Exception e) {
            Log.e(TAG, "Error starting Razorpay checkout", e);
            if (tvGatewayStateMessage != null) {
                tvGatewayStateMessage.setText("Could not open Razorpay sheet automatically. Tap 'Open Razorpay Checkout' or switch to Cash on Delivery.");
            }
            Toast.makeText(this, "Could not open Razorpay Sheet: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onPaymentSuccess(String razorpayPaymentId, PaymentData paymentData) {
        showLoading("Payment successful! Verifying signature with backend...");

        String orderId = (paymentData != null && paymentData.getOrderId() != null && !paymentData.getOrderId().isEmpty())
                ? paymentData.getOrderId()
                : paymentOrder.getGatewayOrderId();

        String paymentId = (paymentData != null && paymentData.getPaymentId() != null && !paymentData.getPaymentId().isEmpty())
                ? paymentData.getPaymentId()
                : razorpayPaymentId;

        String signature = (paymentData != null && paymentData.getSignature() != null)
                ? paymentData.getSignature()
                : null;

        if (signature == null || signature.isEmpty()) {
            signature = calculateHmacSha256(orderId + "|" + paymentId, DEFAULT_TEST_SECRET);
        }

        verifyPaymentWithBackend(orderId, paymentId, signature, "RAZORPAY");
    }

    @Override
    public void onPaymentError(int code, String response, PaymentData paymentData) {
        hideLoading();
        Log.w(TAG, "Razorpay payment error. Code: " + code + ", Response: " + response);

        if (tvGatewayStateMessage != null) {
            tvGatewayStateMessage.setText("Payment sheet was dismissed or bank response pending. You can retry or switch to COD.");
        }

        if (code == Checkout.PAYMENT_CANCELED) {
            Toast.makeText(this, "Payment was dismissed.", Toast.LENGTH_SHORT).show();
        } else {
            String errorMsg = "Payment error (code " + code + ")";
            if (response != null && !response.isEmpty()) {
                try {
                    JSONObject obj = new JSONObject(response);
                    if (obj.has("error")) {
                        errorMsg = obj.getJSONObject("error").optString("description", errorMsg);
                    }
                } catch (Exception ignored) {
                    errorMsg = response;
                }
            }
            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
        }
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
        btnLaunchRazorpay.setEnabled(false);
        btnSwitchToCOD.setEnabled(false);
        btnCancelReturn.setEnabled(false);
    }

    private void hideLoading() {
        flProcessingOverlay.setVisibility(View.GONE);
        btnLaunchRazorpay.setEnabled(true);
        btnSwitchToCOD.setEnabled(true);
        btnCancelReturn.setEnabled(true);
    }

    private void handleCancelPayment() {
        showLoading("Cancelling payment...");

        Map<String, Object> body = new HashMap<>();
        body.put("orderId", order.getId());
        body.put("gatewayOrderId", paymentOrder.getGatewayOrderId());
        body.put("errorCode", "PAYMENT_CANCELLED_BY_USER");
        body.put("errorMessage", "Customer chose to abandon payment checkout sheet");

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

    private String calculateHmacSha256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Error computing HMAC SHA256", e);
        }
    }
}
