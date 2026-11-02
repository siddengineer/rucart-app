package com.rucart.app.ui.customer;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rucart.app.R;
import com.rucart.app.api.ApiClient;
import com.rucart.app.model.ApiResponse;
import com.rucart.app.model.Payment;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransactionHistoryActivity extends AppCompatActivity {

    private ImageView btnBack;
    private RecyclerView rvTransactions;
    private LinearLayout llEmptyState;
    private ProgressBar pbLoading;
    private TransactionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);

        initViews();
        setupRecyclerView();
        loadTransactions();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBackTxnHistory);
        rvTransactions = findViewById(R.id.rvTransactions);
        llEmptyState = findViewById(R.id.llEmptyTxnState);
        pbLoading = findViewById(R.id.pbTxnLoading);

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new TransactionAdapter(this);
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvTransactions.setAdapter(adapter);
    }

    private void loadTransactions() {
        pbLoading.setVisibility(View.VISIBLE);
        llEmptyState.setVisibility(View.GONE);

        ApiClient.getApiService().getMyTransactions().enqueue(new Callback<ApiResponse<List<Payment>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Payment>>> call, Response<ApiResponse<List<Payment>>> response) {
                pbLoading.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<Payment> payments = response.body().getData();
                    if (payments.isEmpty()) {
                        llEmptyState.setVisibility(View.VISIBLE);
                    } else {
                        adapter.setPayments(payments);
                    }
                } else {
                    Toast.makeText(TransactionHistoryActivity.this, "Failed to load transactions", Toast.LENGTH_SHORT).show();
                    llEmptyState.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Payment>>> call, Throwable t) {
                pbLoading.setVisibility(View.GONE);
                llEmptyState.setVisibility(View.VISIBLE);
                Toast.makeText(TransactionHistoryActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
