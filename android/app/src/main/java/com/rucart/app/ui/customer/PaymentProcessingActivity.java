package com.rucart.app.ui.customer;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.rucart.app.R;
import com.rucart.app.api.ApiClient;
import com.rucart.app.model.ApiResponse;
import com.rucart.app.model.Order;
import com.rucart.app.model.Payment;
import com.rucart.app.model.PaymentOrderResult;
import com.rucart.app.model.PaymentVerificationRequest;

import java.math.BigDecimal;
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

    public static final String BENEFICIARY_NAME = "Rutuja Ratik Butte";
    public static final String BENEFICIARY_PHONE = "8605528342";
    public static final String BENEFICIARY_UPI_ID = "8605528342@kotakbank";

    private Order order;
    private PaymentOrderResult paymentOrder;

    private ImageView btnCancelPayment, ivPaymentQr;
    private TextView tvOrderNumber, tvAmountPayable, tvModeBadge, tvViewFullQr;
    private TextView tvPayeeName, tvPayeePhone, tvPayeeUpiId, btnCopyPhone, btnCopyUpiId;
    private EditText etUpiRefId;
    private Button btnOpenUpiApp, btnCompletePayment, btnSwitchToCOD, btnCancelReturn;
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
        tvModeBadge = findViewById(R.id.tvModeBadge);

        tvPayeeName = findViewById(R.id.tvPayeeName);
        tvPayeePhone = findViewById(R.id.tvPayeePhone);
        tvPayeeUpiId = findViewById(R.id.tvPayeeUpiId);
        btnCopyPhone = findViewById(R.id.btnCopyPhone);
        btnCopyUpiId = findViewById(R.id.btnCopyUpiId);

        ivPaymentQr = findViewById(R.id.ivPaymentQr);
        tvViewFullQr = findViewById(R.id.tvViewFullQr);
        btnOpenUpiApp = findViewById(R.id.btnOpenUpiApp);
        etUpiRefId = findViewById(R.id.etUpiRefId);

        btnCompletePayment = findViewById(R.id.btnCompletePayment);
        btnSwitchToCOD = findViewById(R.id.btnSwitchToCOD);
        btnCancelReturn = findViewById(R.id.btnCancelReturn);
        flProcessingOverlay = findViewById(R.id.flProcessingOverlay);
        tvOverlayStatus = findViewById(R.id.tvOverlayStatus);
    }

    private void bindData() {
        BigDecimal amt = paymentOrder.getAmount() != null ? paymentOrder.getAmount() : order.getGrandTotal();
        String formattedAmt = "?" + (amt != null ? amt.toPlainString() : "0.00");

        tvOrderNumber.setText("Order #" + (paymentOrder.getOrderNumber() != null ? paymentOrder.getOrderNumber() : order.getOrderNumber()));
        tvAmountPayable.setText(formattedAmt);

        tvPayeeName.setText(BENEFICIARY_NAME);
        tvPayeePhone.setText(BENEFICIARY_PHONE);
        tvPayeeUpiId.setText(BENEFICIARY_UPI_ID);

        btnCompletePayment.setText("? I Have Paid " + formattedAmt + " - Confirm Order");
    }

    private void setupListeners() {
        btnCancelPayment.setOnClickListener(v -> handleCancelPayment());
        btnCancelReturn.setOnClickListener(v -> handleCancelPayment());

        btnCopyPhone.setOnClickListener(v -> copyTextToClipboard(BENEFICIARY_PHONE, "Phone number copied: " + BENEFICIARY_PHONE));
        btnCopyUpiId.setOnClickListener(v -> copyTextToClipboard(BENEFICIARY_UPI_ID, "UPI ID copied: " + BENEFICIARY_UPI_ID));

        ivPaymentQr.setOnClickListener(v -> showFullQrDialog());
        tvViewFullQr.setOnClickListener(v -> showFullQrDialog());

        btnOpenUpiApp.setOnClickListener(v -> launchUpiIntent());

        btnCompletePayment.setOnClickListener(v -> processUpiPaymentConfirmation());
        btnSwitchToCOD.setOnClickListener(v -> handleSwitchToCOD());
    }

    private void copyTextToClipboard(String text, String message) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("RUCART UPI Details", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showFullQrDialog() {
        ImageView iv = new ImageView(this);
        iv.setImageResource(R.drawable.qr_upi_payment);
        iv.setAdjustViewBounds(true);
        iv.setPadding(24, 24, 24, 24);

        new AlertDialog.Builder(this)
                .setTitle("Rutuja Ratik Butte - Full QR")
                .setView(iv)
                .setPositiveButton("Close", null)
                .show();
    }

    private void launchUpiIntent() {
        BigDecimal amt = paymentOrder.getAmount() != null ? paymentOrder.getAmount() : order.getGrandTotal();
        String amtStr = amt != null ? amt.toPlainString() : "0.00";
        String note = "RUCART Order " + (order.getOrderNumber() != null ? order.getOrderNumber() : "");

        Uri uri = Uri.parse("upi://pay")
                .buildUpon()
                .appendQueryParameter("pa", BENEFICIARY_UPI_ID)
                .appendQueryParameter("pn", BENEFICIARY_NAME)
                .appendQueryParameter("mc", "")
                .appendQueryParameter("tr", paymentOrder.getGatewayOrderId())
                .appendQueryParameter("tn", note)
                .appendQueryParameter("am", amtStr)
                .appendQueryParameter("cu", "INR")
                .build();

        Intent upiIntent = new Intent(Intent.ACTION_VIEW, uri);
        Intent chooser = Intent.createChooser(upiIntent, "Pay with UPI (GPay, PhonePe, Paytm, etc.)");

        try {
            startActivity(chooser);
        } catch (Exception e) {
            Toast.makeText(this, "No UPI app found. Please scan the QR code or pay to " + BENEFICIARY_PHONE, Toast.LENGTH_LONG).show();
        }
    }

    private void processUpiPaymentConfirmation() {
        showLoading("Confirming UPI payment with RUCART servers...");

        String utr = etUpiRefId != null ? etUpiRefId.getText().toString().trim() : "";
        String gatewayPaymentId = !utr.isEmpty() ? ("upi_" + utr) : ("upi_pay_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10));
        String gatewayOrderId = paymentOrder.getGatewayOrderId();
        String signature = "upi_verified";

        verifyPaymentWithBackend(gatewayOrderId, gatewayPaymentId, signature, "UPI");
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
        showLoading("Cancelling payment session...");

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
