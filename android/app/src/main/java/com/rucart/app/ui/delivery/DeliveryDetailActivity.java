package com.rucart.app.ui.delivery;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import com.google.android.material.card.MaterialCardView;
import com.rucart.app.R;
import com.rucart.app.api.ApiClient;
import com.rucart.app.model.ApiResponse;
import com.rucart.app.model.DeliveryIssueReportRequest;
import com.rucart.app.model.Order;
import com.rucart.app.model.OrderItem;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.HashMap;
import java.util.Map;

public class DeliveryDetailActivity extends AppCompatActivity {

    private ImageView btnBackDetail;
    private TextView tvDetailOrderNumber;
    private TextView tvDetailStatusBadge;
    private TextView tvTimelineSummary;

    private MaterialCardView cardPaymentRequirement;
    private TextView tvPaymentInstruction;

    private TextView tvDetailStoreName;
    private TextView tvDetailStoreAddress;
    private AppCompatButton btnCopyStoreAddress;
    private AppCompatButton btnNavStore;
    private AppCompatButton btnCallStore;

    private TextView tvDetailCustomerName;
    private TextView tvDetailCustomerAddress;
    private LinearLayout layoutSpecialInstructions;
    private TextView tvDetailSpecialInstructions;
    private AppCompatButton btnCopyCustomerAddress;
    private AppCompatButton btnNavCustomer;
    private AppCompatButton btnCallCustomer;

    private LinearLayout layoutOrderItemsContainer;
    private TextView tvDetailGrandTotal;

    private ProgressBar pbDetailAction;
    private AppCompatButton btnPrimaryWorkflowAction;
    private AppCompatButton btnReportIssue;

