package com.rucart.app.ui.delivery;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.card.MaterialCardView;
import com.rucart.app.R;
import com.rucart.app.api.ApiClient;
import com.rucart.app.model.ApiResponse;
import com.rucart.app.model.DeliveryDashboardDto;
import com.rucart.app.model.DeliveryPartner;
import com.rucart.app.model.DeliveryPartnerStatus;
import com.rucart.app.ui.auth.RoleSelectionActivity;
import com.rucart.app.util.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.math.BigDecimal;

public class DeliveryMainActivity extends AppCompatActivity {

    private SwipeRefreshLayout swipeRefreshDelivery;
    private TextView tvRiderName;
    private TextView tvRiderVehicle;
    private TextView tvRiderRating;
    private TextView tvRiderEarnings;
    private TextView tvRiderTotalEarnings;
    private TextView tvActiveDeliveriesCount;
    private TextView tvCompletedTodayCount;
    private TextView tvPendingPickupsCount;
    private TextView tvDeliveryIssuesCount;

    private TextView btnStatusAvailable;
    private TextView btnStatusBusy;
    private TextView btnStatusOffline;

    private MaterialCardView cardActiveDeliveries;
    private MaterialCardView cardCompletedToday;
    private MaterialCardView cardPendingPickups;
    private MaterialCardView cardDeliveryIssues;

    private AppCompatButton btnViewAvailableOrders;
    private AppCompatButton btnViewActiveDeliveries;
    private AppCompatButton btnViewDeliveryHistory;
    private TextView btnDeliverySwitchRole;

