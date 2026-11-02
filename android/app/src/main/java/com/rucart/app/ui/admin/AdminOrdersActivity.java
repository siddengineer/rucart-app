package com.rucart.app.ui.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.rucart.app.R;
import com.rucart.app.api.ApiClient;
import com.rucart.app.model.ApiResponse;
import com.rucart.app.model.Order;
import com.rucart.app.model.OrderItem;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AdminOrdersActivity extends AppCompatActivity implements AdminOrderAdapter.OrderActionListener {

    private ImageButton btnBackOrders, btnRefreshOrders;
    private EditText etSearchOrders;
    private TextView filterOrderAll, filterOrderPending, filterOrderDelivered, filterOrderCancelled, filterOrderRefunded, tvEmptyOrders;
    private ProgressBar pbOrdersLoading;
    private RecyclerView rvAdminOrders;
    private AdminOrderAdapter adapter;

    private List<Order> allOrders = new ArrayList<>();
    private String currentFilter = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_orders);

        initViews();
        setupListeners();
        loadOrders();
    }

    private void initViews() {
        btnBackOrders = findViewById(R.id.btnBackOrders);
        btnRefreshOrders = findViewById(R.id.btnRefreshOrders);
        etSearchOrders = findViewById(R.id.etSearchOrders);
        filterOrderAll = findViewById(R.id.filterOrderAll);
        filterOrderPending = findViewById(R.id.filterOrderPending);
        filterOrderDelivered = findViewById(R.id.filterOrderDelivered);
        filterOrderCancelled = findViewById(R.id.filterOrderCancelled);
        filterOrderRefunded = findViewById(R.id.filterOrderRefunded);
        tvEmptyOrders = findViewById(R.id.tvEmptyOrders);
        pbOrdersLoading = findViewById(R.id.pbOrdersLoading);
        rvAdminOrders = findViewById(R.id.rvAdminOrders);

        rvAdminOrders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminOrderAdapter(this);
        rvAdminOrders.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBackOrders.setOnClickListener(v -> finish());
        btnRefreshOrders.setOnClickListener(v -> loadOrders());

        filterOrderAll.setOnClickListener(v -> setFilter("ALL"));
        filterOrderPending.setOnClickListener(v -> setFilter("PENDING"));
        filterOrderDelivered.setOnClickListener(v -> setFilter("DELIVERED"));
        filterOrderCancelled.setOnClickListener(v -> setFilter("CANCELLED"));
        filterOrderRefunded.setOnClickListener(v -> setFilter("REFUNDED"));

        etSearchOrders.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int count, int after) {
                applyFilter();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setFilter(String filter) {
        currentFilter = filter;
        updateFilterChips();
        applyFilter();
    }

    private void updateFilterChips() {
        updateChip(filterOrderAll, currentFilter.equals("ALL"));
        updateChip(filterOrderPending, currentFilter.equals("PENDING"));
        updateChip(filterOrderDelivered, currentFilter.equals("DELIVERED"));
        updateChip(filterOrderCancelled, currentFilter.equals("CANCELLED"));
        updateChip(filterOrderRefunded, currentFilter.equals("REFUNDED"));
    }

    private void updateChip(TextView chip, boolean isSelected) {
        chip.setBackgroundResource(isSelected ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        chip.setTextColor(getColor(isSelected ? R.color.white : R.color.text_secondary));
    }

    private void loadOrders() {
        pbOrdersLoading.setVisibility(View.VISIBLE);
        ApiClient.getApiService().getAdminOrders(null, null, null, null, null).enqueue(new Callback<ApiResponse<List<Order>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Order>>> call, Response<ApiResponse<List<Order>>> response) {
                pbOrdersLoading.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    allOrders = response.body().getData();
                    applyFilter();
                } else {
                    Toast.makeText(AdminOrdersActivity.this, "Failed to load orders", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Order>>> call, Throwable t) {
                pbOrdersLoading.setVisibility(View.GONE);
                Toast.makeText(AdminOrdersActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFilter() {
        String query = etSearchOrders.getText().toString().trim().toLowerCase();
        List<Order> filtered = allOrders.stream()
                .filter(o -> {
                    String status = o.getStatus() != null ? o.getStatus(): "";
                    if (currentFilter.equals("PENDING")) {
                        if (status.equals("DELIVERED") || status.equals("CANCELLED") || status.equals("REFUNDED")) return false;
                    } else if (currentFilter.equals("DELIVERED") && !status.equals("DELIVERED")) {
                        return false;
                    } else if (currentFilter.equals("CANCELLED") && !status.equals("CANCELLED")) {
                        return false;
                    } else if (currentFilter.equals("REFUNDED") && !status.equals("REFUNDED")) {
                        return false;
                    }

                    if (!query.isEmpty()) {
                        String orderNum = o.getOrderNumber() != null ? o.getOrderNumber().toLowerCase() : "";
                        String cust = o.getCustomerName() != null ? o.getCustomerName().toLowerCase() : "";
                        String phone = o.getCustomerPhone() != null ? o.getCustomerPhone() : "";
                        return orderNum.contains(query) || cust.contains(query) || phone.contains(query);
                    }
                    return true;
                })
                .collect(Collectors.toList());

        adapter.setOrders(filtered);
        tvEmptyOrders.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onViewOrder(Order order) {
        StringBuilder sb = new StringBuilder();
        sb.append("Order #: ").append(order.getOrderNumber()).append("\n");
        sb.append("Status: ").append(order.getStatus()).append("\n");
        sb.append("Customer: ").append(order.getCustomerName()).append(" (").append(order.getCustomerPhone()).append(")\n");
        sb.append("Address: ").append(order.getDeliveryAddress()).append("\n");
        sb.append("Store: ").append(order.getStoreName() != null ? order.getStoreName() : (order.getStore() != null ? order.getStore().getName() : "Direct")).append("\n\n");

        sb.append("Items:\n");
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            for (OrderItem item : order.getItems()) {
                String name = (item.getProduct() != null) ? item.getProduct().getName() : "Product";
                sb.append("• ").append(name).append(" x").append(item.getQuantity())
                        .append(" - ₹").append(item.getSubtotal()).append("\n");
            }
        } else {
            sb.append("• Grocery Items (Standard snapshot)\n");
        }

        sb.append("\nSubtotal: ₹").append(order.getItemTotal() != null ? order.getItemTotal() : "0.00").append("\n");
        sb.append("Delivery Fee: ₹").append(order.getDeliveryFee() != null ? order.getDeliveryFee() : "0.00").append("\n");
        sb.append("Grand Total: ₹").append(order.getGrandTotal()).append("\n");
        sb.append("Payment: ").append(order.getPaymentMethod()).append(" (").append(order.getPaymentStatus()).append(")");

        new AlertDialog.Builder(this)
                .setTitle("Order Details")
                .setMessage(sb.toString())
                .setPositiveButton("Close", null)
                .show();
    }

    @Override
    public void onOverrideStatus(Order order) {
        String[] statuses = {"ACCEPTED", "PREPARING", "OUT_FOR_DELIVERY", "DELIVERED", "CANCELLED", "REFUNDED"};

        new AlertDialog.Builder(this)
                .setTitle("Override Status: #" + order.getOrderNumber())
                .setItems(statuses, (dialog, which) -> {
                    String selectedStatus = statuses[which];
                    promptForOverrideReason(order, selectedStatus);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void promptForOverrideReason(Order order, String newStatus) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 10);

        EditText etReason = new EditText(this);
        etReason.setHint("Enter reason for status override");
        layout.addView(etReason);

        new AlertDialog.Builder(this)
                .setTitle("Confirm Status -> " + newStatus)
                .setMessage("This sensitive change will be permanently logged in the admin audit trail.")
                .setView(layout)
                .setPositiveButton("Apply Override", (d, w) -> {
                    String reason = etReason.getText().toString().trim();
                    if (reason.isEmpty()) {
                        reason = "Administrative status override";
                    }

                    ApiClient.getApiService().overrideOrderStatusAdmin(order.getId(), newStatus, reason)
                            .enqueue(new Callback<ApiResponse<Order>>() {
                                @Override
                                public void onResponse(Call<ApiResponse<Order>> call, Response<ApiResponse<Order>> response) {
                                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                        Toast.makeText(AdminOrdersActivity.this, "Order #" + order.getOrderNumber() + " updated to " + newStatus, Toast.LENGTH_SHORT).show();
                                        loadOrders();
                                    } else {
                                        Toast.makeText(AdminOrdersActivity.this, "Failed to override status", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<ApiResponse<Order>> call, Throwable t) {
                                    Toast.makeText(AdminOrdersActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
