package com.rucart.app.ui.delivery;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
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

import java.math.BigDecimal;
import java.util.List;

public class DeliveryHistoryActivity extends AppCompatActivity {

    private ImageView btnBackHistory;
    private TextView filterAll;
    private TextView filterToday;
    private TextView filterWeek;

    private TextView tvHistoryDeliveriesCount;
    private TextView tvHistoryPeriodEarnings;

    private SwipeRefreshLayout swipeRefreshHistory;
    private RecyclerView rvDeliveryHistory;
    private ProgressBar pbHistoryLoading;
    private LinearLayout layoutHistoryEmpty;

    private DeliveryHistoryAdapter adapter;
    private String currentPeriod = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_history);

        String initial = getIntent().getStringExtra("initial_period");
        if (initial != null && !initial.isEmpty()) {
            currentPeriod = initial;
        }

        initViews();
        setupListeners();
        updateFilterStyles();
        loadHistory();
    }

    private void initViews() {
        btnBackHistory = findViewById(R.id.btnBackHistory);
        filterAll = findViewById(R.id.filterAll);
        filterToday = findViewById(R.id.filterToday);
        filterWeek = findViewById(R.id.filterWeek);

        tvHistoryDeliveriesCount = findViewById(R.id.tvHistoryDeliveriesCount);
        tvHistoryPeriodEarnings = findViewById(R.id.tvHistoryPeriodEarnings);

        swipeRefreshHistory = findViewById(R.id.swipeRefreshHistory);
        rvDeliveryHistory = findViewById(R.id.rvDeliveryHistory);
        pbHistoryLoading = findViewById(R.id.pbHistoryLoading);
        layoutHistoryEmpty = findViewById(R.id.layoutHistoryEmpty);

        rvDeliveryHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DeliveryHistoryAdapter(this::openOrderDetail);
        rvDeliveryHistory.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBackHistory.setOnClickListener(v -> finish());

        filterAll.setOnClickListener(v -> {
            if (!"all".equals(currentPeriod)) {
                currentPeriod = "all";
                updateFilterStyles();
                loadHistory();
            }
        });

        filterToday.setOnClickListener(v -> {
            if (!"today".equals(currentPeriod)) {
                currentPeriod = "today";
                updateFilterStyles();
                loadHistory();
            }
        });

        filterWeek.setOnClickListener(v -> {
            if (!"week".equals(currentPeriod)) {
                currentPeriod = "week";
                updateFilterStyles();
                loadHistory();
            }
        });

        swipeRefreshHistory.setOnRefreshListener(this::loadHistory);
    }

    private void updateFilterStyles() {
        filterAll.setBackgroundResource("all".equals(currentPeriod) ? R.drawable.bg_button_primary : R.drawable.bg_button_secondary);
        filterAll.setTextColor(getResources().getColor("all".equals(currentPeriod) ? R.color.colorWhite : R.color.colorTextPrimary));

        filterToday.setBackgroundResource("today".equals(currentPeriod) ? R.drawable.bg_button_primary : R.drawable.bg_button_secondary);
        filterToday.setTextColor(getResources().getColor("today".equals(currentPeriod) ? R.color.colorWhite : R.color.colorTextPrimary));

        filterWeek.setBackgroundResource("week".equals(currentPeriod) ? R.drawable.bg_button_primary : R.drawable.bg_button_secondary);
        filterWeek.setTextColor(getResources().getColor("week".equals(currentPeriod) ? R.color.colorWhite : R.color.colorTextPrimary));
    }

    private void loadHistory() {
        pbHistoryLoading.setVisibility(View.VISIBLE);
        layoutHistoryEmpty.setVisibility(View.GONE);

        ApiClient.getApiService().getDeliveryHistory(currentPeriod).enqueue(new Callback<ApiResponse<List<Order>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Order>>> call, Response<ApiResponse<List<Order>>> response) {
                pbHistoryLoading.setVisibility(View.GONE);
                swipeRefreshHistory.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<Order> list = response.body().getData();
                    calculateAndRenderStats(list);
                    if (list.isEmpty()) {
                        rvDeliveryHistory.setVisibility(View.GONE);
                        layoutHistoryEmpty.setVisibility(View.VISIBLE);
                    } else {
                        rvDeliveryHistory.setVisibility(View.VISIBLE);
                        layoutHistoryEmpty.setVisibility(View.GONE);
                        adapter.setOrders(list);
                    }
                } else {
                    rvDeliveryHistory.setVisibility(View.GONE);
                    layoutHistoryEmpty.setVisibility(View.VISIBLE);
                    tvHistoryDeliveriesCount.setText("0 deliveries");
                    tvHistoryPeriodEarnings.setText("₹ 0.00");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Order>>> call, Throwable t) {
                pbHistoryLoading.setVisibility(View.GONE);
                swipeRefreshHistory.setRefreshing(false);
                rvDeliveryHistory.setVisibility(View.GONE);
                layoutHistoryEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    private void calculateAndRenderStats(List<Order> list) {
        long deliveredCount = list.stream().filter(o -> "DELIVERED".equals(o.getStatus())).count();
        BigDecimal earnings = BigDecimal.valueOf(deliveredCount).multiply(new BigDecimal("35.00"));

        tvHistoryDeliveriesCount.setText(deliveredCount + " delivered (" + list.size() + " total)");
        tvHistoryPeriodEarnings.setText("₹ " + String.format("%.2f", earnings));
    }

    private void openOrderDetail(Order order) {
        Intent intent = new Intent(this, DeliveryDetailActivity.class);
        intent.putExtra("order_id", order.getId());
        startActivity(intent);
    }
}
