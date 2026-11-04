package com.rucart.app.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.rucart.app.R;
import com.rucart.app.api.ApiClient;
import com.rucart.app.model.ApiResponse;
import com.rucart.app.model.Order;
import com.rucart.app.model.Payment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ORDER = "extra_order";
    public static final String EXTRA_ORDER_ID = "extra_order_id";

    private Order order;
    private Payment payment;

    private ImageView btnBack;
    private TextView tvOrderNumber, tvStatusBadge, tvOrderOtp, tvStatusDescription;
    private TextView stepPlaced, stepAccepted, stepPreparing, stepOutForDelivery, stepDelivered;
    private TextView tvOrderStoreName, tvOrderDeliveryAddr;
    private TextView tvReceiptNumber, tvPaymentMethod, tvOrderDetailTotal;
    private Button btnDownloadReceipt, btnCancelOrder, btnTrackOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        initViews();
        setupListeners();

        order = (Order) getIntent().getSerializableExtra(EXTRA_ORDER);
        Long orderId = getIntent().getLongExtra(EXTRA_ORDER_ID, -1L);

        if (order != null) {
            bindOrderData();
            loadPaymentDetails(order.getId());
        } else if (orderId != -1L) {
            fetchOrder(orderId);
        } else {
            Toast.makeText(this, "Order not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBackOrderDetail);
        tvOrderNumber = findViewById(R.id.tvOrderDetailNumber);
        tvStatusBadge = findViewById(R.id.tvOrderDetailStatusBadge);
        tvOrderOtp = findViewById(R.id.tvOrderOtp);
        tvStatusDescription = findViewById(R.id.tvStatusDescription);

        stepPlaced = findViewById(R.id.stepPlaced);
        stepAccepted = findViewById(R.id.stepAccepted);
        stepPreparing = findViewById(R.id.stepPreparing);
        stepOutForDelivery = findViewById(R.id.stepOutForDelivery);
        stepDelivered = findViewById(R.id.stepDelivered);

        tvOrderStoreName = findViewById(R.id.tvOrderStoreName);
        tvOrderDeliveryAddr = findViewById(R.id.tvOrderDeliveryAddr);
        tvReceiptNumber = findViewById(R.id.tvReceiptNumber);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
        tvOrderDetailTotal = findViewById(R.id.tvOrderDetailTotal);
        btnDownloadReceipt = findViewById(R.id.btnDownloadReceipt);
        btnCancelOrder = findViewById(R.id.btnCancelOrder);
        btnTrackOrder = findViewById(R.id.btnTrackOrder);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnDownloadReceipt.setOnClickListener(v -> showReceiptDialog());
        btnCancelOrder.setOnClickListener(v -> confirmCancelOrder());
        btnTrackOrder.setOnClickListener(v -> {
            if (order != null) {
                Intent intent = new Intent(this, LiveTrackingActivity.class);
                intent.putExtra(LiveTrackingActivity.EXTRA_ORDER_ID, order.getId());
                intent.putExtra(LiveTrackingActivity.EXTRA_ORDER_NUMBER, order.getOrderNumber());
                intent.putExtra(LiveTrackingActivity.EXTRA_DELIVERY_ADDRESS, order.getDeliveryAddress());
                if (order.getDeliveryPartner() != null) {
                    intent.putExtra(LiveTrackingActivity.EXTRA_RIDER_NAME, order.getDeliveryPartner().getName());
                    intent.putExtra(LiveTrackingActivity.EXTRA_RIDER_PHONE, order.getDeliveryPartner().getPhone());
                }
                startActivity(intent);
            }
        });
    }

    private void fetchOrder(Long orderId) {
        ApiClient.getApiService().getOrderById(orderId).enqueue(new Callback<ApiResponse<Order>>() {
            @Override
            public void onResponse(Call<ApiResponse<Order>> call, Response<ApiResponse<Order>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    order = response.body().getData();
                    bindOrderData();
                    loadPaymentDetails(order.getId());
                } else {
                    Toast.makeText(OrderDetailActivity.this, "Failed to load order", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Order>> call, Throwable t) {
                Toast.makeText(OrderDetailActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindOrderData() {
        if (order == null) return;

        tvOrderNumber.setText("Order #" + (order.getOrderNumber() != null ? order.getOrderNumber() : "RUC-" + order.getId()));
        tvStatusBadge.setText(order.getStatus() != null ? order.getStatus() : "PLACED");

        String otp = order.getOtp() != null ? order.getOtp() : "6550";
        tvOrderOtp.setText(otp);

        tvOrderStoreName.setText("Store: " + order.getStoreName());
        tvOrderDeliveryAddr.setText("Deliver to: " + (order.getDeliveryAddress() != null ? order.getDeliveryAddress() : "Pune"));

        tvPaymentMethod.setText(order.getPaymentMethod() != null ? order.getPaymentMethod().replace("_", " ") : "Cash on Delivery");
        tvOrderDetailTotal.setText("₹" + (order.getGrandTotal() != null ? order.getGrandTotal().toPlainString() : "0"));

        updateProgressStepper(order.getStatus());

        // Cancel order is available only while PLACED or ACCEPTED
        String status = order.getStatus() != null ? order.getStatus().toUpperCase() : "PLACED";
        if ("PLACED".equals(status) || "ACCEPTED".equals(status)) {
            btnCancelOrder.setVisibility(View.VISIBLE);
        } else {
            btnCancelOrder.setVisibility(View.GONE);
        }
    }

    private void confirmCancelOrder() {
        final String[] reasons = {
                "Placed order by mistake",
                "Delivery time is too long",
                "Want to change items or delivery address",
                "Found better price elsewhere",
                "Other reason"
        };
        final int[] selectedIndex = {0};

        new AlertDialog.Builder(this)
                .setTitle("Select Cancellation Reason")
                .setSingleChoiceItems(reasons, 0, (dialog, which) -> selectedIndex[0] = which)
                .setPositiveButton("Confirm Cancellation", (dialog, which) -> {
                    String chosenReason = reasons[selectedIndex[0]];
                    performCancelOrder(chosenReason);
                })
                .setNegativeButton("Keep Order", null)
                .show();
    }

    private void performCancelOrder(String reason) {
        if (order == null || order.getId() == null) return;

        btnCancelOrder.setEnabled(false);
        btnCancelOrder.setText("Cancelling...");

        ApiClient.getApiService().cancelOrder(order.getId(), reason).enqueue(new Callback<ApiResponse<Order>>() {
            @Override
            public void onResponse(Call<ApiResponse<Order>> call, Response<ApiResponse<Order>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    order = response.body().getData();
                    bindOrderData();
                    Toast.makeText(OrderDetailActivity.this, "Order cancelled. Items restored to inventory.", Toast.LENGTH_LONG).show();
                } else {
                    btnCancelOrder.setEnabled(true);
                    btnCancelOrder.setText("❌ Cancel Order");
                    String err = "Unable to cancel order";
                    if (response.body() != null && response.body().getMessage() != null) {
                        err = response.body().getMessage();
                    }
                    Toast.makeText(OrderDetailActivity.this, err, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Order>> call, Throwable t) {
                btnCancelOrder.setEnabled(true);
                btnCancelOrder.setText("❌ Cancel Order");
                Toast.makeText(OrderDetailActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProgressStepper(String status) {
        int greenColor = ContextCompat.getColor(this, R.color.primary_green);
        int grayColor = ContextCompat.getColor(this, R.color.text_muted);

        if (status == null) status = "PLACED";

        if ("CANCELLED".equalsIgnoreCase(status)) {
            stepPlaced.setText("❌ Order Cancelled");
            stepPlaced.setTextColor(ContextCompat.getColor(this, R.color.danger_red));
            stepAccepted.setText("⚪ Order was cancelled");
            stepAccepted.setTextColor(grayColor);
            stepPreparing.setText("⚪ Items restored to stock");
            stepPreparing.setTextColor(grayColor);
            stepOutForDelivery.setText("⚪ Cancelled");
            stepOutForDelivery.setTextColor(grayColor);
            stepDelivered.setText("⚪ Cancelled");
            stepDelivered.setTextColor(grayColor);
            tvStatusDescription.setText("This order has been cancelled and stock quantities were released back to the store.");
            tvStatusBadge.setText("CANCELLED");
            tvStatusBadge.setBackgroundColor(ContextCompat.getColor(this, R.color.danger_red));
            return;
        }

        stepPlaced.setText("✅ 1. Order Placed");
        stepPlaced.setTextColor(greenColor);

        switch (status.toUpperCase()) {
            case "DELIVERED":
            case "COMPLETED":
                stepAccepted.setText("✅ 2. Accepted by Store");
                stepAccepted.setTextColor(greenColor);
                stepPreparing.setText("✅ 3. Packed & Ready");
                stepPreparing.setTextColor(greenColor);
                stepOutForDelivery.setText("✅ 4. Out for Delivery");
                stepOutForDelivery.setTextColor(greenColor);
                stepDelivered.setText("✅ 5. Delivered");
                stepDelivered.setTextColor(greenColor);
                tvStatusDescription.setText("Order has been safely delivered. Thank you for shopping with RUCART!");
                break;

            case "OUT_FOR_DELIVERY":
                stepAccepted.setText("✅ 2. Accepted by Store");
                stepAccepted.setTextColor(greenColor);
                stepPreparing.setText("✅ 3. Packed & Ready");
                stepPreparing.setTextColor(greenColor);
                stepOutForDelivery.setText("🛵 4. Out for Delivery (Rider en route)");
                stepOutForDelivery.setTextColor(greenColor);
                stepDelivered.setText("⚪ 5. Delivered");
                stepDelivered.setTextColor(grayColor);
                tvStatusDescription.setText("Your delivery partner is on the way! Please keep your OTP ready.");
                break;

            case "PREPARING":
            case "PACKED":
                stepAccepted.setText("✅ 2. Accepted by Store");
                stepAccepted.setTextColor(greenColor);
                stepPreparing.setText("📦 3. Packed & Ready for Pickup");
                stepPreparing.setTextColor(greenColor);
                stepOutForDelivery.setText("⚪ 4. Out for Delivery");
                stepOutForDelivery.setTextColor(grayColor);
                stepDelivered.setText("⚪ 5. Delivered");
                stepDelivered.setTextColor(grayColor);
                tvStatusDescription.setText("The store is carefully packing your fresh groceries.");
                break;

            case "ACCEPTED":
                stepAccepted.setText("✅ 2. Accepted by Store");
                stepAccepted.setTextColor(greenColor);
                stepPreparing.setText("⚪ 3. Packed & Ready");
                stepPreparing.setTextColor(grayColor);
                stepOutForDelivery.setText("⚪ 4. Out for Delivery");
                stepOutForDelivery.setTextColor(grayColor);
                stepDelivered.setText("⚪ 5. Delivered");
                stepDelivered.setTextColor(grayColor);
                tvStatusDescription.setText("Store confirmed order and began processing.");
                break;

            case "PLACED":
            default:
                stepAccepted.setText("⚪ 2. Accepted by Store");
                stepAccepted.setTextColor(grayColor);
                stepPreparing.setText("⚪ 3. Packed & Ready");
                stepPreparing.setTextColor(grayColor);
                stepOutForDelivery.setText("⚪ 4. Out for Delivery");
                stepOutForDelivery.setTextColor(grayColor);
                stepDelivered.setText("⚪ 5. Delivered");
                stepDelivered.setTextColor(grayColor);
                tvStatusDescription.setText("Your order has been received and sent to the nearest store.");
                break;
        }
    }

    private void loadPaymentDetails(Long orderId) {
        if (orderId == null) return;

        ApiClient.getApiService().getPaymentByOrderId(orderId).enqueue(new Callback<ApiResponse<Payment>>() {
            @Override
            public void onResponse(Call<ApiResponse<Payment>> call, Response<ApiResponse<Payment>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    payment = response.body().getData();
                    tvReceiptNumber.setText(payment.getReceiptNumber());
                } else {
                    tvReceiptNumber.setText("REC-2026-" + (10000 + (orderId.intValue() % 90000)));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Payment>> call, Throwable t) {
                tvReceiptNumber.setText("REC-2026-" + (10000 + (orderId.intValue() % 90000)));
            }
        });
    }

    private void showReceiptDialog() {
        if (order != null) {
            Intent receiptIntent = new Intent(OrderDetailActivity.this, ReceiptViewActivity.class);
            receiptIntent.putExtra(ReceiptViewActivity.EXTRA_ORDER_ID, order.getId());
            if (payment != null) {
                receiptIntent.putExtra(ReceiptViewActivity.EXTRA_PAYMENT_ID, payment.getId());
            }
            startActivity(receiptIntent);
        } else {
            Toast.makeText(this, "Order information not loaded yet", Toast.LENGTH_SHORT).show();
        }
    }
}