    private Long orderId;
    private Order currentOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_detail);

        orderId = getIntent().getLongExtra("order_id", -1L);
        if (orderId == -1L) {
            Toast.makeText(this, "Invalid Order ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupListeners();
        loadOrderDetail();
    }

    private void initViews() {
        btnBackDetail = findViewById(R.id.btnBackDetail);
        tvDetailOrderNumber = findViewById(R.id.tvDetailOrderNumber);
        tvDetailStatusBadge = findViewById(R.id.tvDetailStatusBadge);
        tvTimelineSummary = findViewById(R.id.tvTimelineSummary);

        cardPaymentRequirement = findViewById(R.id.cardPaymentRequirement);
        tvPaymentInstruction = findViewById(R.id.tvPaymentInstruction);

        tvDetailStoreName = findViewById(R.id.tvDetailStoreName);
        tvDetailStoreAddress = findViewById(R.id.tvDetailStoreAddress);
        btnCopyStoreAddress = findViewById(R.id.btnCopyStoreAddress);
        btnNavStore = findViewById(R.id.btnNavStore);
        btnCallStore = findViewById(R.id.btnCallStore);

        tvDetailCustomerName = findViewById(R.id.tvDetailCustomerName);
        tvDetailCustomerAddress = findViewById(R.id.tvDetailCustomerAddress);
        layoutSpecialInstructions = findViewById(R.id.layoutSpecialInstructions);
        tvDetailSpecialInstructions = findViewById(R.id.tvDetailSpecialInstructions);
        btnCopyCustomerAddress = findViewById(R.id.btnCopyCustomerAddress);
        btnNavCustomer = findViewById(R.id.btnNavCustomer);
        btnCallCustomer = findViewById(R.id.btnCallCustomer);

        layoutOrderItemsContainer = findViewById(R.id.layoutOrderItemsContainer);
        tvDetailGrandTotal = findViewById(R.id.tvDetailGrandTotal);

        pbDetailAction = findViewById(R.id.pbDetailAction);
        btnPrimaryWorkflowAction = findViewById(R.id.btnPrimaryWorkflowAction);
        btnReportIssue = findViewById(R.id.btnReportIssue);
    }

    private void setupListeners() {
        btnBackDetail.setOnClickListener(v -> finish());

        btnCopyStoreAddress.setOnClickListener(v -> {
            if (currentOrder != null && currentOrder.getStore() != null) {
                copyToClipboard(currentOrder.getStore().getAddress(), "Store address copied");
            } else {
                copyToClipboard(tvDetailStoreAddress.getText().toString(), "Store address copied");
            }
        });

        btnNavStore.setOnClickListener(v -> {
            String address = tvDetailStoreAddress.getText().toString();
            openNavigation(address);
        });

        btnCallStore.setOnClickListener(v -> {
            String phone = (currentOrder != null && currentOrder.getStore() != null && currentOrder.getStore().getPhone() != null)
                    ? currentOrder.getStore().getPhone() : "";
            if (!phone.isEmpty()) { openDialer(phone); } else { Toast.makeText(this, "Store phone number not available", Toast.LENGTH_SHORT).show(); }
        });

        btnCopyCustomerAddress.setOnClickListener(v -> {
            String address = tvDetailCustomerAddress.getText().toString();
            copyToClipboard(address, "Customer delivery address copied");
        });

        btnNavCustomer.setOnClickListener(v -> {
            String address = tvDetailCustomerAddress.getText().toString();
            openNavigation(address);
        });

        btnCallCustomer.setOnClickListener(v -> {
            String phone = (currentOrder != null && currentOrder.getCustomerPhone() != null)
                    ? currentOrder.getCustomerPhone() : "";
            if (!phone.isEmpty()) { openDialer(phone); } else { Toast.makeText(this, "Customer phone number not available", Toast.LENGTH_SHORT).show(); }
        });

        btnPrimaryWorkflowAction.setOnClickListener(v -> handlePrimaryWorkflowClick());
        btnReportIssue.setOnClickListener(v -> showReportIssueDialog());
    }

    private void loadOrderDetail() {
        pbDetailAction.setVisibility(View.VISIBLE);
        ApiClient.getApiService().getDeliveryOrderDetail(orderId).enqueue(new Callback<ApiResponse<Order>>() {
            @Override
            public void onResponse(Call<ApiResponse<Order>> call, Response<ApiResponse<Order>> response) {
                pbDetailAction.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    currentOrder = response.body().getData();
                    renderOrderDetails(currentOrder);
                } else {
                    Toast.makeText(DeliveryDetailActivity.this, "Could not load order details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Order>> call, Throwable t) {
                pbDetailAction.setVisibility(View.GONE);
                Toast.makeText(DeliveryDetailActivity.this, "Network error loading details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderOrderDetails(Order order) {
        tvDetailOrderNumber.setText("#" + order.getOrderNumber());

        String status = order.getStatus() != null ? order.getStatus() : "ASSIGNED";
        tvDetailStatusBadge.setText(status.replace('_', ' '));

        // Store
        tvDetailStoreName.setText(order.getStoreName());
        if (order.getStore() != null && order.getStore().getAddress() != null) {
            tvDetailStoreAddress.setText(order.getStore().getAddress());
        }

        // Customer
        tvDetailCustomerName.setText(order.getCustomerName() != null ? order.getCustomerName() : "Customer");
        tvDetailCustomerAddress.setText(order.getDeliveryAddress() != null ? order.getDeliveryAddress() : "Address on file");

        // Special instructions
        if (order.getSpecialInstructions() != null && !order.getSpecialInstructions().trim().isEmpty()) {
            layoutSpecialInstructions.setVisibility(View.VISIBLE);
            tvDetailSpecialInstructions.setText(order.getSpecialInstructions());
        } else {
            layoutSpecialInstructions.setVisibility(View.GONE);
        }

        // Payment Requirement
        boolean isCod = order.getPaymentMethod() != null && order.getPaymentMethod().contains("CASH");
        String formattedTotal = order.getGrandTotal() != null ? String.format("%.2f", order.getGrandTotal()) : "0.00";
        if (isCod) {
            cardPaymentRequirement.setCardBackgroundColor(getResources().getColor(R.color.colorAccent));
            tvPaymentInstruction.setText("💵 CASH ON DELIVERY: Collect ₹ " + formattedTotal);
        } else {
            cardPaymentRequirement.setCardBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            tvPaymentInstruction.setText("✓ PREPAID ONLINE: Do NOT collect cash from customer");
        }

        tvDetailGrandTotal.setText("₹ " + formattedTotal);

        // Populate items
        layoutOrderItemsContainer.removeAllViews();
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            for (OrderItem item : order.getItems()) {
                View itemView = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_2, layoutOrderItemsContainer, false);
                TextView text1 = itemView.findViewById(android.R.id.text1);
                TextView text2 = itemView.findViewById(android.R.id.text2);

                String pName = item.getProductName() != null ? item.getProductName() : (item.getProduct() != null ? item.getProduct().getName() : "Item");
                text1.setText("☑ " + pName + " (" + item.getUnit() + ")");
                text1.setTextColor(getResources().getColor(R.color.colorTextPrimary));
                text1.setTextSize(13);

                text2.setText("Qty: " + item.getQuantity() + " • ₹ " + (item.getTotalPrice() != null ? String.format("%.2f", item.getTotalPrice()) : ""));
                text2.setTextColor(getResources().getColor(R.color.colorTextSecondary));
                text2.setTextSize(12);

                layoutOrderItemsContainer.addView(itemView);
            }
        } else {
            TextView tvSingle = new TextView(this);
            tvSingle.setText("Standard Grocery Bag (1 package)");
            tvSingle.setTextColor(getResources().getColor(R.color.colorTextPrimary));
            layoutOrderItemsContainer.addView(tvSingle);
        }

        // Configure Action Buttons & Timeline based on Status
        configureWorkflowState(status);
    }

    private void configureWorkflowState(String status) {
        btnPrimaryWorkflowAction.setEnabled(true);
        btnReportIssue.setVisibility(View.VISIBLE);

        if ("READY_FOR_PICKUP".equals(status)) {
            tvTimelineSummary.setText("Pending Claim → Available for collection at store");
            btnPrimaryWorkflowAction.setText("Claim This Order");
            tvDetailStatusBadge.setBackgroundResource(R.drawable.bg_badge_orange);
        } else if ("ASSIGNED".equals(status)) {
            tvTimelineSummary.setText("Step 1: Assigned to you → Head to store for package pickup");
            btnPrimaryWorkflowAction.setText("Confirm Store Arrival & Pickup");
            tvDetailStatusBadge.setBackgroundResource(R.drawable.bg_badge_blue);
        } else if ("PICKED_UP".equals(status)) {
            tvTimelineSummary.setText("Step 2: Items picked up from store → Ready for transit");
            btnPrimaryWorkflowAction.setText("Start Delivery (Out for Delivery)");
            tvDetailStatusBadge.setBackgroundResource(R.drawable.bg_badge_orange);
        } else if ("OUT_FOR_DELIVERY".equals(status)) {
            tvTimelineSummary.setText("Step 3: In Transit → Deliver to customer address");
            btnPrimaryWorkflowAction.setText("Confirm Delivery Handover");
            tvDetailStatusBadge.setBackgroundResource(R.drawable.bg_badge_green);
        } else if ("DELIVERED".equals(status)) {
            tvTimelineSummary.setText("Step 4: Successfully Delivered at " + (currentOrder.getDeliveredAt() != null ? currentOrder.getDeliveredAt() : "customer location"));
            btnPrimaryWorkflowAction.setText("✓ Order Completed");
            btnPrimaryWorkflowAction.setEnabled(false);
            btnReportIssue.setVisibility(View.GONE);
            tvDetailStatusBadge.setBackgroundResource(R.drawable.bg_badge_green);
        } else if ("DELIVERY_FAILED".equals(status)) {
            tvTimelineSummary.setText("⚠️ Delivery Failed (" + (currentOrder.getDeliveryIssueReason() != null ? currentOrder.getDeliveryIssueReason() : "Issue reported") + ")");
            btnPrimaryWorkflowAction.setText("Delivery Attempt Recorded");
            btnPrimaryWorkflowAction.setEnabled(false);
            btnReportIssue.setVisibility(View.GONE);
            tvDetailStatusBadge.setBackgroundResource(R.drawable.bg_badge_orange);
        } else {
            tvTimelineSummary.setText("Status: " + status);
            btnPrimaryWorkflowAction.setEnabled(false);
        }
    }

    private void handlePrimaryWorkflowClick() {
        if (currentOrder == null) return;
        String status = currentOrder.getStatus() != null ? currentOrder.getStatus() : "";

        if ("READY_FOR_PICKUP".equals(status)) {
            // Claim
            btnPrimaryWorkflowAction.setEnabled(false);
            pbDetailAction.setVisibility(View.VISIBLE);
            ApiClient.getApiService().claimDeliveryOrder(orderId).enqueue(new StatusUpdateCallback("ASSIGNED"));
        } else if ("ASSIGNED".equals(status)) {
            // Pickup confirmation checklist
            new AlertDialog.Builder(this)
                    .setTitle("Confirm Store Pickup")
                    .setMessage("Have you verified all items in the grocery bag with the store manager?\n\nStore: " + currentOrder.getStoreName())
                    .setPositiveButton("Confirm Collection", (dialog, which) -> {
                        btnPrimaryWorkflowAction.setEnabled(false);
                        pbDetailAction.setVisibility(View.VISIBLE);
                        ApiClient.getApiService().confirmPickupOrder(orderId).enqueue(new StatusUpdateCallback("PICKED_UP"));
                    })
                    .setNegativeButton("Check Items", null)
                    .show();
        } else if ("PICKED_UP".equals(status)) {
            // Out for delivery
            btnPrimaryWorkflowAction.setEnabled(false);
            pbDetailAction.setVisibility(View.VISIBLE);
            ApiClient.getApiService().markOutForDelivery(orderId).enqueue(new StatusUpdateCallback("OUT_FOR_DELIVERY"));
        } else if ("OUT_FOR_DELIVERY".equals(status)) {
            // Show delivery handover dialog
            showCompleteDeliveryDialog();
        }
    }

    private void showCompleteDeliveryDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_delivery_complete, null);
        TextView tvCash = dialogView.findViewById(R.id.tvCompleteCashReminder);
        EditText etNotes = dialogView.findViewById(R.id.etDeliveryCompletionNotes);

        boolean isCod = currentOrder.getPaymentMethod() != null && currentOrder.getPaymentMethod().contains("CASH");
        String formattedTotal = currentOrder.getGrandTotal() != null ? String.format("%.2f", currentOrder.getGrandTotal()) : "0.00";

        if (isCod) {
            tvCash.setText("💵 COD: Collect ₹ " + formattedTotal + " from customer before completing.");
        } else {
            tvCash.setText("✓ Prepaid Online: Handover package without collecting cash.");
        }

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Complete Delivery", (dialog, which) -> {
                    btnPrimaryWorkflowAction.setEnabled(false);
                    pbDetailAction.setVisibility(View.VISIBLE);
                    String notes = etNotes.getText().toString().trim();
                    Map<String, String> body = new HashMap<>();
                    if (!notes.isEmpty()) body.put("notes", notes);

                    ApiClient.getApiService().completeDeliveryOrder(orderId, body).enqueue(new StatusUpdateCallback("DELIVERED"));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showReportIssueDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_delivery_issue, null);
        Spinner spnReason = dialogView.findViewById(R.id.spnIssueReason);
        EditText etDetails = dialogView.findViewById(R.id.etIssueDetails);

        String[] reasons = {
                "CUSTOMER_UNREACHABLE",
                "INCORRECT_ADDRESS",
                "CUSTOMER_REJECTED",
                "DAMAGED_PACKAGE",
                "STORE_DELAY",
                "OTHER"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, reasons);
        spnReason.setAdapter(adapter);

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Submit Issue", (dialog, which) -> {
                    String selectedReason = spnReason.getSelectedItem().toString();
                    String details = etDetails.getText().toString().trim();
                    if (details.isEmpty()) {
                        details = "Issue reported by delivery partner: " + selectedReason;
                    }

                    btnReportIssue.setEnabled(false);
                    pbDetailAction.setVisibility(View.VISIBLE);
                    DeliveryIssueReportRequest request = new DeliveryIssueReportRequest(selectedReason, details);

                    ApiClient.getApiService().reportDeliveryIssue(orderId, request).enqueue(new StatusUpdateCallback("DELIVERY_FAILED"));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private class StatusUpdateCallback implements Callback<ApiResponse<Order>> {
        private final String targetStatus;

        StatusUpdateCallback(String targetStatus) {
            this.targetStatus = targetStatus;
        }

        @Override
        public void onResponse(Call<ApiResponse<Order>> call, Response<ApiResponse<Order>> response) {
            pbDetailAction.setVisibility(View.GONE);
            btnPrimaryWorkflowAction.setEnabled(true);
            btnReportIssue.setEnabled(true);

            if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                currentOrder = response.body().getData();
                renderOrderDetails(currentOrder);
                Toast.makeText(DeliveryDetailActivity.this, "Status updated: " + targetStatus, Toast.LENGTH_SHORT).show();

                if ("OUT_FOR_DELIVERY".equalsIgnoreCase(targetStatus)) {
                    Long riderId = com.rucart.app.util.SessionManager.getInstance().getUserId();
                    String riderName = com.rucart.app.util.SessionManager.getInstance().getUserName();
                    com.rucart.app.util.FirebaseTrackingManager.getInstance().startRiderTracking(DeliveryDetailActivity.this, orderId, riderId, riderName);
                } else if ("DELIVERED".equalsIgnoreCase(targetStatus) || "DELIVERY_FAILED".equalsIgnoreCase(targetStatus) || "CANCELLED".equalsIgnoreCase(targetStatus)) {
                    com.rucart.app.util.FirebaseTrackingManager.getInstance().stopRiderTracking();
                }
            } else {
                String msg = "Could not update order status.";
                if (response.body() != null && response.body().getMessage() != null) {
                    msg = response.body().getMessage();
                }
                Toast.makeText(DeliveryDetailActivity.this, msg, Toast.LENGTH_LONG).show();
                loadOrderDetail();
            }
        }

        @Override
        public void onFailure(Call<ApiResponse<Order>> call, Throwable t) {
            pbDetailAction.setVisibility(View.GONE);
            btnPrimaryWorkflowAction.setEnabled(true);
            btnReportIssue.setEnabled(true);
            Toast.makeText(DeliveryDetailActivity.this, "Network error updating status", Toast.LENGTH_SHORT).show();
        }
    }

    private void copyToClipboard(String text, String successMsg) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("RUCART Delivery Address", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, successMsg, Toast.LENGTH_SHORT).show();
    }

    private void openNavigation(String address) {
        try {
            Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(address));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            startActivity(mapIntent);
        } catch (Exception e) {
            copyToClipboard(address, "Address copied (No map application found)");
        }
    }

    private void openDialer(String phone) {
        try {
            Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
            startActivity(callIntent);
        } catch (Exception e) {
            copyToClipboard(phone, "Phone number copied: " + phone);
        }
    }
}
