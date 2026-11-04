package com.rucart.app.ui.admin;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
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
import com.rucart.app.model.RiderRegisterRequest;
import com.rucart.app.model.RiderResponse;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminRidersActivity extends AppCompatActivity {

    private SwipeRefreshLayout swipeRefreshRiders;
    private RecyclerView rvRiders;
    private EditText etSearchRiders;
    private MaterialButton btnViewLiveMap;
    private ExtendedFloatingActionButton fabAddRider;

    private RiderAdapter adapter;
    private List<RiderResponse> riderList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_riders);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        swipeRefreshRiders = findViewById(R.id.swipeRefreshRiders);
        rvRiders = findViewById(R.id.rvRiders);
        etSearchRiders = findViewById(R.id.etSearchRiders);
        btnViewLiveMap = findViewById(R.id.btnViewLiveMap);
        fabAddRider = findViewById(R.id.fabAddRider);

        adapter = new RiderAdapter();
        rvRiders.setLayoutManager(new LinearLayoutManager(this));
        rvRiders.setAdapter(adapter);

        setupListeners();
        loadRiders(null);
    }

    private void setupListeners() {
        swipeRefreshRiders.setOnRefreshListener(() -> loadRiders(etSearchRiders.getText().toString().trim()));

        etSearchRiders.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadRiders(s.toString().trim());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnViewLiveMap.setOnClickListener(v -> startActivity(new Intent(this, AdminLiveTrackingActivity.class)));

        fabAddRider.setOnClickListener(v -> showRegisterRiderDialog());
    }

    private void loadRiders(String search) {
        swipeRefreshRiders.setRefreshing(true);
        ApiClient.getApiService().getAllRiders(search).enqueue(new Callback<ApiResponse<List<RiderResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<RiderResponse>>> call, Response<ApiResponse<List<RiderResponse>>> response) {
                swipeRefreshRiders.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    riderList = response.body().getData();
                    adapter.setRiders(riderList);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<RiderResponse>>> call, Throwable t) {
                swipeRefreshRiders.setRefreshing(false);
                Toast.makeText(AdminRidersActivity.this, "Failed to load riders: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showRegisterRiderDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_register_user, null);
        EditText etName = dialogView.findViewById(R.id.etRegName);
        EditText etPhone = dialogView.findViewById(R.id.etRegPhone);
        EditText etEmail = dialogView.findViewById(R.id.etRegEmail);
        EditText etAddress = dialogView.findViewById(R.id.etRegAddress);

        new AlertDialog.Builder(this)
                .setTitle("Register New Delivery Rider")
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

                    RiderRegisterRequest request = new RiderRegisterRequest(name, phone, email, address, "Motorcycle", "MH-12-REG");
                    ApiClient.getApiService().registerRider(request).enqueue(new Callback<ApiResponse<RiderResponse>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<RiderResponse>> call, Response<ApiResponse<RiderResponse>> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                                RiderResponse rider = response.body().getData();
                                String generatedPassword = rider.getGeneratedPassword();

                                new AlertDialog.Builder(AdminRidersActivity.this)
                                        .setTitle("Rider Registered Successfully")
                                        .setMessage("Rider: " + rider.getName() + "\nPhone: " + rider.getPhone() +
                                                "\n\nGenerated Initial Password:\n" + generatedPassword +
                                                "\n\nPlease securely copy and share this password with the rider.")
                                        .setPositiveButton("Copy Password", (d, w) -> {
                                            ClipboardManager cb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                            cb.setPrimaryClip(ClipData.newPlainText("Rider Password", generatedPassword));
                                            Toast.makeText(AdminRidersActivity.this, "Password copied to clipboard", Toast.LENGTH_SHORT).show();
                                        })
                                        .setNegativeButton("Close", null)
                                        .show();

                                loadRiders(null);
                            } else {
                                Toast.makeText(AdminRidersActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<RiderResponse>> call, Throwable t) {
                            Toast.makeText(AdminRidersActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showResetPasswordDialog(RiderResponse rider) {
        final EditText etNewPassword = new EditText(this);
        etNewPassword.setHint("Leave blank to auto-generate");

        new AlertDialog.Builder(this)
                .setTitle("Reset Password for " + rider.getName())
                .setMessage("Enter a new password or leave blank to automatically generate one:")
                .setView(etNewPassword)
                .setPositiveButton("Reset", (dialog, which) -> {
                    String pwd = etNewPassword.getText().toString().trim();
                    Map<String, String> body = new HashMap<>();
                    if (!pwd.isEmpty()) body.put("newPassword", pwd);

                    ApiClient.getApiService().resetRiderPassword(rider.getUserId(), body).enqueue(new Callback<ApiResponse<Map<String, String>>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Map<String, String>>> call, Response<ApiResponse<Map<String, String>>> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                                String newPwd = response.body().getData().get("newPassword");
                                new AlertDialog.Builder(AdminRidersActivity.this)
                                        .setTitle("Password Reset Successful")
                                        .setMessage("New Password:\n" + newPwd)
                                        .setPositiveButton("Copy", (d, w) -> {
                                            ClipboardManager cb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                            cb.setPrimaryClip(ClipData.newPlainText("New Password", newPwd));
                                            Toast.makeText(AdminRidersActivity.this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
                                        })
                                        .setNegativeButton("Close", null)
                                        .show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Map<String, String>>> call, Throwable t) {
                            Toast.makeText(AdminRidersActivity.this, "Failed to reset password", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void toggleRiderStatus(RiderResponse rider) {
        String[] options = {"AVAILABLE (Active)", "SLEEP (Temporarily Unavailable)", "SUSPENDED (Deactivate)"};
        new AlertDialog.Builder(this)
                .setTitle("Update Status for " + rider.getName())
                .setItems(options, (dialog, which) -> {
                    String selectedStatus;
                    if (which == 0) selectedStatus = "AVAILABLE";
                    else if (which == 1) selectedStatus = "SLEEP";
                    else selectedStatus = "SUSPENDED";

                    Map<String, String> body = new HashMap<>();
                    body.put("status", selectedStatus);

                    ApiClient.getApiService().updateRiderStatus(rider.getUserId(), body).enqueue(new Callback<ApiResponse<RiderResponse>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<RiderResponse>> call, Response<ApiResponse<RiderResponse>> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(AdminRidersActivity.this, "Status updated to " + selectedStatus, Toast.LENGTH_SHORT).show();
                                loadRiders(null);
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<RiderResponse>> call, Throwable t) {
                            Toast.makeText(AdminRidersActivity.this, "Failed to update status", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .show();
    }

    private void showRiderOrders(RiderResponse rider) {
        ApiClient.getApiService().getRiderDeliveredOrders(rider.getUserId()).enqueue(new Callback<ApiResponse<List<Order>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Order>>> call, Response<ApiResponse<List<Order>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<Order> orders = response.body().getData();
                    StringBuilder sb = new StringBuilder();
                    sb.append("Delivered Orders: ").append(orders.size()).append("\n\n");
                    BigDecimal sum = BigDecimal.ZERO;
                    for (Order o : orders) {
                        BigDecimal grandTotal = o.getGrandTotal() != null ? o.getGrandTotal() : BigDecimal.ZERO;
                        sum = sum.add(grandTotal);
                        sb.append("• Order #").append(o.getOrderNumber())
                                .append(" | ₹ ").append(String.format("%.2f", grandTotal))
                                .append(" | ").append(o.getDeliveredAt() != null ? o.getDeliveredAt() : "Delivered")
                                .append("\n");
                    }
                    sb.append("\nTotal Delivered Amount: ₹ ").append(String.format("%.2f", sum));

                    new AlertDialog.Builder(AdminRidersActivity.this)
                            .setTitle("Order History - " + rider.getName())
                            .setMessage(sb.toString())
                            .setPositiveButton("Close", null)
                            .show();
                } else {
                    Toast.makeText(AdminRidersActivity.this, "No delivered orders found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Order>>> call, Throwable t) {
                Toast.makeText(AdminRidersActivity.this, "Failed to load order history", Toast.LENGTH_SHORT).show();
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

    private class RiderAdapter extends RecyclerView.Adapter<RiderAdapter.ViewHolder> {
        private List<RiderResponse> items = new ArrayList<>();

        void setRiders(List<RiderResponse> list) {
            this.items = list != null ? list : new ArrayList<>();
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_rider, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            RiderResponse rider = items.get(position);
            holder.tvRiderItemName.setText(rider.getName());
            holder.tvRiderItemPhone.setText(rider.getPhone() + (rider.getEmail() != null ? " • " + rider.getEmail() : ""));
            holder.tvRiderItemVehicle.setText((rider.getVehicleType() != null ? rider.getVehicleType() : "Vehicle") + " • " + (rider.getVehicleNumber() != null ? rider.getVehicleNumber() : ""));

            String status = rider.getStatus() != null ? rider.getStatus() : (rider.isActive() ? "AVAILABLE" : "SUSPENDED");
            holder.tvRiderItemStatus.setText(status);

            if ("SUSPENDED".equalsIgnoreCase(status) || !rider.isActive()) {
                holder.tvRiderItemStatus.setBackgroundColor(getResources().getColor(R.color.danger_red));
                holder.btnRiderToggleStatus.setText("Activate");
            } else if ("SLEEP".equalsIgnoreCase(status)) {
                holder.tvRiderItemStatus.setBackgroundColor(getResources().getColor(R.color.accent_orange));
                holder.btnRiderToggleStatus.setText("Manage Status");
            } else {
                holder.tvRiderItemStatus.setBackgroundColor(getResources().getColor(R.color.primary_green));
                holder.btnRiderToggleStatus.setText("Manage Status");
            }

            holder.tvRiderItemDeliveries.setText("Delivered: " + rider.getTotalDeliveries() + " orders");
            holder.tvRiderItemAmount.setText("Order Total: ₹ " + String.format("%.2f", rider.getTotalDeliveredAmount()));

            holder.btnRiderOrders.setOnClickListener(v -> showRiderOrders(rider));
            holder.btnRiderResetPwd.setOnClickListener(v -> showResetPasswordDialog(rider));
            holder.btnRiderToggleStatus.setOnClickListener(v -> toggleRiderStatus(rider));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvRiderItemName, tvRiderItemStatus, tvRiderItemPhone, tvRiderItemVehicle, tvRiderItemDeliveries, tvRiderItemAmount;
            MaterialButton btnRiderOrders, btnRiderResetPwd, btnRiderToggleStatus;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvRiderItemName = itemView.findViewById(R.id.tvRiderItemName);
                tvRiderItemStatus = itemView.findViewById(R.id.tvRiderItemStatus);
                tvRiderItemPhone = itemView.findViewById(R.id.tvRiderItemPhone);
                tvRiderItemVehicle = itemView.findViewById(R.id.tvRiderItemVehicle);
                tvRiderItemDeliveries = itemView.findViewById(R.id.tvRiderItemDeliveries);
                tvRiderItemAmount = itemView.findViewById(R.id.tvRiderItemAmount);
                btnRiderOrders = itemView.findViewById(R.id.btnRiderOrders);
                btnRiderResetPwd = itemView.findViewById(R.id.btnRiderResetPwd);
                btnRiderToggleStatus = itemView.findViewById(R.id.btnRiderToggleStatus);
            }
        }
    }
}