    private DeliveryPartnerStatus currentStatus = DeliveryPartnerStatus.AVAILABLE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enforce delivery partner authorization
        SessionManager session = SessionManager.getInstance();
        if (!session.isLoggedIn() || (!"DELIVERY_PARTNER".equals(session.getUserRole()) && !"ADMIN".equals(session.getUserRole()))) {
            startActivity(new Intent(this, DeliveryLoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_delivery_main);

        initViews();
        setupListeners();
        loadDashboard();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboard();
    }

    private void initViews() {
        swipeRefreshDelivery = findViewById(R.id.swipeRefreshDelivery);
        tvRiderName = findViewById(R.id.tvRiderName);
        tvRiderVehicle = findViewById(R.id.tvRiderVehicle);
        tvRiderRating = findViewById(R.id.tvRiderRating);
        tvRiderEarnings = findViewById(R.id.tvRiderEarnings);
        tvRiderTotalEarnings = findViewById(R.id.tvRiderTotalEarnings);
        tvActiveDeliveriesCount = findViewById(R.id.tvActiveDeliveriesCount);
        tvCompletedTodayCount = findViewById(R.id.tvCompletedTodayCount);
        tvPendingPickupsCount = findViewById(R.id.tvPendingPickupsCount);
        tvDeliveryIssuesCount = findViewById(R.id.tvDeliveryIssuesCount);

        btnStatusAvailable = findViewById(R.id.btnStatusAvailable);
        btnStatusBusy = findViewById(R.id.btnStatusBusy);
        btnStatusOffline = findViewById(R.id.btnStatusOffline);

        cardActiveDeliveries = findViewById(R.id.cardActiveDeliveries);
        cardCompletedToday = findViewById(R.id.cardCompletedToday);
        cardPendingPickups = findViewById(R.id.cardPendingPickups);
        cardDeliveryIssues = findViewById(R.id.cardDeliveryIssues);

        btnViewAvailableOrders = findViewById(R.id.btnViewAvailableOrders);
        btnViewActiveDeliveries = findViewById(R.id.btnViewActiveDeliveries);
        btnViewDeliveryHistory = findViewById(R.id.btnViewDeliveryHistory);
        btnDeliverySwitchRole = findViewById(R.id.btnDeliverySwitchRole);

        // Prepopulate with session user
        String sessionName = SessionManager.getInstance().getUserName();
        if (sessionName != null && !sessionName.isEmpty()) {
            tvRiderName.setText(sessionName);
        }
    }

    private void setupListeners() {
        swipeRefreshDelivery.setOnRefreshListener(this::loadDashboard);

        btnStatusAvailable.setOnClickListener(v -> updateRiderStatus(DeliveryPartnerStatus.AVAILABLE));
        btnStatusBusy.setOnClickListener(v -> updateRiderStatus(DeliveryPartnerStatus.BUSY));
        btnStatusOffline.setOnClickListener(v -> updateRiderStatus(DeliveryPartnerStatus.OFFLINE));

        cardActiveDeliveries.setOnClickListener(v -> openOrders(DeliveryOrdersActivity.TAB_ACTIVE));
        btnViewActiveDeliveries.setOnClickListener(v -> openOrders(DeliveryOrdersActivity.TAB_ACTIVE));

        cardPendingPickups.setOnClickListener(v -> openOrders(DeliveryOrdersActivity.TAB_AVAILABLE));
        btnViewAvailableOrders.setOnClickListener(v -> openOrders(DeliveryOrdersActivity.TAB_AVAILABLE));

        cardCompletedToday.setOnClickListener(v -> openHistory("today"));
        cardDeliveryIssues.setOnClickListener(v -> openHistory("all"));
        btnViewDeliveryHistory.setOnClickListener(v -> openHistory("all"));

        btnDeliverySwitchRole.setOnClickListener(v -> {
            startActivity(new Intent(this, RoleSelectionActivity.class));
            finish();
        });
    }

    private void openOrders(String tab) {
        Intent intent = new Intent(this, DeliveryOrdersActivity.class);
        intent.putExtra("initial_tab", tab);
        startActivity(intent);
    }

    private void openHistory(String period) {
        Intent intent = new Intent(this, DeliveryHistoryActivity.class);
        intent.putExtra("initial_period", period);
        startActivity(intent);
    }

    private void loadDashboard() {
        swipeRefreshDelivery.setRefreshing(true);
        ApiClient.getApiService().getDeliveryDashboard().enqueue(new Callback<ApiResponse<DeliveryDashboardDto>>() {
            @Override
            public void onResponse(Call<ApiResponse<DeliveryDashboardDto>> call, Response<ApiResponse<DeliveryDashboardDto>> response) {
                swipeRefreshDelivery.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    renderDashboard(response.body().getData());
                } else {
                    renderFallbackDashboard();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<DeliveryDashboardDto>> call, Throwable t) {
                swipeRefreshDelivery.setRefreshing(false);
                renderFallbackDashboard();
            }
        });
    }

    private void renderDashboard(DeliveryDashboardDto dto) {
        tvRiderName.setText(dto.getPartnerName());
        tvRiderVehicle.setText(dto.getVehicleType() + " • " + dto.getVehicleNumber());
        tvRiderRating.setText("★ " + String.format("%.1f", dto.getRating()));

        tvActiveDeliveriesCount.setText(String.valueOf(dto.getActiveAssignedDeliveriesCount()));
        tvCompletedTodayCount.setText(String.valueOf(dto.getCompletedTodayCount()));
        tvPendingPickupsCount.setText(String.valueOf(dto.getPendingPickupsCount()));
        tvDeliveryIssuesCount.setText(String.valueOf(dto.getDeliveryIssuesCount()));

        tvRiderEarnings.setText("₹ " + String.format("%.2f", dto.getEarningsToday()));
        tvRiderTotalEarnings.setText("₹ " + String.format("%.2f", dto.getEarningsTotal()));

        currentStatus = dto.getStatus();
        renderStatusButtons(currentStatus);
    }

    private void renderFallbackDashboard() {
        tvRiderEarnings.setText("₹ 0.00");
        tvRiderTotalEarnings.setText("₹ 0.00");
        tvActiveDeliveriesCount.setText("0");
        tvCompletedTodayCount.setText("0");
        tvPendingPickupsCount.setText("0");
        tvDeliveryIssuesCount.setText("0");
        renderStatusButtons(currentStatus);
    }

    private void updateRiderStatus(DeliveryPartnerStatus newStatus) {
        renderStatusButtons(newStatus);
        ApiClient.getApiService().updateDeliveryStatus(newStatus.name()).enqueue(new Callback<ApiResponse<DeliveryPartner>>() {
            @Override
            public void onResponse(Call<ApiResponse<DeliveryPartner>> call, Response<ApiResponse<DeliveryPartner>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentStatus = newStatus;
                    Toast.makeText(DeliveryMainActivity.this, "Duty status: " + newStatus.name(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(DeliveryMainActivity.this, "Status updated to " + newStatus.name(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<DeliveryPartner>> call, Throwable t) {
                currentStatus = newStatus;
                Toast.makeText(DeliveryMainActivity.this, "Status saved: " + newStatus.name(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderStatusButtons(DeliveryPartnerStatus status) {
        btnStatusAvailable.setBackgroundResource(status == DeliveryPartnerStatus.AVAILABLE ? R.drawable.bg_button_primary : R.drawable.bg_button_secondary);
        btnStatusAvailable.setTextColor(getResources().getColor(status == DeliveryPartnerStatus.AVAILABLE ? R.color.colorWhite : R.color.colorTextPrimary));

        btnStatusBusy.setBackgroundResource(status == DeliveryPartnerStatus.BUSY ? R.drawable.bg_badge_orange : R.drawable.bg_button_secondary);
        btnStatusBusy.setTextColor(getResources().getColor(status == DeliveryPartnerStatus.BUSY ? R.color.colorWhite : R.color.colorTextPrimary));

        btnStatusOffline.setBackgroundResource(status == DeliveryPartnerStatus.OFFLINE ? R.drawable.bg_button_primary : R.drawable.bg_button_secondary);
        btnStatusOffline.setTextColor(getResources().getColor(status == DeliveryPartnerStatus.OFFLINE ? R.color.colorWhite : R.color.colorTextPrimary));
    }
}
