package com.rucart.app.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rucart.app.R;
import com.rucart.app.api.ApiClient;
import com.rucart.app.model.ApiResponse;
import com.rucart.app.model.Notification;
import com.rucart.app.model.Order;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView btnMarkAllRead;
    private RecyclerView rvNotifications;
    private ProgressBar pbLoading;
    private LinearLayout llEmptyState, llErrorState;
    private TextView tvErrorMsg;
    private Button btnRetry;

    private NotificationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        initViews();
        setupRecyclerView();
        setupListeners();
        loadNotifications();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBackNotifications);
        btnMarkAllRead = findViewById(R.id.btnMarkAllRead);
        rvNotifications = findViewById(R.id.rvNotifications);
        pbLoading = findViewById(R.id.pbNotifications);
        llEmptyState = findViewById(R.id.llEmptyNotifications);
        llErrorState = findViewById(R.id.llErrorNotifications);
        tvErrorMsg = findViewById(R.id.tvErrorMsg);
        btnRetry = findViewById(R.id.btnRetryNotifications);
    }

    private void setupRecyclerView() {
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(this, this::handleNotificationClick);
        rvNotifications.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnRetry.setOnClickListener(v -> loadNotifications());

        btnMarkAllRead.setOnClickListener(v -> markAllAsRead());
    }

    private void loadNotifications() {
        pbLoading.setVisibility(View.VISIBLE);
        llEmptyState.setVisibility(View.GONE);
        llErrorState.setVisibility(View.GONE);

        ApiClient.getApiService().getMyNotifications().enqueue(new Callback<ApiResponse<List<Notification>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Notification>>> call, Response<ApiResponse<List<Notification>>> response) {
                pbLoading.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<Notification> list = response.body().getData();
                    if (list.isEmpty()) {
                        llEmptyState.setVisibility(View.VISIBLE);
                    } else {
                        adapter.setNotifications(list);
                    }
                } else {
                    llErrorState.setVisibility(View.VISIBLE);
                    tvErrorMsg.setText("Failed to load notifications: HTTP " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Notification>>> call, Throwable t) {
                pbLoading.setVisibility(View.GONE);
                llErrorState.setVisibility(View.VISIBLE);
                tvErrorMsg.setText("Network error: " + t.getMessage());
            }
        });
    }

    private void markAllAsRead() {
        btnMarkAllRead.setEnabled(false);
        ApiClient.getApiService().markAllNotificationsAsRead().enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
                btnMarkAllRead.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(NotificationsActivity.this, "All notifications marked as read", Toast.LENGTH_SHORT).show();
                    loadNotifications();
                } else {
                    Toast.makeText(NotificationsActivity.this, "Unable to update notifications", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                btnMarkAllRead.setEnabled(true);
                Toast.makeText(NotificationsActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleNotificationClick(Notification notification) {
        String refId = notification.getReferenceId();
        if (refId == null || refId.isBlank()) return;

        if (refId.startsWith("RUC")) {
            // Reference is an Order Number - find order or open customer orders
            Intent intent = new Intent(this, OrderDetailActivity.class);
            // Pass order number as fallback extra
            intent.putExtra("order_number", refId);
            startActivity(intent);
        } else if (refId.startsWith("TXN") || refId.startsWith("PAY") || refId.startsWith("RFND")) {
            Intent intent = new Intent(this, TransactionHistoryActivity.class);
            startActivity(intent);
        }
    }
}
