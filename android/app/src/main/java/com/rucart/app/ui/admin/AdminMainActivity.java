package com.rucart.app.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import com.google.android.material.card.MaterialCardView;
import com.rucart.app.R;
import com.rucart.app.api.ApiClient;
import com.rucart.app.model.*;
import com.rucart.app.ui.auth.RoleSelectionActivity;
import com.rucart.app.util.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.math.BigDecimal;
import java.util.List;

public class AdminMainActivity extends AppCompatActivity {

    private TextView tvAdminStores, tvAdminCustomers, tvAdminProducts, tvAdminOrdersToday;
    private TextView tvAdminPendingOrders, tvAdminCancelledOrders, tvAdminRevenue, tvAdminInventoryAlerts;
    private TextView tvAdminGreeting;
    private ImageButton btnRefreshDashboard, btnAdminLogout;
    private MaterialCardView cardNavStores, cardNavProducts, cardNavOrders, cardNavUsers, cardNavReports, cardNavAuditLogs, cardInventoryAlerts, cardNavRiders, cardNavStoreOwners, cardNavInvoices, cardNavCommission, cardNavOffers;
    private AppCompatButton btnQuickAddStore, btnQuickAddProduct, btnAdminSwitchRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enforce admin authorization
        SessionManager session = SessionManager.getInstance();
        if (!session.isLoggedIn() || !"ADMIN".equals(session.getUserRole())) {
            startActivity(new Intent(this, AdminLoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_admin_main);

        initViews();
        setupListeners();
        loadDashboardStats();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardStats();
    }

    private void initViews() {
        tvAdminStores = findViewById(R.id.tvAdminStores);
        tvAdminCustomers = findViewById(R.id.tvAdminCustomers);
        tvAdminProducts = findViewById(R.id.tvAdminProducts);
        tvAdminOrdersToday = findViewById(R.id.tvAdminOrdersToday);
        tvAdminPendingOrders = findViewById(R.id.tvAdminPendingOrders);
        tvAdminCancelledOrders = findViewById(R.id.tvAdminCancelledOrders);
        tvAdminRevenue = findViewById(R.id.tvAdminRevenue);
        tvAdminInventoryAlerts = findViewById(R.id.tvAdminInventoryAlerts);
        tvAdminGreeting = findViewById(R.id.tvAdminGreeting);

        btnRefreshDashboard = findViewById(R.id.btnRefreshDashboard);
        btnAdminLogout = findViewById(R.id.btnAdminLogout);

        cardNavStores = findViewById(R.id.cardNavStores);
        cardNavProducts = findViewById(R.id.cardNavProducts);
        cardNavOrders = findViewById(R.id.cardNavOrders);
        cardNavUsers = findViewById(R.id.cardNavUsers);
        cardNavReports = findViewById(R.id.cardNavReports);
        cardNavAuditLogs = findViewById(R.id.cardNavAuditLogs);
        cardInventoryAlerts = findViewById(R.id.cardInventoryAlerts);
        cardNavRiders = findViewById(R.id.cardNavRiders);
        cardNavStoreOwners = findViewById(R.id.cardNavStoreOwners);
        cardNavInvoices = findViewById(R.id.cardNavInvoices);
        cardNavCommission = findViewById(R.id.cardNavCommission);
        cardNavOffers = findViewById(R.id.cardNavOffers);

        btnQuickAddStore = findViewById(R.id.btnQuickAddStore);
        btnQuickAddProduct = findViewById(R.id.btnQuickAddProduct);
        btnAdminSwitchRole = findViewById(R.id.btnAdminSwitchRole);

        String adminName = SessionManager.getInstance().getUserName();
        if (adminName != null && !adminName.isEmpty()) {
            tvAdminGreeting.setText("Welcome, " + adminName);
        }
    }

    private void setupListeners() {
        btnRefreshDashboard.setOnClickListener(v -> {
            Toast.makeText(this, "Refreshing dashboard...", Toast.LENGTH_SHORT).show();
            loadDashboardStats();
        });

        btnAdminLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to end your administrator session?")
                    .setPositiveButton("Logout", (d, w) -> {
                        SessionManager.getInstance().clearSession();
                        startActivity(new Intent(this, AdminLoginActivity.class));
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // Navigation Hub
        cardNavStores.setOnClickListener(v -> startActivity(new Intent(this, AdminStoresActivity.class)));
        cardNavProducts.setOnClickListener(v -> startActivity(new Intent(this, AdminProductsActivity.class)));
        cardNavOrders.setOnClickListener(v -> startActivity(new Intent(this, AdminOrdersActivity.class)));
        cardNavUsers.setOnClickListener(v -> startActivity(new Intent(this, AdminUsersActivity.class)));
        cardNavReports.setOnClickListener(v -> startActivity(new Intent(this, AdminReportsActivity.class)));
        cardNavAuditLogs.setOnClickListener(v -> startActivity(new Intent(this, AdminAuditLogsActivity.class)));
        cardNavRiders.setOnClickListener(v -> startActivity(new Intent(this, AdminRidersActivity.class)));
        cardNavStoreOwners.setOnClickListener(v -> startActivity(new Intent(this, AdminStoreOwnersActivity.class)));
        cardNavInvoices.setOnClickListener(v -> startActivity(new Intent(this, AdminInvoicesActivity.class)));
        cardNavCommission.setOnClickListener(v -> startActivity(new Intent(this, AdminCommissionActivity.class)));
        cardNavOffers.setOnClickListener(v -> startActivity(new Intent(this, AdminOffersBannersActivity.class)));

        // Clicking inventory alerts navigates to products view with low stock
        cardInventoryAlerts.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminProductsActivity.class);
            intent.putExtra("filter_low_stock", true);
            startActivity(intent);
        });

        // Quick Actions
        btnQuickAddStore.setOnClickListener(v -> showAddStoreDialog());
        btnQuickAddProduct.setOnClickListener(v -> showAddProductDialog());

        btnAdminSwitchRole.setOnClickListener(v -> {
            startActivity(new Intent(this, RoleSelectionActivity.class));
            finish();
        });
    }

