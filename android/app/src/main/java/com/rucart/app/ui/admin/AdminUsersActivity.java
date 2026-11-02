package com.rucart.app.ui.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.rucart.app.R;
import com.rucart.app.api.ApiClient;
import com.rucart.app.model.ApiResponse;
import com.rucart.app.model.Role;
import com.rucart.app.model.User;
import com.rucart.app.model.UserStatusUpdateRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AdminUsersActivity extends AppCompatActivity implements AdminUserAdapter.UserActionListener {

    private ImageButton btnBackUsers, btnRefreshUsers;
    private EditText etSearchUsers;
    private TextView tabUserAll, tabUserCustomers, tabUserStoreOwners, tabUserDeliveryPartners, tvEmptyUsers;
    private ProgressBar pbUsersLoading;
    private RecyclerView rvAdminUsers;
    private AdminUserAdapter adapter;

    private List<User> allUsers = new ArrayList<>();
    private String currentRoleFilter = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_users);

        initViews();
        setupListeners();
        loadUsers();
    }

    private void initViews() {
        btnBackUsers = findViewById(R.id.btnBackUsers);
        btnRefreshUsers = findViewById(R.id.btnRefreshUsers);
        etSearchUsers = findViewById(R.id.etSearchUsers);
        tabUserAll = findViewById(R.id.tabUserAll);
        tabUserCustomers = findViewById(R.id.tabUserCustomers);
        tabUserStoreOwners = findViewById(R.id.tabUserStoreOwners);
        tabUserDeliveryPartners = findViewById(R.id.tabUserDeliveryPartners);
        tvEmptyUsers = findViewById(R.id.tvEmptyUsers);
        pbUsersLoading = findViewById(R.id.pbUsersLoading);
        rvAdminUsers = findViewById(R.id.rvAdminUsers);

        rvAdminUsers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminUserAdapter(this);
        rvAdminUsers.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBackUsers.setOnClickListener(v -> finish());
        btnRefreshUsers.setOnClickListener(v -> loadUsers());

        tabUserAll.setOnClickListener(v -> setRoleFilter("ALL"));
        tabUserCustomers.setOnClickListener(v -> setRoleFilter("CUSTOMER"));
        tabUserStoreOwners.setOnClickListener(v -> setRoleFilter("STORE_OWNER"));
        tabUserDeliveryPartners.setOnClickListener(v -> setRoleFilter("DELIVERY_PARTNER"));

        etSearchUsers.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int count, int after) {
                applyFilter();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setRoleFilter(String role) {
        currentRoleFilter = role;
        updateRoleTabs();
        applyFilter();
    }

    private void updateRoleTabs() {
        updateTab(tabUserAll, currentRoleFilter.equals("ALL"));
        updateTab(tabUserCustomers, currentRoleFilter.equals("CUSTOMER"));
        updateTab(tabUserStoreOwners, currentRoleFilter.equals("STORE_OWNER"));
        updateTab(tabUserDeliveryPartners, currentRoleFilter.equals("DELIVERY_PARTNER"));
    }

    private void updateTab(TextView tab, boolean isSelected) {
        tab.setBackgroundResource(isSelected ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        tab.setTextColor(getColor(isSelected ? R.color.white : R.color.text_secondary));
    }

    private void loadUsers() {
        pbUsersLoading.setVisibility(View.VISIBLE);
        ApiClient.getApiService().getAdminUsers().enqueue(new Callback<ApiResponse<List<User>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<User>>> call, Response<ApiResponse<List<User>>> response) {
                pbUsersLoading.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    allUsers = response.body().getData();
                    applyFilter();
                } else {
                    Toast.makeText(AdminUsersActivity.this, "Failed to load users", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<User>>> call, Throwable t) {
                pbUsersLoading.setVisibility(View.GONE);
                Toast.makeText(AdminUsersActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFilter() {
        String query = etSearchUsers.getText().toString().trim().toLowerCase();
        List<User> filtered = allUsers.stream()
                .filter(u -> {
                    String role = (u.getRole() != null) ? u.getRole().name() : "CUSTOMER";
                    if (!currentRoleFilter.equals("ALL") && !role.equals(currentRoleFilter)) {
                        return false;
                    }

                    if (!query.isEmpty()) {
                        String name = (u.getName() != null) ? u.getName().toLowerCase() : "";
                        String phone = (u.getPhone() != null) ? u.getPhone() : "";
                        String email = (u.getEmail() != null) ? u.getEmail().toLowerCase() : "";
                        return name.contains(query) || phone.contains(query) || email.contains(query);
                    }
                    return true;
                })
                .collect(Collectors.toList());

        adapter.setUsers(filtered);
        tvEmptyUsers.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onToggleStatus(User user) {
        boolean newActive = !user.isActive();
        String actionTitle = newActive ? "Reactivate User" : "Suspend User";
        String prompt = newActive
                ? "Reactivate account for " + user.getName() + "?"
                : "Suspend account for " + user.getName() + "? Suspended users cannot log in or place orders.";

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 10);

        EditText etReason = new EditText(this);
        etReason.setHint("Reason for status change (optional)");
        layout.addView(etReason);

        new AlertDialog.Builder(this)
                .setTitle(actionTitle)
                .setMessage(prompt)
                .setView(layout)
                .setPositiveButton(newActive ? "Reactivate" : "Suspend", (d, w) -> {
                    String reason = etReason.getText().toString().trim();
                    UserStatusUpdateRequest req = new UserStatusUpdateRequest(newActive, reason.isEmpty() ? "Admin status update" : reason);

                    ApiClient.getApiService().toggleUserStatus(user.getId(), req).enqueue(new Callback<ApiResponse<User>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                user.setActive(newActive);
                                adapter.notifyDataSetChanged();
                                Toast.makeText(AdminUsersActivity.this, "User status updated!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(AdminUsersActivity.this, "Failed to update user status", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                            Toast.makeText(AdminUsersActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onViewDetails(User user) {
        StringBuilder sb = new StringBuilder();
        sb.append("User ID: #").append(user.getId()).append("\n");
        sb.append("Name: ").append(user.getName()).append("\n");
        sb.append("Role: ").append(user.getRole()).append("\n");
        sb.append("Status: ").append(user.isActive() ? "Active" : "Suspended").append("\n");
        sb.append("Phone: +91 ").append(user.getPhone()).append("\n");
        sb.append("Email: ").append(user.getEmail() != null ? user.getEmail() : "None registered").append("\n");
        sb.append("Address: ").append(user.getAddress() != null ? user.getAddress() : "None").append("\n\n");
        sb.append("Security Notice: Passwords and tokens are strictly shielded and never exposed.");

        new AlertDialog.Builder(this)
                .setTitle("User Profile")
                .setMessage(sb.toString())
                .setPositiveButton("Close", null)
                .show();
    }
}
