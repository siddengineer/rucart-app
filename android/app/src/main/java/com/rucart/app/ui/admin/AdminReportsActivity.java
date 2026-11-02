package com.rucart.app.ui.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.rucart.app.R;
import com.rucart.app.api.ApiClient;
import com.rucart.app.model.*;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.math.BigDecimal;
import java.util.Map;

public class AdminReportsActivity extends AppCompatActivity {

    private ImageButton btnBackReports;
    private AppCompatButton btnProcessRefundPrompt;
    private TextView periodToday, periodWeek, periodMonth, periodAll;
    private TextView tvReportRevenue, tvReportRefunds, tvReportTotalOrders, tvReportCompletedCancelled, tvPaymentBreakdown;
    private ProgressBar pbReportsLoading;
    private RecyclerView rvStorePerformance;
    private StorePerformanceAdapter adapter;

    private String selectedPeriod = "week";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_reports);

        initViews();
        setupListeners();
        loadReport();
    }

    private void initViews() {
        btnBackReports = findViewById(R.id.btnBackReports);
        btnProcessRefundPrompt = findViewById(R.id.btnProcessRefundPrompt);
        periodToday = findViewById(R.id.periodToday);
        periodWeek = findViewById(R.id.periodWeek);
        periodMonth = findViewById(R.id.periodMonth);
        periodAll = findViewById(R.id.periodAll);
        tvReportRevenue = findViewById(R.id.tvReportRevenue);
        tvReportRefunds = findViewById(R.id.tvReportRefunds);
        tvReportTotalOrders = findViewById(R.id.tvReportTotalOrders);
        tvReportCompletedCancelled = findViewById(R.id.tvReportCompletedCancelled);
        tvPaymentBreakdown = findViewById(R.id.tvPaymentBreakdown);
        pbReportsLoading = findViewById(R.id.pbReportsLoading);
        rvStorePerformance = findViewById(R.id.rvStorePerformance);

        rvStorePerformance.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StorePerformanceAdapter();
        rvStorePerformance.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBackReports.setOnClickListener(v -> finish());
        btnProcessRefundPrompt.setOnClickListener(v -> showIssueRefundDialog());

        periodToday.setOnClickListener(v -> selectPeriod("today"));
        periodWeek.setOnClickListener(v -> selectPeriod("week"));
        periodMonth.setOnClickListener(v -> selectPeriod("month"));
        periodAll.setOnClickListener(v -> selectPeriod("all"));
    }

    private void selectPeriod(String period) {
        selectedPeriod = period;
        updatePeriodTabs();
        loadReport();
    }

    private void updatePeriodTabs() {
        updateTab(periodToday, selectedPeriod.equals("today"));
        updateTab(periodWeek, selectedPeriod.equals("week"));
        updateTab(periodMonth, selectedPeriod.equals("month"));
        updateTab(periodAll, selectedPeriod.equals("all"));
    }

    private void updateTab(TextView tab, boolean isSelected) {
        tab.setBackgroundResource(isSelected ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        tab.setTextColor(getColor(isSelected ? R.color.white : R.color.text_secondary));
    }

    private void loadReport() {
        pbReportsLoading.setVisibility(View.VISIBLE);
        ApiClient.getApiService().getAdminReports(selectedPeriod).enqueue(new Callback<ApiResponse<OperationalReport>>() {
            @Override
            public void onResponse(Call<ApiResponse<OperationalReport>> call, Response<ApiResponse<OperationalReport>> response) {
                pbReportsLoading.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    OperationalReport report = response.body().getData();
                    tvReportRevenue.setText("₹" + report.getTotalRevenue());
                    tvReportRefunds.setText("₹" + report.getTotalRefunds());
                    tvReportTotalOrders.setText(String.valueOf(report.getTotalOrders()));
                    tvReportCompletedCancelled.setText(report.getCompletedOrders() + " / " + report.getCancelledOrders());

                    StringBuilder pb = new StringBuilder();
                    if (report.getPaymentMethodBreakdown() != null && !report.getPaymentMethodBreakdown().isEmpty()) {
                        for (Map.Entry<String, Long> entry : report.getPaymentMethodBreakdown().entrySet()) {
                            if (pb.length() > 0) pb.append(" | ");
                            pb.append(entry.getKey()).append(": ").append(entry.getValue());
                        }
                    } else {
                        pb.append("CASH_ON_DELIVERY: ").append(report.getTotalOrders());
                    }
                    tvPaymentBreakdown.setText(pb.toString());

                    adapter.setItems(report.getStorePerformances());
                } else {
                    Toast.makeText(AdminReportsActivity.this, "Failed to load reports", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<OperationalReport>> call, Throwable t) {
                pbReportsLoading.setVisibility(View.GONE);
                Toast.makeText(AdminReportsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showIssueRefundDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 10);

        EditText etPaymentId = new EditText(this);
        etPaymentId.setHint("Payment / Transaction ID (e.g. 1)");
        etPaymentId.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(etPaymentId);

        EditText etAmount = new EditText(this);
        etAmount.setHint("Refund Amount in ₹ (e.g. 250.00)");
        etAmount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(etAmount);

        EditText etReason = new EditText(this);
        etReason.setHint("Refund Reason (e.g. Damaged product returned)");
        layout.addView(etReason);

        new AlertDialog.Builder(this)
                .setTitle("Process Administrative Refund")
                .setView(layout)
                .setPositiveButton("Process Refund", (d, w) -> {
                    String idStr = etPaymentId.getText().toString().trim();
                    String amtStr = etAmount.getText().toString().trim();
                    String reason = etReason.getText().toString().trim();

                    if (idStr.isEmpty() || amtStr.isEmpty()) {
                        Toast.makeText(this, "Payment ID and Amount are required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Long paymentId = Long.parseLong(idStr);
                    BigDecimal amount = new BigDecimal(amtStr);
                    RefundProcessRequest req = new RefundProcessRequest(amount, reason.isEmpty() ? "Customer dispute resolution" : reason);

                    ApiClient.getApiService().processRefundAdmin(paymentId, req).enqueue(new Callback<ApiResponse<Payment>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Payment>> call, Response<ApiResponse<Payment>> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                Toast.makeText(AdminReportsActivity.this, "Refund processed successfully!", Toast.LENGTH_SHORT).show();
                                loadReport();
                            } else {
                                Toast.makeText(AdminReportsActivity.this, "Failed to process refund", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Payment>> call, Throwable t) {
                            Toast.makeText(AdminReportsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
