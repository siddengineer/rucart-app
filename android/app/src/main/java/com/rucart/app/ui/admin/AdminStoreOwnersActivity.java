package com.rucart.app.ui.admin;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.rucart.app.R;
import com.rucart.app.api.ApiClient;
import com.rucart.app.model.ApiResponse;
import com.rucart.app.model.Order;
import com.rucart.app.model.StoreOwnerRegisterRequest;
import com.rucart.app.model.StoreOwnerResponse;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminStoreOwnersActivity extends AppCompatActivity {

    private SwipeRefreshLayout swipeRefreshStoreOwners;
    private RecyclerView rvStoreOwners;
    private EditText etSearchStoreOwners;
    private ExtendedFloatingActionButton fabAddStoreOwner;

    private StoreOwnerAdapter adapter;
    private List<StoreOwnerResponse> ownerList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_store_owners);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        swipeRefreshStoreOwners = findViewById(R.id.swipeRefreshStoreOwners);
        rvStoreOwners = findViewById(R.id.rvStoreOwners);
        etSearchStoreOwners = findViewById(R.id.etSearchStoreOwners);
        fabAddStoreOwner = findViewById(R.id.fabAddStoreOwner);

        adapter = new StoreOwnerAdapter();
        rvStoreOwners.setLayoutManager(new LinearLayoutManager(this));
        rvStoreOwners.setAdapter(adapter);

        setupListeners();
        loadStoreOwners(null);
    }

    private void setupListeners() {
        swipeRefreshStoreOwners.setOnRefreshListener(() -> loadStoreOwners(etSearchStoreOwners.getText().toString().trim()));

        etSearchStoreOwners.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadStoreOwners(s.toString().trim());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        fabAddStoreOwner.setOnClickListener(v -> showRegisterStoreOwnerDialog());
    }

    private void loadStoreOwners(String search) {
        swipeRefreshStoreOwners.setRefreshing(true);
        ApiClient.getApiService().getAllStoreOwners(search).enqueue(new Callback<ApiResponse<List<StoreOwnerResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<StoreOwnerResponse>>> call, Response<ApiResponse<List<StoreOwnerResponse>>> response) {
                swipeRefreshStoreOwners.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    ownerList = response.body().getData();
                    adapter.setOwners(ownerList);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<StoreOwnerResponse>>> call, Throwable t) {
                swipeRefreshStoreOwners.setRefreshing(false);
                Toast.makeText(AdminStoreOwnersActivity.this, "Failed to load store owners: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showRegisterStoreOwnerDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_register_user, null);
        EditText etName = dialogView.findViewById(R.id.etRegName);
        EditText etPhone = dialogView.findViewById(R.id.etRegPhone);
        EditText etEmail = dialogView.findViewById(R.id.etRegEmail);
        EditText etAddress = dialogView.findViewById(R.id.etRegAddress);

        new AlertDialog.Builder(this)
                .setTitle("Register New Store Owner")
                .setView(dialogView)
                .setPositiveButton("Register", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String phone = etPhone.getText().toString().trim();
                    String email = etEmail.getText().toString().trim();
                    String address = etAddress.getText().toString().trim();

                    if (name.isEmpty() || phone.isEmpty()) {
                        Toast.makeText(this, "Name and mobile number are required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String storeName = name + "'s RUCART Store";
                    StoreOwnerRegisterRequest request = new StoreOwnerRegisterRequest(name, phone, email, address, storeName, "Pune Area");
                    ApiClient.getApiService().registerStoreOwner(request).enqueue(new Callback<ApiResponse<StoreOwnerResponse>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<StoreOwnerResponse>> call, Response<ApiResponse<StoreOwnerResponse>> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                                StoreOwnerResponse owner = response.body().getData();
                                String pwd = owner.getGeneratedPassword();

                                new AlertDialog.Builder(AdminStoreOwnersActivity.this)
                                        .setTitle("Store Owner Registered")
                                        .setMessage("Owner: " + owner.getName() + "\nPhone: " + owner.getPhone() +
                                                "\nStore: " + owner.getStoreName() +
                                                "\n\nGenerated Initial Password:\n" + pwd +
                                                "\n\nPlease securely copy and share this password with the owner.")
                                        .setPositiveButton("Copy Password", (d, w) -> {
                                            ClipboardManager cb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                            cb.setPrimaryClip(ClipData.newPlainText("Store Owner Password", pwd));
                                            Toast.makeText(AdminStoreOwnersActivity.this, "Password copied to clipboard", Toast.LENGTH_SHORT).show();
                                        })
                                        .setNegativeButton("Close", null)
                                        .show();

                                loadStoreOwners(null);
                            } else {
                                Toast.makeText(AdminStoreOwnersActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<StoreOwnerResponse>> call, Throwable t) {
                            Toast.makeText(AdminStoreOwnersActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showResetPasswordDialog(StoreOwnerResponse owner) {
        final EditText etNewPassword = new EditText(this);
        etNewPassword.setHint("Leave blank to auto-generate");

        new AlertDialog.Builder(this)
                .setTitle("Reset Password for " + owner.getName())
                .setMessage("Enter new password or leave blank:")
                .setView(etNewPassword)
                .setPositiveButton("Reset", (dialog, which) -> {
                    String pwd = etNewPassword.getText().toString().trim();
                    Map<String, String> body = new HashMap<>();
                    if (!pwd.isEmpty()) body.put("newPassword", pwd);

                    ApiClient.getApiService().resetStoreOwnerPassword(owner.getUserId(), body).enqueue(new Callback<ApiResponse<Map<String, String>>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Map<String, String>>> call, Response<ApiResponse<Map<String, String>>> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                                String newPwd = response.body().getData().get("newPassword");
                                new AlertDialog.Builder(AdminStoreOwnersActivity.this)
                                        .setTitle("Password Reset Successful")
                                        .setMessage("New Password:\n" + newPwd)
                                        .setPositiveButton("Copy", (d, w) -> {
                                            ClipboardManager cb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                            cb.setPrimaryClip(ClipData.newPlainText("New Password", newPwd));
                                            Toast.makeText(AdminStoreOwnersActivity.this, "Copied", Toast.LENGTH_SHORT).show();
                                        })
                                        .setNegativeButton("Close", null)
                                        .show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Map<String, String>>> call, Throwable t) {
                            Toast.makeText(AdminStoreOwnersActivity.this, "Failed to reset password", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void toggleOwnerStatus(StoreOwnerResponse owner) {
        boolean newStatus = !owner.isActive();
        Map<String, Boolean> body = new HashMap<>();
        body.put("active", newStatus);

        ApiClient.getApiService().updateStoreOwnerStatus(owner.getUserId(), body).enqueue(new Callback<ApiResponse<StoreOwnerResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<StoreOwnerResponse>> call, Response<ApiResponse<StoreOwnerResponse>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdminStoreOwnersActivity.this, "Account " + (newStatus ? "Activated" : "Deactivated"), Toast.LENGTH_SHORT).show();
                    loadStoreOwners(null);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<StoreOwnerResponse>> call, Throwable t) {
                Toast.makeText(AdminStoreOwnersActivity.this, "Failed to update status", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showOwnerOrders(StoreOwnerResponse owner) {
        ApiClient.getApiService().getStoreOwnerOrders(owner.getUserId()).enqueue(new Callback<ApiResponse<List<Order>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Order>>> call, Response<ApiResponse<List<Order>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<Order> orders = response.body().getData();
                    StringBuilder sb = new StringBuilder();
                    sb.append("Total Store Orders: ").append(orders.size()).append("\n\n");
                    BigDecimal sum = BigDecimal.ZERO;
                    for (Order o : orders) {
                        BigDecimal grandTotal = o.getGrandTotal() != null ? o.getGrandTotal() : BigDecimal.ZERO;
                        sum = sum.add(grandTotal);
                        sb.append("• Order #").append(o.getOrderNumber())
                                .append(" | ₹ ").append(String.format("%.2f", grandTotal))
                                .append(" | Status: ").append(o.getStatus())
                                .append("\n");
                    }
                    sb.append("\nTotal Sales Volume: ₹ ").append(String.format("%.2f", sum));

                    new AlertDialog.Builder(AdminStoreOwnersActivity.this)
                            .setTitle("Orders - " + owner.getStoreName())
                            .setMessage(sb.toString())
                            .setPositiveButton("Close", null)
                            .show();
                } else {
                    Toast.makeText(AdminStoreOwnersActivity.this, "No store orders found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Order>>> call, Throwable t) {
                Toast.makeText(AdminStoreOwnersActivity.this, "Failed to load orders", Toast.LENGTH_SHORT).show();
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

    private class StoreOwnerAdapter extends RecyclerView.Adapter<StoreOwnerAdapter.ViewHolder> {
        private List<StoreOwnerResponse> items = new ArrayList<>();

        void setOwners(List<StoreOwnerResponse> list) {
            this.items = list != null ? list : new ArrayList<>();
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_store_owner, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            StoreOwnerResponse owner = items.get(position);
            holder.tvOwnerItemName.setText(owner.getName());
            holder.tvOwnerItemStore.setText("Store: " + (owner.getStoreName() != null ? owner.getStoreName() : "Unassigned"));
            holder.tvOwnerItemPhone.setText(owner.getPhone() + (owner.getEmail() != null ? " • " + owner.getEmail() : ""));

            holder.tvOwnerItemStatus.setText(owner.isActive() ? "ACTIVE" : "INACTIVE");
            holder.tvOwnerItemStatus.setBackgroundColor(getResources().getColor(owner.isActive() ? R.color.primary_green : R.color.danger_red));
            holder.btnOwnerToggleStatus.setText(owner.isActive() ? "Deactivate" : "Activate");
            holder.btnOwnerToggleStatus.setTextColor(getResources().getColor(owner.isActive() ? R.color.danger_red : R.color.primary_green));

            holder.tvOwnerItemOrders.setText("Total Orders: " + owner.getTotalOrders());
            holder.tvOwnerItemSales.setText("Sales: ₹ " + String.format("%.2f", owner.getTotalSales()));

            holder.btnOwnerOrders.setOnClickListener(v -> showOwnerOrders(owner));
            holder.btnOwnerResetPwd.setOnClickListener(v -> showResetPasswordDialog(owner));
            holder.btnOwnerToggleStatus.setOnClickListener(v -> toggleOwnerStatus(owner));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvOwnerItemName, tvOwnerItemStatus, tvOwnerItemStore, tvOwnerItemPhone, tvOwnerItemOrders, tvOwnerItemSales;
            MaterialButton btnOwnerOrders, btnOwnerResetPwd, btnOwnerToggleStatus;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvOwnerItemName = itemView.findViewById(R.id.tvOwnerItemName);
                tvOwnerItemStatus = itemView.findViewById(R.id.tvOwnerItemStatus);
                tvOwnerItemStore = itemView.findViewById(R.id.tvOwnerItemStore);
                tvOwnerItemPhone = itemView.findViewById(R.id.tvOwnerItemPhone);
                tvOwnerItemOrders = itemView.findViewById(R.id.tvOwnerItemOrders);
                tvOwnerItemSales = itemView.findViewById(R.id.tvOwnerItemSales);
                btnOwnerOrders = itemView.findViewById(R.id.btnOwnerOrders);
                btnOwnerResetPwd = itemView.findViewById(R.id.btnOwnerResetPwd);
                btnOwnerToggleStatus = itemView.findViewById(R.id.btnOwnerToggleStatus);
            }
        }
    }
}
