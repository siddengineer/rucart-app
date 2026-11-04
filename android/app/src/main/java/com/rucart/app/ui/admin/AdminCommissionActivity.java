package com.rucart.app.ui.admin;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.rucart.app.R;
import com.rucart.app.api.ApiClient;
import com.rucart.app.model.ApiResponse;
import com.rucart.app.model.RiderCommissionDto;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminCommissionActivity extends AppCompatActivity {

    private TextView tvCurrentEffectiveCut;
    private TextView tvCommissionTypeDesc;
    private TextView tvLastUpdatedCommission;
    private RadioGroup rgCommissionType;
    private RadioButton rbFixed;
    private RadioButton rbPercentage;
    private EditText etCommissionRate;
    private EditText etCommissionReason;
    private MaterialButton btnSaveCommission;
    private TextView tvCommissionHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_commission);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        tvCurrentEffectiveCut = findViewById(R.id.tvCurrentEffectiveCut);
        tvCommissionTypeDesc = findViewById(R.id.tvCommissionTypeDesc);
        tvLastUpdatedCommission = findViewById(R.id.tvLastUpdatedCommission);
        rgCommissionType = findViewById(R.id.rgCommissionType);
        rbFixed = findViewById(R.id.rbFixed);
        rbPercentage = findViewById(R.id.rbPercentage);
        etCommissionRate = findViewById(R.id.etCommissionRate);
        etCommissionReason = findViewById(R.id.etCommissionReason);
        btnSaveCommission = findViewById(R.id.btnSaveCommission);
        tvCommissionHistory = findViewById(R.id.tvCommissionHistory);

        btnSaveCommission.setOnClickListener(v -> saveCommissionChanges());

        loadCommissionData();
    }

    private void loadCommissionData() {
        ApiClient.getApiService().getCurrentCommission().enqueue(new Callback<ApiResponse<RiderCommissionDto>>() {
            @Override
            public void onResponse(Call<ApiResponse<RiderCommissionDto>> call, Response<ApiResponse<RiderCommissionDto>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    RiderCommissionDto dto = response.body().getData();
                    renderCommission(dto);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<RiderCommissionDto>> call, Throwable t) {
                Toast.makeText(AdminCommissionActivity.this, "Failed to load commission data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderCommission(RiderCommissionDto dto) {
        String type = dto.getCommissionType();
        if ("PERCENTAGE".equalsIgnoreCase(type)) {
            tvCurrentEffectiveCut.setText(dto.getPercentageRate() + "% of order total");
            tvCommissionTypeDesc.setText("Type: PERCENTAGE rate per delivered order");
            rbPercentage.setChecked(true);
            etCommissionRate.setText(dto.getPercentageRate().toString());
        } else {
            tvCurrentEffectiveCut.setText("₹ " + String.format("%.2f", dto.getFixedAmount()) + " / order");
            tvCommissionTypeDesc.setText("Type: FIXED amount per delivered order");
            rbFixed.setChecked(true);
            etCommissionRate.setText(dto.getFixedAmount().toString());
        }

        tvLastUpdatedCommission.setText("Updated: " + (dto.getUpdatedAt() != null ? dto.getUpdatedAt() : "Default system settings") +
                (dto.getUpdatedBy() != null ? " by " + dto.getUpdatedBy() : ""));

        // Render history
        StringBuilder sb = new StringBuilder();
        if (dto.getHistory() != null && !dto.getHistory().isEmpty()) {
            for (RiderCommissionDto.HistoryItemDto h : dto.getHistory()) {
                sb.append("• ").append(h.getCreatedAt() != null ? h.getCreatedAt() : "Recorded").append(": ")
                        .append(h.getCommissionType()).append(" = ")
                        .append("PERCENTAGE".equals(h.getCommissionType()) ? h.getPercentageRate() + "%" : "₹ " + h.getFixedAmount())
                        .append(" (").append(h.getChangeReason() != null ? h.getChangeReason() : "Update").append(")")
                        .append("\n\n");
            }
        } else {
            sb.append("No previous commission changes recorded.");
        }
        tvCommissionHistory.setText(sb.toString().trim());
    }

    private void saveCommissionChanges() {
        String rateStr = etCommissionRate.getText().toString().trim();
        String reason = etCommissionReason.getText().toString().trim();

        if (rateStr.isEmpty()) {
            etCommissionRate.setError("Rate is required");
            return;
        }

        boolean isPercentage = rbPercentage.isChecked();
        String type = isPercentage ? "PERCENTAGE" : "FIXED";

        Map<String, Object> body = new HashMap<>();
        body.put("commissionType", type);
        if (isPercentage) {
            body.put("percentageRate", new BigDecimal(rateStr));
            body.put("fixedAmount", BigDecimal.ZERO);
        } else {
            body.put("fixedAmount", new BigDecimal(rateStr));
            body.put("percentageRate", BigDecimal.ZERO);
        }
        body.put("description", reason.isEmpty() ? "Updated by Admin" : reason);

        btnSaveCommission.setEnabled(false);
        ApiClient.getApiService().updateCommission(body).enqueue(new Callback<ApiResponse<RiderCommissionDto>>() {
            @Override
            public void onResponse(Call<ApiResponse<RiderCommissionDto>> call, Response<ApiResponse<RiderCommissionDto>> response) {
                btnSaveCommission.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    Toast.makeText(AdminCommissionActivity.this, "Commission successfully updated!", Toast.LENGTH_SHORT).show();
                    renderCommission(response.body().getData());
                } else {
                    Toast.makeText(AdminCommissionActivity.this, "Failed to update commission", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<RiderCommissionDto>> call, Throwable t) {
                btnSaveCommission.setEnabled(true);
                Toast.makeText(AdminCommissionActivity.this, "Network error updating commission", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
