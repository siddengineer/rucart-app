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
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.rucart.app.R;
import com.rucart.app.api.ApiClient;
import com.rucart.app.model.ApiResponse;
import com.rucart.app.model.Store;
import com.rucart.app.model.StoreAdminRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AdminStoresActivity extends AppCompatActivity implements AdminStoreAdapter.StoreActionListener {

    private ImageButton btnBackStores;
    private AppCompatButton btnAddStore;
    private EditText etSearchStores;
    private TextView filterStoreAll, filterStoreOpen, filterStoreActive, tvEmptyStores;
    private ProgressBar pbStoresLoading;
    private RecyclerView rvAdminStores;
    private AdminStoreAdapter adapter;

    private List<Store> allStores = new ArrayList<>();
    private String currentFilter = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_stores);

        initViews();
        setupListeners();
        loadStores();
    }

    private void initViews() {
        btnBackStores = findViewById(R.id.btnBackStores);
        btnAddStore = findViewById(R.id.btnAddStore);
        etSearchStores = findViewById(R.id.etSearchStores);
        filterStoreAll = findViewById(R.id.filterStoreAll);
        filterStoreOpen = findViewById(R.id.filterStoreOpen);
        filterStoreActive = findViewById(R.id.filterStoreActive);
        tvEmptyStores = findViewById(R.id.tvEmptyStores);
        pbStoresLoading = findViewById(R.id.pbStoresLoading);
        rvAdminStores = findViewById(R.id.rvAdminStores);

        rvAdminStores.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminStoreAdapter(this);
        rvAdminStores.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBackStores.setOnClickListener(v -> finish());
        btnAddStore.setOnClickListener(v -> showAddStoreDialog());

        filterStoreAll.setOnClickListener(v -> {
            currentFilter = "ALL";
            updateFilterChips();
            applyFilter();
        });

        filterStoreOpen.setOnClickListener(v -> {
            currentFilter = "OPEN";
            updateFilterChips();
            applyFilter();
        });

        filterStoreActive.setOnClickListener(v -> {
            currentFilter = "ACTIVE";
            updateFilterChips();
            applyFilter();
        });

        etSearchStores.addTextChangedListener(new TextWatcher() {
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

    private void updateFilterChips() {
        filterStoreAll.setBackgroundResource(currentFilter.equals("ALL") ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        filterStoreAll.setTextColor(getColor(currentFilter.equals("ALL") ? R.color.white : R.color.text_secondary));

        filterStoreOpen.setBackgroundResource(currentFilter.equals("OPEN") ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        filterStoreOpen.setTextColor(getColor(currentFilter.equals("OPEN") ? R.color.white : R.color.text_secondary));

        filterStoreActive.setBackgroundResource(currentFilter.equals("ACTIVE") ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        filterStoreActive.setTextColor(getColor(currentFilter.equals("ACTIVE") ? R.color.white : R.color.text_secondary));
    }

    private void loadStores() {
        pbStoresLoading.setVisibility(View.VISIBLE);
        ApiClient.getApiService().getAdminStores(null).enqueue(new Callback<ApiResponse<List<Store>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Store>>> call, Response<ApiResponse<List<Store>>> response) {
                pbStoresLoading.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    allStores = response.body().getData();
                    applyFilter();
                } else {
                    Toast.makeText(AdminStoresActivity.this, "Failed to load stores", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Store>>> call, Throwable t) {
                pbStoresLoading.setVisibility(View.GONE);
                Toast.makeText(AdminStoresActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFilter() {
        String query = etSearchStores.getText().toString().trim().toLowerCase();
        List<Store> filtered = allStores.stream()
                .filter(s -> {
                    if (currentFilter.equals("OPEN") && !s.isOpen()) return false;
                    if (currentFilter.equals("ACTIVE") && !s.isActive()) return false;
                    if (!query.isEmpty()) {
                        String name = s.getName() != null ? s.getName().toLowerCase() : "";
                        String address = s.getAddress() != null ? s.getAddress().toLowerCase() : "";
                        return name.contains(query) || address.contains(query);
                    }
                    return true;
                })
                .collect(Collectors.toList());

        adapter.setStores(filtered);
        tvEmptyStores.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onToggleOpen(Store store) {
        boolean newOpen = !store.isOpen();
        ApiClient.getApiService().toggleStoreStatusAdmin(store.getId(), null, newOpen).enqueue(new Callback<ApiResponse<Store>>() {
            @Override
            public void onResponse(Call<ApiResponse<Store>> call, Response<ApiResponse<Store>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    store.setOpen(newOpen);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(AdminStoresActivity.this, store.getName() + " is now " + (newOpen ? "OPEN" : "CLOSED"), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AdminStoresActivity.this, "Failed to update store status", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Store>> call, Throwable t) {
                Toast.makeText(AdminStoresActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onToggleActive(Store store) {
        boolean newActive = !store.isActive();
        String prompt = newActive ? "Activate " + store.getName() + "?" : "Deactivate " + store.getName() + "? Inactive stores will not receive customer orders.";
        new AlertDialog.Builder(this)
                .setTitle("Confirm Status Change")
                .setMessage(prompt)
                .setPositiveButton(newActive ? "Activate" : "Deactivate", (d, w) -> {
                    ApiClient.getApiService().toggleStoreStatusAdmin(store.getId(), newActive, null).enqueue(new Callback<ApiResponse<Store>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Store>> call, Response<ApiResponse<Store>> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                store.setActive(newActive);
                                adapter.notifyDataSetChanged();
                                Toast.makeText(AdminStoresActivity.this, "Status updated", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(AdminStoresActivity.this, "Failed to update active state", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Store>> call, Throwable t) {
                            Toast.makeText(AdminStoresActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onEditStore(Store store) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 10);

        EditText etName = new EditText(this);
        etName.setHint("Store Name");
        etName.setText(store.getName());
        layout.addView(etName);

        EditText etAddress = new EditText(this);
        etAddress.setHint("Address");
        etAddress.setText(store.getAddress());
        layout.addView(etAddress);

        EditText etCity = new EditText(this);
        etCity.setHint("City");
        etCity.setText(store.getCity());
        layout.addView(etCity);

        EditText etAreas = new EditText(this);
        etAreas.setHint("Service Areas");
        etAreas.setText(store.getServiceAreas());
        layout.addView(etAreas);

        EditText etPhone = new EditText(this);
        etPhone.setHint("Phone");
        etPhone.setText(store.getPhone());
        layout.addView(etPhone);

        new AlertDialog.Builder(this)
                .setTitle("Edit Store: " + store.getName())
                .setView(layout)
                .setPositiveButton("Save Changes", (d, w) -> {
                    StoreAdminRequest req = new StoreAdminRequest();
                    req.setName(etName.getText().toString().trim());
                    req.setAddress(etAddress.getText().toString().trim());
                    req.setCity(etCity.getText().toString().trim());
                    req.setServiceAreas(etAreas.getText().toString().trim());
                    req.setPhone(etPhone.getText().toString().trim());

                    ApiClient.getApiService().updateAdminStore(store.getId(), req).enqueue(new Callback<ApiResponse<Store>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Store>> call, Response<ApiResponse<Store>> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                Toast.makeText(AdminStoresActivity.this, "Store updated!", Toast.LENGTH_SHORT).show();
                                loadStores();
                            } else {
                                Toast.makeText(AdminStoresActivity.this, "Failed to update store", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Store>> call, Throwable t) {
                            Toast.makeText(AdminStoresActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAddStoreDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 10);

        EditText etName = new EditText(this);
        etName.setHint("Store Name (e.g. Rucart Express Deccan)");
        layout.addView(etName);

        EditText etAddress = new EditText(this);
        etAddress.setHint("Address (e.g. Deccan Gymkhana)");
        layout.addView(etAddress);

        EditText etCity = new EditText(this);
        etCity.setHint("City (e.g. Pune)");
        layout.addView(etCity);

        EditText etAreas = new EditText(this);
        etAreas.setHint("Service Areas (e.g. Deccan, Prabhat Rd)");
        layout.addView(etAreas);

        EditText etPhone = new EditText(this);
        etPhone.setHint("Store Contact Phone (10 digits)");
        layout.addView(etPhone);

        new AlertDialog.Builder(this)
                .setTitle("Add New Store")
                .setView(layout)
                .setPositiveButton("Create Store", (d, w) -> {
                    String name = etName.getText().toString().trim();
                    String address = etAddress.getText().toString().trim();
                    String city = etCity.getText().toString().trim();
                    String areas = etAreas.getText().toString().trim();
                    String phone = etPhone.getText().toString().trim();

                    if (name.isEmpty() || address.isEmpty() || phone.isEmpty()) {
                        Toast.makeText(this, "Store name, address, and phone are required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    StoreAdminRequest req = new StoreAdminRequest();
                    req.setName(name);
                    req.setAddress(address);
                    req.setCity(city.isEmpty() ? "Pune" : city);
                    req.setServiceAreas(areas.isEmpty() ? "Pune" : areas);
                    req.setPhone(phone);
                    req.setActive(true);
                    req.setOpen(true);

                    ApiClient.getApiService().createAdminStore(req).enqueue(new Callback<ApiResponse<Store>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Store>> call, Response<ApiResponse<Store>> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                Toast.makeText(AdminStoresActivity.this, "Store created successfully!", Toast.LENGTH_SHORT).show();
                                loadStores();
                            } else {
                                Toast.makeText(AdminStoresActivity.this, "Failed to create store", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Store>> call, Throwable t) {
                            Toast.makeText(AdminStoresActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
