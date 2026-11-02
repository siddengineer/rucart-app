package com.rucart.app.ui.delivery;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.rucart.app.R;
import com.rucart.app.api.ApiClient;
import com.rucart.app.model.ApiResponse;
import com.rucart.app.model.Order;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class DeliveryOrdersActivity extends AppCompatActivity {

    public static final String TAB_AVAILABLE = "available";
    public static final String TAB_ACTIVE = "active";

    private ImageView btnBack;
    private TextView tvOrdersTitle;
    private TextView tabAvailable;
    private TextView tabActive;

    private SwipeRefreshLayout swipeRefreshOrders;
    private RecyclerView rvDeliveryOrders;
    private ProgressBar pbOrdersLoading;
    private LinearLayout layoutOrdersEmpty;
    private TextView tvEmptyTitle;
    private TextView tvEmptySubtitle;
    private AppCompatButton btnRefreshEmpty;

    private DeliveryOrderAdapter adapter;
    private String currentTab = TAB_AVAILABLE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_orders);

        String initialTab = getIntent().getStringExtra("initial_tab");
        if (initialTab != null && !initialTab.isEmpty()) {
            currentTab = initialTab;
        }

        initViews();
        setupListeners();
        updateTabStyles();
        loadOrders();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrders();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvOrdersTitle = findViewById(R.id.tvOrdersTitle);
        tabAvailable = findViewById(R.id.tabAvailable);
        tabActive = findViewById(R.id.tabActive);

        swipeRefreshOrders = findViewById(R.id.swipeRefreshOrders);
        rvDeliveryOrders = findViewById(R.id.rvDeliveryOrders);
        pbOrdersLoading = findViewById(R.id.pbOrdersLoading);
        layoutOrdersEmpty = findViewById(R.id.layoutOrdersEmpty);
        tvEmptyTitle = findViewById(R.id.tvEmptyTitle);
        tvEmptySubtitle = findViewById(R.id.tvEmptySubtitle);
        btnRefreshEmpty = findViewById(R.id.btnRefreshEmpty);

        rvDeliveryOrders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DeliveryOrderAdapter((order, isClaimAction) -> {
            if (isClaimAction) {
                confirmAndClaimOrder(order);
            } else {
                openOrderDetail(order);
            }
        });
        rvDeliveryOrders.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        tabAvailable.setOnClickListener(v -> {
            if (!TAB_AVAILABLE.equals(currentTab)) {
                currentTab = TAB_AVAILABLE;
                updateTabStyles();
                loadOrders();
            }
        });

        tabActive.setOnClickListener(v -> {
            if (!TAB_ACTIVE.equals(currentTab)) {
                currentTab = TAB_ACTIVE;
                updateTabStyles();
                loadOrders();
            }
        });

        swipeRefreshOrders.setOnRefreshListener(this::loadOrders);
        btnRefreshEmpty.setOnClickListener(v -> loadOrders());
    }

    private void updateTabStyles() {
        boolean isAvail = TAB_AVAILABLE.equals(currentTab);
        tabAvailable.setBackgroundResource(isAvail ? R.drawable.bg_button_primary : R.drawable.bg_button_secondary);
        tabAvailable.setTextColor(getResources().getColor(isAvail ? R.color.colorWhite : R.color.colorTextPrimary));

        tabActive.setBackgroundResource(!isAvail ? R.drawable.bg_button_primary : R.drawable.bg_button_secondary);
        tabActive.setTextColor(getResources().getColor(!isAvail ? R.color.colorWhite : R.color.colorTextPrimary));

        tvOrdersTitle.setText(isAvail ? "Orders Ready for Pickup" : "My Active Deliveries");
    }

    private void loadOrders() {
        pbOrdersLoading.setVisibility(View.VISIBLE);
        layoutOrdersEmpty.setVisibility(View.GONE);

        if (TAB_AVAILABLE.equals(currentTab)) {
            ApiClient.getApiService().getAvailableDeliveryOrders().enqueue(new Callback<ApiResponse<List<Order>>>() {
                @Override
                public void onResponse(Call<ApiResponse<List<Order>>> call, Response<ApiResponse<List<Order>>> response) {
                    handleOrdersResponse(response);
                }

                @Override
                public void onFailure(Call<ApiResponse<List<Order>>> call, Throwable t) {
                    handleOrdersFailure();
                }
            });
        } else {
            ApiClient.getApiService().getMyActiveDeliveries().enqueue(new Callback<ApiResponse<List<Order>>>() {
                @Override
                public void onResponse(Call<ApiResponse<List<Order>>> call, Response<ApiResponse<List<Order>>> response) {
                    handleOrdersResponse(response);
                }

                @Override
                public void onFailure(Call<ApiResponse<List<Order>>> call, Throwable t) {
                    handleOrdersFailure();
                }
            });
        }
    }

    private void handleOrdersResponse(Response<ApiResponse<List<Order>>> response) {
        pbOrdersLoading.setVisibility(View.GONE);
        swipeRefreshOrders.setRefreshing(false);

        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
            List<Order> list = response.body().getData();
            if (list.isEmpty()) {
                showEmptyState();
            } else {
                rvDeliveryOrders.setVisibility(View.VISIBLE);
                layoutOrdersEmpty.setVisibility(View.GONE);
                adapter.setOrders(list, TAB_AVAILABLE.equals(currentTab));
            }
        } else {
            showEmptyState();
        }
    }

    private void handleOrdersFailure() {
        pbOrdersLoading.setVisibility(View.GONE);
        swipeRefreshOrders.setRefreshing(false);
        showEmptyState();
    }

    private void showEmptyState() {
        rvDeliveryOrders.setVisibility(View.GONE);
        layoutOrdersEmpty.setVisibility(View.VISIBLE);

        if (TAB_AVAILABLE.equals(currentTab)) {
            tvEmptyTitle.setText("No Available Orders");
            tvEmptySubtitle.setText("There are currently no pending orders ready for pickup. Pull down to refresh or check again shortly.");
        } else {
            tvEmptyTitle.setText("No Active Deliveries");
            tvEmptySubtitle.setText("You have no deliveries in progress. Check the 'Available for Pickup' tab to claim available orders.");
        }
    }

    private void confirmAndClaimOrder(Order order) {
        new AlertDialog.Builder(this)
                .setTitle("Claim Delivery")
                .setMessage("Are you ready to deliver Order #" + order.getOrderNumber() + " from " + order.getStoreName() + "?\n\nOnce claimed, you will proceed to the store to collect the items.")
                .setPositiveButton("Claim Order", (dialog, which) -> executeClaim(order))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void executeClaim(Order order) {
        pbOrdersLoading.setVisibility(View.VISIBLE);
        ApiClient.getApiService().claimDeliveryOrder(order.getId()).enqueue(new Callback<ApiResponse<Order>>() {
            @Override
            public void onResponse(Call<ApiResponse<Order>> call, Response<ApiResponse<Order>> response) {
                pbOrdersLoading.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    Toast.makeText(DeliveryOrdersActivity.this, "Order #" + order.getOrderNumber() + " claimed successfully!", Toast.LENGTH_SHORT).show();
                    // Switch to active tab and open workflow
                    currentTab = TAB_ACTIVE;
                    updateTabStyles();
                    loadOrders();
                    openOrderDetail(response.body().getData());
                } else {
                    String msg = "Could not claim order.";
                    if (response.body() != null && response.body().getMessage() != null) {
                        msg = response.body().getMessage();
                    }
                    Toast.makeText(DeliveryOrdersActivity.this, msg, Toast.LENGTH_LONG).show();
                    loadOrders();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Order>> call, Throwable t) {
                pbOrdersLoading.setVisibility(View.GONE);
                Toast.makeText(DeliveryOrdersActivity.this, "Network error claiming order. Please retry.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openOrderDetail(Order order) {
        Intent intent = new Intent(this, DeliveryDetailActivity.class);
        intent.putExtra("order_id", order.getId());
        startActivity(intent);
    }
}