    private void loadDashboardStats() {
        ApiClient.getApiService().getAdminDashboard().enqueue(new Callback<ApiResponse<DashboardStats>>() {
            @Override
            public void onResponse(Call<ApiResponse<DashboardStats>> call, Response<ApiResponse<DashboardStats>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    DashboardStats stats = response.body().getData();
                    tvAdminStores.setText(stats.getTotalActiveStores() + " / " + stats.getTotalStores());
                    tvAdminCustomers.setText(String.valueOf(stats.getTotalCustomers()));
                    tvAdminProducts.setText(String.valueOf(stats.getTotalProducts()));
                    tvAdminOrdersToday.setText(String.valueOf(stats.getOrdersToday()));
                    tvAdminPendingOrders.setText(String.valueOf(stats.getPendingOrders()));
                    tvAdminCancelledOrders.setText(String.valueOf(stats.getCancelledOrders()));
                    tvAdminRevenue.setText("₹" + stats.getTotalRevenue());
                    tvAdminInventoryAlerts.setText(stats.getInventoryAlerts() + " Items Low");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<DashboardStats>> call, Throwable t) {
                Toast.makeText(AdminMainActivity.this, "Failed to load dashboard: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddStoreDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 10);

        EditText etName = new EditText(this);
        etName.setHint("Store Name (e.g. Rucart Express FC Road)");
        layout.addView(etName);

        EditText etAddress = new EditText(this);
        etAddress.setHint("Address (e.g. 102 Ferguson College Rd)");
        layout.addView(etAddress);

        EditText etCity = new EditText(this);
        etCity.setHint("City (e.g. Pune)");
        layout.addView(etCity);

        EditText etAreas = new EditText(this);
        etAreas.setHint("Service Areas (e.g. FC Road, Shivajinagar)");
        layout.addView(etAreas);

        EditText etPhone = new EditText(this);
        etPhone.setHint("Store Contact Phone (10 digits)");
        layout.addView(etPhone);

        new AlertDialog.Builder(this)
                .setTitle("Create New Store")
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
                                Toast.makeText(AdminMainActivity.this, "Store created successfully!", Toast.LENGTH_SHORT).show();
                                loadDashboardStats();
                            } else {
                                Toast.makeText(AdminMainActivity.this, "Failed to create store", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Store>> call, Throwable t) {
                            Toast.makeText(AdminMainActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAddProductDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 10);

        EditText etName = new EditText(this);
        etName.setHint("Product Name (e.g. Organic Tomatoes)");
        layout.addView(etName);

        EditText etBrand = new EditText(this);
        etBrand.setHint("Brand (e.g. Rucart Fresh)");
        layout.addView(etBrand);

        EditText etPrice = new EditText(this);
        etPrice.setHint("Price in ₹ (e.g. 45.00)");
        etPrice.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(etPrice);

        EditText etUnit = new EditText(this);
        etUnit.setHint("Unit (e.g. 1 kg, 500 g)");
        layout.addView(etUnit);

        EditText etStock = new EditText(this);
        etStock.setHint("Initial Stock Quantity (e.g. 100)");
        etStock.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(etStock);

        new AlertDialog.Builder(this)
                .setTitle("Add New Product")
                .setView(layout)
                .setPositiveButton("Add Product", (d, w) -> {
                    String name = etName.getText().toString().trim();
                    String brand = etBrand.getText().toString().trim();
                    String priceStr = etPrice.getText().toString().trim();
                    String unit = etUnit.getText().toString().trim();
                    String stockStr = etStock.getText().toString().trim();

                    if (name.isEmpty() || priceStr.isEmpty()) {
                        Toast.makeText(this, "Product name and price are required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    BigDecimal price = new BigDecimal(priceStr);
                    int stock = stockStr.isEmpty() ? 50 : Integer.parseInt(stockStr);

                    ProductCreateRequest req = new ProductCreateRequest();
                    req.setName(name);
                    req.setBrand(brand.isEmpty() ? "Rucart Fresh" : brand);
                    req.setPrice(price);
                    req.setOriginalPrice(price.multiply(BigDecimal.valueOf(1.2)));
                    req.setUnit(unit.isEmpty() ? "1 unit" : unit);
                    req.setCategoryId(1L);
                    req.setInitialStock(stock);
                    req.setLowStockThreshold(10);
                    req.setAvailable(true);

                    ApiClient.getApiService().createAdminProduct(req).enqueue(new Callback<ApiResponse<Product>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Product>> call, Response<ApiResponse<Product>> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                Toast.makeText(AdminMainActivity.this, "Product created successfully!", Toast.LENGTH_SHORT).show();
                                loadDashboardStats();
                            } else {
                                Toast.makeText(AdminMainActivity.this, "Failed to create product", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Product>> call, Throwable t) {
                            Toast.makeText(AdminMainActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
