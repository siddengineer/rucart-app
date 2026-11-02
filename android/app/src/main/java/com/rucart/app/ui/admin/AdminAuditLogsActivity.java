package com.rucart.app.ui.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.rucart.app.R;
import com.rucart.app.api.ApiClient;
import com.rucart.app.model.ApiResponse;
import com.rucart.app.model.AuditLog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class AdminAuditLogsActivity extends AppCompatActivity {

    private ImageButton btnBackAudit, btnRefreshAudit;
    private TextView filterAuditAll, filterAuditStores, filterAuditUsers, filterAuditOrders, filterAuditPayments, tvEmptyAuditLogs;
    private ProgressBar pbAuditLoading;
    private RecyclerView rvAdminAuditLogs;
    private AuditLogAdapter adapter;

    private String currentTargetFilter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_audit_logs);

        initViews();
        setupListeners();
        loadAuditLogs();
    }

    private void initViews() {
        btnBackAudit = findViewById(R.id.btnBackAudit);
        btnRefreshAudit = findViewById(R.id.btnRefreshAudit);
        filterAuditAll = findViewById(R.id.filterAuditAll);
        filterAuditStores = findViewById(R.id.filterAuditStores);
        filterAuditUsers = findViewById(R.id.filterAuditUsers);
        filterAuditOrders = findViewById(R.id.filterAuditOrders);
        filterAuditPayments = findViewById(R.id.filterAuditPayments);
        tvEmptyAuditLogs = findViewById(R.id.tvEmptyAuditLogs);
        pbAuditLoading = findViewById(R.id.pbAuditLoading);
        rvAdminAuditLogs = findViewById(R.id.rvAdminAuditLogs);

        rvAdminAuditLogs.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AuditLogAdapter();
        rvAdminAuditLogs.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBackAudit.setOnClickListener(v -> finish());
        btnRefreshAudit.setOnClickListener(v -> loadAuditLogs());

        filterAuditAll.setOnClickListener(v -> setTargetFilter(null));
        filterAuditStores.setOnClickListener(v -> setTargetFilter("STORE"));
        filterAuditUsers.setOnClickListener(v -> setTargetFilter("USER"));
        filterAuditOrders.setOnClickListener(v -> setTargetFilter("ORDER"));
        filterAuditPayments.setOnClickListener(v -> setTargetFilter("PAYMENT"));
    }

    private void setTargetFilter(String targetType) {
        currentTargetFilter = targetType;
        updateFilterTabs();
        loadAuditLogs();
    }

    private void updateFilterTabs() {
        updateTab(filterAuditAll, currentTargetFilter == null);
        updateTab(filterAuditStores, "STORE".equals(currentTargetFilter));
        updateTab(filterAuditUsers, "USER".equals(currentTargetFilter));
        updateTab(filterAuditOrders, "ORDER".equals(currentTargetFilter));
        updateTab(filterAuditPayments, "PAYMENT".equals(currentTargetFilter));
    }

    private void updateTab(TextView tab, boolean isSelected) {
        tab.setBackgroundResource(isSelected ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        tab.setTextColor(getColor(isSelected ? R.color.white : R.color.text_secondary));
    }

    private void loadAuditLogs() {
        pbAuditLoading.setVisibility(View.VISIBLE);
        ApiClient.getApiService().getAdminAuditLogs(currentTargetFilter, null, null).enqueue(new Callback<ApiResponse<List<AuditLog>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<AuditLog>>> call, Response<ApiResponse<List<AuditLog>>> response) {
                pbAuditLoading.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<AuditLog> list = response.body().getData();
                    adapter.setLogs(list);
                    tvEmptyAuditLogs.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    Toast.makeText(AdminAuditLogsActivity.this, "Failed to load audit logs", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<AuditLog>>> call, Throwable t) {
                pbAuditLoading.setVisibility(View.GONE);
                Toast.makeText(AdminAuditLogsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
