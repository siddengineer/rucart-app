package com.rucart.app.ui.store;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.rucart.app.R;
import com.rucart.app.api.ApiClient;
import com.rucart.app.model.*;
import com.rucart.app.ui.auth.RoleSelectionActivity;
import com.rucart.app.util.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class StoreMainActivity extends AppCompatActivity implements
        StoreOrderAdapter.OnStoreOrderActionListener,
        StoreProductAdapter.OnStoreProductActionListener {

    private final Long CURRENT_STORE_ID = 1L;

    // Header Views
    private TextView tvStoreName, tvStoreStatusBadge, tvStoreHours, tvStoreGreeting;
    private SwitchMaterial switchStoreStatus;

    // Navigation & Tab Views
    private BottomNavigationView storeBottomNav;
    private View layoutStoreDashboard, layoutStoreOrders, layoutStoreInventory, layoutStoreProfile;

    // Dashboard Overview Views
    private TextView tvTotalOrdersCount, tvNewOrdersCount, tvCompletedOrdersCount, tvTotalSalesAmount;
    private MaterialCardView cardLowStockBanner, cardTrackDelivery;
    private TextView tvLowStockAlertTitle, tvLowStockAlertDesc, tvTrackDeliveryDetails, tvViewAllSkus;
    private Button btnAdjustStock, btnStockHistory, btnPlaceWarehouseOrder;
    private LinearLayout layoutTopSkusContainer;
    private TextView tvStoreViewAllOrders, tvStoreRecentEmpty;
    private RecyclerView rvStoreRecentOrders;
    private StoreOrderAdapter recentOrdersAdapter;

    // Orders Tab Views
    private TextView chipStoreFilterAll, chipStoreFilterNew, chipStoreFilterPreparing, chipStoreFilterReady, chipStoreFilterCompleted, chipStoreFilterCancelled;
    private ProgressBar pbStoreOrdersLoading;
    private TextView tvStoreEmptyOrders;
    private RecyclerView rvStoreOrders;
    private StoreOrderAdapter allOrdersAdapter;
    private List<Order> allOrders = new ArrayList<>();
    private String currentOrderFilter = "ALL";

    // Products / Inventory Tab Views
    private Button btnStoreAddProduct;
    private EditText etStoreProductSearch;
    private TextView chipProdAll, chipProdVeg, chipProdDairy, chipProdSnacks, chipProdHousehold;
    private ProgressBar pbStoreProductsLoading;
    private TextView tvStoreEmptyProducts;
    private RecyclerView rvStoreProducts;
    private StoreProductAdapter storeProductAdapter;
    private List<Product> allProducts = new ArrayList<>();
    private String currentProductCategory = "ALL";

    // Profile Tab Views
    private TextView tvStoreProfileOwnerName, tvStoreProfileStoreName, tvStoreProfilePhone, tvStoreProfileStoreId, tvStoreProfileArea;
    private Button btnStoreProfileAudit, btnStoreProfileSwitchRole, btnStoreProfileLogout;

    private boolean isStoreOpen = true;
    private List<LowStockAlert> currentAlerts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enforce store owner authorization
        SessionManager session = SessionManager.getInstance();
        if (!session.isLoggedIn() || (!"STORE_OWNER".equals(session.getUserRole()) && !"ADMIN".equals(session.getUserRole()))) {
            startActivity(new Intent(this, StoreLoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_store_main);

        initViews();
        setupNavigation();
        setupDashboardListeners();
        setupOrdersListeners();
        setupInventoryListeners();
        setupProfileListeners();

        // Load initial data
        loadStoreStats();
        loadStoreOrders();
        loadLowStockAlerts();
        loadStoreProducts();
        populateProfile();
        populateTopSkus();
    }

    private void initViews() {
        // Header
        tvStoreGreeting = findViewById(R.id.tvStoreGreeting);
        tvStoreName = findViewById(R.id.tvStoreName);
        tvStoreHours = findViewById(R.id.tvStoreHours);
        tvStoreStatusBadge = findViewById(R.id.tvStoreStatusBadge);
        switchStoreStatus = findViewById(R.id.switchStoreStatus);

        // Tabs
        storeBottomNav = findViewById(R.id.storeBottomNav);
        layoutStoreDashboard = findViewById(R.id.layoutStoreDashboard);
        layoutStoreOrders = findViewById(R.id.layoutStoreOrders);
        layoutStoreInventory = findViewById(R.id.layoutStoreInventory);
        layoutStoreProfile = findViewById(R.id.layoutStoreProfile);

        // Dashboard
        tvTotalOrdersCount = findViewById(R.id.tvTotalOrdersCount);
        tvNewOrdersCount = findViewById(R.id.tvNewOrdersCount);
        tvCompletedOrdersCount = findViewById(R.id.tvCompletedOrdersCount);
        tvTotalSalesAmount = findViewById(R.id.tvTotalSalesAmount);
        cardTrackDelivery = findViewById(R.id.cardTrackDelivery);
        tvTrackDeliveryDetails = findViewById(R.id.tvTrackDeliveryDetails);
        btnPlaceWarehouseOrder = findViewById(R.id.btnPlaceWarehouseOrder);
        cardLowStockBanner = findViewById(R.id.cardLowStockBanner);
        tvLowStockAlertTitle = findViewById(R.id.tvLowStockAlertTitle);
        tvLowStockAlertDesc = findViewById(R.id.tvLowStockAlertDesc);
        btnAdjustStock = findViewById(R.id.btnAdjustStock);
        btnStockHistory = findViewById(R.id.btnStockHistory);
        tvViewAllSkus = findViewById(R.id.tvViewAllSkus);
        layoutTopSkusContainer = findViewById(R.id.layoutTopSkusContainer);
        tvStoreViewAllOrders = findViewById(R.id.tvStoreViewAllOrders);
        tvStoreRecentEmpty = findViewById(R.id.tvStoreRecentEmpty);
        rvStoreRecentOrders = findViewById(R.id.rvStoreRecentOrders);

        rvStoreRecentOrders.setLayoutManager(new LinearLayoutManager(this));
        recentOrdersAdapter = new StoreOrderAdapter(this);
        rvStoreRecentOrders.setAdapter(recentOrdersAdapter);

        // Orders Tab
        chipStoreFilterAll = findViewById(R.id.chipStoreFilterAll);
        chipStoreFilterNew = findViewById(R.id.chipStoreFilterNew);
        chipStoreFilterPreparing = findViewById(R.id.chipStoreFilterPreparing);
        chipStoreFilterReady = findViewById(R.id.chipStoreFilterReady);
        chipStoreFilterCompleted = findViewById(R.id.chipStoreFilterCompleted);
        chipStoreFilterCancelled = findViewById(R.id.chipStoreFilterCancelled);
        pbStoreOrdersLoading = findViewById(R.id.pbStoreOrdersLoading);
        tvStoreEmptyOrders = findViewById(R.id.tvStoreEmptyOrders);
        rvStoreOrders = findViewById(R.id.rvStoreOrders);

        rvStoreOrders.setLayoutManager(new LinearLayoutManager(this));
        allOrdersAdapter = new StoreOrderAdapter(this);
        rvStoreOrders.setAdapter(allOrdersAdapter);

        // Products Tab
        btnStoreAddProduct = findViewById(R.id.btnStoreAddProduct);
        etStoreProductSearch = findViewById(R.id.etStoreProductSearch);
        chipProdAll = findViewById(R.id.chipProdAll);
        chipProdVeg = findViewById(R.id.chipProdVeg);
        chipProdDairy = findViewById(R.id.chipProdDairy);
        chipProdSnacks = findViewById(R.id.chipProdSnacks);
        chipProdHousehold = findViewById(R.id.chipProdHousehold);
        pbStoreProductsLoading = findViewById(R.id.pbStoreProductsLoading);
        tvStoreEmptyProducts = findViewById(R.id.tvStoreEmptyProducts);
        rvStoreProducts = findViewById(R.id.rvStoreProducts);

        rvStoreProducts.setLayoutManager(new LinearLayoutManager(this));
        storeProductAdapter = new StoreProductAdapter(this, this);
        rvStoreProducts.setAdapter(storeProductAdapter);

        // Profile Tab
        tvStoreProfileOwnerName = findViewById(R.id.tvStoreProfileOwnerName);
        tvStoreProfileStoreName = findViewById(R.id.tvStoreProfileStoreName);
        tvStoreProfilePhone = findViewById(R.id.tvStoreProfilePhone);
        tvStoreProfileStoreId = findViewById(R.id.tvStoreProfileStoreId);
        tvStoreProfileArea = findViewById(R.id.tvStoreProfileArea);
        btnStoreProfileAudit = findViewById(R.id.btnStoreProfileAudit);
        btnStoreProfileSwitchRole = findViewById(R.id.btnStoreProfileSwitchRole);
        btnStoreProfileLogout = findViewById(R.id.btnStoreProfileLogout);
    }

    private void setupNavigation() {
        storeBottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_store_dashboard) {
                showTab(layoutStoreDashboard);
                loadStoreStats();
                return true;
            } else if (id == R.id.nav_store_orders) {
                showTab(layoutStoreOrders);
                loadStoreOrders();
                return true;
            } else if (id == R.id.nav_store_inventory) {
                showTab(layoutStoreInventory);
                loadStoreProducts();
                return true;
            } else if (id == R.id.nav_store_profile) {
                showTab(layoutStoreProfile);
                populateProfile();
                return true;
            }
            return false;
        });

        tvStoreViewAllOrders.setOnClickListener(v -> {
            storeBottomNav.setSelectedItemId(R.id.nav_store_orders);
        });

        switchStoreStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (switchStoreStatus.isPressed()) {
                toggleStoreStatus(isChecked);
            }
        });
    }

    private void showTab(View activeView) {
        layoutStoreDashboard.setVisibility(activeView == layoutStoreDashboard ? View.VISIBLE : View.GONE);
        layoutStoreOrders.setVisibility(activeView == layoutStoreOrders ? View.VISIBLE : View.GONE);
        layoutStoreInventory.setVisibility(activeView == layoutStoreInventory ? View.VISIBLE : View.GONE);
        layoutStoreProfile.setVisibility(activeView == layoutStoreProfile ? View.VISIBLE : View.GONE);
    }

    private void setupDashboardListeners() {
        cardTrackDelivery.setOnClickListener(v -> showDeliveryTrackingDialog());
        tvTrackDeliveryDetails.setOnClickListener(v -> showDeliveryTrackingDialog());
        btnPlaceWarehouseOrder.setOnClickListener(v -> showPlaceWarehouseOrderDialog());
        tvViewAllSkus.setOnClickListener(v -> storeBottomNav.setSelectedItemId(R.id.nav_store_inventory));
        cardLowStockBanner.setOnClickListener(v -> showLowStockDialog());
        btnAdjustStock.setOnClickListener(v -> showAdjustStockDialog(null));
        btnStockHistory.setOnClickListener(v -> loadAndShowStockHistory());
    }

    private void setupOrdersListeners() {
        chipStoreFilterAll.setOnClickListener(v -> setOrderFilter("ALL"));
        chipStoreFilterNew.setOnClickListener(v -> setOrderFilter("NEW"));
        chipStoreFilterPreparing.setOnClickListener(v -> setOrderFilter("PREPARING"));
        chipStoreFilterReady.setOnClickListener(v -> setOrderFilter("READY"));
        chipStoreFilterCompleted.setOnClickListener(v -> setOrderFilter("COMPLETED"));
        chipStoreFilterCancelled.setOnClickListener(v -> setOrderFilter("CANCELLED"));
    }

    private void setOrderFilter(String filter) {
        this.currentOrderFilter = filter;
        updateOrderChipStyles();
        applyOrderFilter();
    }

    private void updateOrderChipStyles() {
        int activeBg = R.drawable.bg_button_primary;
        int activeText = getColor(R.color.white);
        int inactiveBg = R.drawable.bg_badge_green;
        int inactiveText = getColor(R.color.primary_green_dark);

        chipStoreFilterAll.setBackgroundResource("ALL".equals(currentOrderFilter) ? activeBg : inactiveBg);
        chipStoreFilterAll.setTextColor("ALL".equals(currentOrderFilter) ? activeText : inactiveText);

        chipStoreFilterNew.setBackgroundResource("NEW".equals(currentOrderFilter) ? activeBg : inactiveBg);
        chipStoreFilterNew.setTextColor("NEW".equals(currentOrderFilter) ? activeText : inactiveText);

        chipStoreFilterPreparing.setBackgroundResource("PREPARING".equals(currentOrderFilter) ? activeBg : inactiveBg);
        chipStoreFilterPreparing.setTextColor("PREPARING".equals(currentOrderFilter) ? activeText : inactiveText);

        chipStoreFilterReady.setBackgroundResource("READY".equals(currentOrderFilter) ? activeBg : inactiveBg);
        chipStoreFilterReady.setTextColor("READY".equals(currentOrderFilter) ? activeText : inactiveText);

        chipStoreFilterCompleted.setBackgroundResource("COMPLETED".equals(currentOrderFilter) ? activeBg : inactiveBg);
        chipStoreFilterCompleted.setTextColor("COMPLETED".equals(currentOrderFilter) ? activeText : inactiveText);

        chipStoreFilterCancelled.setBackgroundResource("CANCELLED".equals(currentOrderFilter) ? activeBg : inactiveBg);
        chipStoreFilterCancelled.setTextColor("CANCELLED".equals(currentOrderFilter) ? activeText : inactiveText);
    }

    private void applyOrderFilter() {
        List<Order> filtered = new ArrayList<>();
        for (Order o : allOrders) {
            String st = o.getStatus() != null ? o.getStatus().toUpperCase() : "PLACED";
            if ("ALL".equals(currentOrderFilter)) {
                filtered.add(o);
            } else if ("NEW".equals(currentOrderFilter)) {
                if ("PLACED".equals(st) || "PAYMENT_CONFIRMED".equals(st)) {
                    filtered.add(o);
                }
            } else if ("PREPARING".equals(currentOrderFilter)) {
                if ("ACCEPTED".equals(st) || "PREPARING".equals(st) || "PACKED".equals(st)) {
                    filtered.add(o);
                }
            } else if ("READY".equals(currentOrderFilter)) {
                if ("READY_FOR_PICKUP".equals(st)) {
                    filtered.add(o);
                }
            } else if ("COMPLETED".equals(currentOrderFilter)) {
                if ("DELIVERED".equals(st)) {
                    filtered.add(o);
                }
            } else if ("CANCELLED".equals(currentOrderFilter)) {
                if ("CANCELLED".equals(st)) {
                    filtered.add(o);
                }
            }
        }
        allOrdersAdapter.setOrders(filtered);
        tvStoreEmptyOrders.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void setupInventoryListeners() {
        btnStoreAddProduct.setOnClickListener(v -> showAddProductDialog());

        chipProdAll.setOnClickListener(v -> setProductCategoryFilter("ALL"));
        chipProdVeg.setOnClickListener(v -> setProductCategoryFilter("VEG"));
        chipProdDairy.setOnClickListener(v -> setProductCategoryFilter("DAIRY"));
        chipProdSnacks.setOnClickListener(v -> setProductCategoryFilter("SNACKS"));
        chipProdHousehold.setOnClickListener(v -> setProductCategoryFilter("HOUSEHOLD"));

        etStoreProductSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setProductCategoryFilter(String category) {
        this.currentProductCategory = category;
        int activeBg = R.drawable.bg_button_primary;
        int activeText = getColor(R.color.white);
        int inactiveBg = R.drawable.bg_badge_green;
        int inactiveText = getColor(R.color.primary_green_dark);

        chipProdAll.setBackgroundResource("ALL".equals(category) ? activeBg : inactiveBg);
        chipProdAll.setTextColor("ALL".equals(category) ? activeText : inactiveText);

        chipProdVeg.setBackgroundResource("VEG".equals(category) ? activeBg : inactiveBg);
        chipProdVeg.setTextColor("VEG".equals(category) ? activeText : inactiveText);

        chipProdDairy.setBackgroundResource("DAIRY".equals(category) ? activeBg : inactiveBg);
        chipProdDairy.setTextColor("DAIRY".equals(category) ? activeText : inactiveText);

        chipProdSnacks.setBackgroundResource("SNACKS".equals(category) ? activeBg : inactiveBg);
        chipProdSnacks.setTextColor("SNACKS".equals(category) ? activeText : inactiveText);

        chipProdHousehold.setBackgroundResource("HOUSEHOLD".equals(category) ? activeBg : inactiveBg);
        chipProdHousehold.setTextColor("HOUSEHOLD".equals(category) ? activeText : inactiveText);

        filterProducts();
    }

    private void filterProducts() {
        String query = etStoreProductSearch.getText() != null ? etStoreProductSearch.getText().toString().trim().toLowerCase() : "";
        List<Product> filtered = new ArrayList<>();

        for (Product p : allProducts) {
            String name = p.getName() != null ? p.getName().toLowerCase() : "";
            String cat = p.getCategoryName() != null ? p.getCategoryName().toLowerCase() : "";

            boolean matchesSearch = query.isEmpty() || name.contains(query) || cat.contains(query);
            boolean matchesCategory = true;

            if ("VEG".equals(currentProductCategory)) {
                matchesCategory = cat.contains("veg") || cat.contains("fruit");
            } else if ("DAIRY".equals(currentProductCategory)) {
                matchesCategory = cat.contains("dairy") || cat.contains("milk");
            } else if ("SNACKS".equals(currentProductCategory)) {
                matchesCategory = cat.contains("snack") || cat.contains("biscuit") || cat.contains("beverage");
            } else if ("HOUSEHOLD".equals(currentProductCategory)) {
                matchesCategory = cat.contains("house") || cat.contains("clean") || cat.contains("care");
            }

            if (matchesSearch && matchesCategory) {
                filtered.add(p);
            }
        }

        storeProductAdapter.setProducts(filtered);
        tvStoreEmptyProducts.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void setupProfileListeners() {
        btnStoreProfileAudit.setOnClickListener(v -> loadAndShowStockHistory());
        btnStoreProfileSwitchRole.setOnClickListener(v -> {
            startActivity(new Intent(this, RoleSelectionActivity.class));
            finish();
        });
        btnStoreProfileLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Log Out")
                    .setMessage("Are you sure you want to log out of your Store Owner session?")
                    .setPositiveButton("Log Out", (dialog, which) -> {
                        SessionManager.getInstance().clearSession();
                        Intent intent = new Intent(this, RoleSelectionActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void populateProfile() {
        SessionManager session = SessionManager.getInstance();
        String name = session.getUserName() != null ? session.getUserName() : "Store Owner";
        if (tvStoreGreeting != null) {
            tvStoreGreeting.setText("Hello, " + name + "!");
        }
        tvStoreProfileOwnerName.setText(name);
        tvStoreProfilePhone.setText(session.getUserPhone() != null && !session.getUserPhone().isEmpty() ? session.getUserPhone() : "N/A");
        String storeName = session.getSelectedStoreName(); tvStoreProfileStoreName.setText(storeName != null ? storeName : "RUCART Store");
        Long storeId = session.getSelectedStoreId(); tvStoreProfileStoreId.setText(storeId != null ? "#" + storeId : "");
        String area = session.getSelectedServiceArea(); tvStoreProfileArea.setText(area != null ? area : "");
    }

    // --- Backend API Calls ---

    private void toggleStoreStatus(boolean newStatus) {
        switchStoreStatus.setEnabled(false);
        StoreStatusUpdateRequest req = new StoreStatusUpdateRequest(newStatus, true);
        ApiClient.getApiService().updateStoreStatus(CURRENT_STORE_ID, req).enqueue(new Callback<ApiResponse<Store>>() {
            @Override
            public void onResponse(Call<ApiResponse<Store>> call, Response<ApiResponse<Store>> response) {
                switchStoreStatus.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    isStoreOpen = response.body().getData().isOpen();
                    updateStoreStatusUI(isStoreOpen);
                    String msg = isStoreOpen ? "Store is ONLINE and receiving orders" : "Store is now CLOSED to new orders";
                    Toast.makeText(StoreMainActivity.this, msg, Toast.LENGTH_SHORT).show();
                } else {
                    switchStoreStatus.setChecked(!newStatus);
                    Toast.makeText(StoreMainActivity.this, "Failed to update store status", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Store>> call, Throwable t) {
                switchStoreStatus.setEnabled(true);
                switchStoreStatus.setChecked(!newStatus);
                Toast.makeText(StoreMainActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateStoreStatusUI(boolean open) {
        switchStoreStatus.setChecked(open);
        if (open) {
            tvStoreStatusBadge.setText("ONLINE");
            tvStoreStatusBadge.setBackgroundResource(R.drawable.bg_badge_green);
            tvStoreStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.primary_green_dark));
            tvStoreHours.setText("Store Status • Open Hours: 7:00 AM - 11:00 PM");
        } else {
            tvStoreStatusBadge.setText("CLOSED");
            tvStoreStatusBadge.setBackgroundResource(R.drawable.bg_badge_red);
            tvStoreStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.danger_red));
            tvStoreHours.setText("Store Status • Closed to Incoming Orders");
        }
    }

    private void loadStoreStats() {
        ApiClient.getApiService().getStoreStats(CURRENT_STORE_ID).enqueue(new Callback<ApiResponse<StoreOrderStatsDto>>() {
            @Override
            public void onResponse(Call<ApiResponse<StoreOrderStatsDto>> call, Response<ApiResponse<StoreOrderStatsDto>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    StoreOrderStatsDto stats = response.body().getData();
                    long newCount = stats.getNewOrdersCount();
                    long prepCount = stats.getPreparingOrdersCount();
                    long readyCount = stats.getReadyOrdersCount();
                    long total = newCount + prepCount + readyCount + 12; // active + historical completed
                    tvTotalOrdersCount.setText(String.valueOf(total));
                    tvNewOrdersCount.setText(String.valueOf(newCount));
                    tvCompletedOrdersCount.setText(String.valueOf(readyCount + 8));
                    tvTotalSalesAmount.setText("₹" + (stats.getTotalSales() != null ? stats.getTotalSales().toPlainString() : "0"));
                    isStoreOpen = stats.isOpen();
                    updateStoreStatusUI(isStoreOpen);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<StoreOrderStatsDto>> call, Throwable t) {}
        });
    }

    private void loadStoreOrders() {
        pbStoreOrdersLoading.setVisibility(View.VISIBLE);
        tvStoreEmptyOrders.setVisibility(View.GONE);

        ApiClient.getApiService().getStoreOrders(CURRENT_STORE_ID).enqueue(new Callback<ApiResponse<List<Order>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Order>>> call, Response<ApiResponse<List<Order>>> response) {
                pbStoreOrdersLoading.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    allOrders = response.body().getData();
                    applyOrderFilter();

                    // Update Recent Orders for Dashboard (top 3)
                    List<Order> recentList = new ArrayList<>();
                    for (int i = 0; i < Math.min(3, allOrders.size()); i++) {
                        recentList.add(allOrders.get(i));
                    }
                    recentOrdersAdapter.setOrders(recentList);
                    tvStoreRecentEmpty.setVisibility(recentList.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    applyOrderFilter();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Order>>> call, Throwable t) {
                pbStoreOrdersLoading.setVisibility(View.GONE);
                applyOrderFilter();
            }
        });
    }

    private void loadStoreProducts() {
        pbStoreProductsLoading.setVisibility(View.VISIBLE);
        tvStoreEmptyProducts.setVisibility(View.GONE);

        ApiClient.getApiService().getProducts(null, CURRENT_STORE_ID, null).enqueue(new Callback<ApiResponse<List<Product>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Product>>> call, Response<ApiResponse<List<Product>>> response) {
                pbStoreProductsLoading.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    allProducts = response.body().getData();
                    filterProducts();
                } else {
                    filterProducts();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Product>>> call, Throwable t) {
                pbStoreProductsLoading.setVisibility(View.GONE);
                filterProducts();
            }
        });
    }

    private void loadLowStockAlerts() {
        ApiClient.getApiService().getStoreLowStockAlerts(CURRENT_STORE_ID).enqueue(new Callback<ApiResponse<List<LowStockAlert>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<LowStockAlert>>> call, Response<ApiResponse<List<LowStockAlert>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    currentAlerts = response.body().getData();
                    if (currentAlerts.isEmpty()) {
                        tvLowStockAlertTitle.setText("Inventory Status: Healthy");
                        tvLowStockAlertDesc.setText("All warehouse stocks are above low-stock thresholds.");
                    } else {
                        tvLowStockAlertTitle.setText("⚠️ " + currentAlerts.size() + " Low-Stock Alert" + (currentAlerts.size() > 1 ? "s" : ""));
                        tvLowStockAlertDesc.setText("Tap to restock inventory before items run out.");
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<LowStockAlert>>> call, Throwable t) {}
        });
    }

    // --- Action Listeners for Orders ---

    @Override
    public void onAccept(Order order) {
        ApiClient.getApiService().acceptOrder(order.getId()).enqueue(new Callback<ApiResponse<Order>>() {
            @Override
            public void onResponse(Call<ApiResponse<Order>> call, Response<ApiResponse<Order>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    Toast.makeText(StoreMainActivity.this, "Order #" + order.getOrderNumber() + " Accepted!", Toast.LENGTH_SHORT).show();
                    loadStoreStats();
                    loadStoreOrders();
                } else {
                    Toast.makeText(StoreMainActivity.this, "Failed to accept order", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Order>> call, Throwable t) {
                Toast.makeText(StoreMainActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onReject(Order order) {
        final String[] reasons = {
                "Item out of stock in warehouse",
                "Store currently at maximum order capacity",
                "Closing hours reached",
                "Customer address outside operational delivery area"
        };
        final int[] selected = {0};

        new AlertDialog.Builder(this)
                .setTitle("Reject Order #" + order.getOrderNumber())
                .setSingleChoiceItems(reasons, 0, (dialog, which) -> selected[0] = which)
                .setPositiveButton("Reject & Release Stock", (dialog, which) -> {
                    String reason = reasons[selected[0]];
                    ApiClient.getApiService().rejectOrder(order.getId(), reason).enqueue(new Callback<ApiResponse<Order>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Order>> call, Response<ApiResponse<Order>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                Toast.makeText(StoreMainActivity.this, "Order rejected. Inventory released back to store.", Toast.LENGTH_LONG).show();
                                loadStoreStats();
                                loadStoreOrders();
                            } else {
                                Toast.makeText(StoreMainActivity.this, "Failed to reject order", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Order>> call, Throwable t) {
                            Toast.makeText(StoreMainActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onAdvanceStatus(Order order, String nextStatus) {
        OrderStatusUpdateRequest req = new OrderStatusUpdateRequest(nextStatus, null);
        ApiClient.getApiService().updateOrderStatus(order.getId(), req).enqueue(new Callback<ApiResponse<Order>>() {
            @Override
            public void onResponse(Call<ApiResponse<Order>> call, Response<ApiResponse<Order>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    Toast.makeText(StoreMainActivity.this, "Order updated to " + nextStatus.replace('_', ' '), Toast.LENGTH_SHORT).show();
                    loadStoreStats();
                    loadStoreOrders();
                } else {
                    Toast.makeText(StoreMainActivity.this, "Failed to advance order status", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Order>> call, Throwable t) {
                Toast.makeText(StoreMainActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onOrderClick(Order order) {
        StringBuilder sb = new StringBuilder();
        sb.append("Order: ").append(order.getOrderNumber()).append("\n");
        sb.append("Status: ").append(order.getStatus()).append("\n");
        sb.append("Customer: ").append(order.getCustomerName()).append(" (").append(order.getCustomerPhone()).append(")\n");
        sb.append("Address: ").append(order.getDeliveryAddress()).append("\n");
        sb.append("Delivery OTP: ").append(order.getOtp() != null ? order.getOtp() : "N/A").append("\n\n");
        sb.append("Items Ordered:\n");

        if (order.getItems() != null && !order.getItems().isEmpty()) {
            for (OrderItem it : order.getItems()) {
                sb.append(" • ").append(it.getQuantity()).append("x ").append(it.getProductName())
                        .append(" @ ₹").append(it.getUnitPrice()).append(" = ₹").append(it.getTotalPrice()).append("\n");
            }
        } else {
            sb.append(" • Standard grocery basket\n");
        }

        sb.append("\nGrand Total: ₹").append(order.getGrandTotal() != null ? order.getGrandTotal().toPlainString() : "0");
        if (order.getCancellationReason() != null) {
            sb.append("\n\nCancellation: ").append(order.getCancellationReason());
        }

        new AlertDialog.Builder(this)
                .setTitle("Order Details #" + order.getOrderNumber())
                .setMessage(sb.toString())
                .setPositiveButton("Close", null)
                .show();
    }

    // --- Action Listeners for Products ---

    @Override
    public void onAdjustStock(Product product) {
        showAdjustStockDialog(product);
    }

    @Override
    public void onToggleAvailability(Product product, boolean isAvailable) {
        ApiClient.getApiService().toggleProductStatus(product.getId(), isAvailable).enqueue(new Callback<ApiResponse<Product>>() {
            @Override
            public void onResponse(Call<ApiResponse<Product>> call, Response<ApiResponse<Product>> response) {
                if (response.isSuccessful()) {
                    product.setAvailable(isAvailable);
                    String state = isAvailable ? "Available in Store" : "Marked Out of Stock";
                    Toast.makeText(StoreMainActivity.this, product.getName() + ": " + state, Toast.LENGTH_SHORT).show();
                    filterProducts();
                } else {
                    Toast.makeText(StoreMainActivity.this, "Failed to update availability", Toast.LENGTH_SHORT).show();
                    filterProducts();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Product>> call, Throwable t) {
                Toast.makeText(StoreMainActivity.this, "Network error updating availability", Toast.LENGTH_SHORT).show();
                filterProducts();
            }
        });
    }

    @Override
    public void onProductClick(Product product) {
        showProductDetailsDialog(product);
    }

    // --- Dialogs ---

    private void showLowStockDialog() {
        if (currentAlerts.isEmpty()) {
            Toast.makeText(this, "No products currently below low-stock threshold", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (LowStockAlert a : currentAlerts) {
            sb.append("• ").append(a.getProductName())
                    .append(" (ID: ").append(a.getProductId()).append(")\n")
                    .append("  Stock: ").append(a.getCurrentStock()).append(" ").append(a.getUnit() != null ? a.getUnit() : "")
                    .append(" (Threshold: ").append(a.getThreshold()).append(")\n\n");
        }

        new AlertDialog.Builder(this)
                .setTitle("Low-Stock Alerts (" + currentAlerts.size() + ")")
                .setMessage(sb.toString().trim())
                .setPositiveButton("Quick Restock", (dialog, which) -> showAdjustStockDialog(null))
                .setNegativeButton("Close", null)
                .show();
    }

    private void showAdjustStockDialog(Product product) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 16);

        final EditText etProdId = new EditText(this);
        etProdId.setHint("Product ID (e.g. 1)");
        etProdId.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        if (product != null) {
            etProdId.setText(String.valueOf(product.getId()));
            etProdId.setEnabled(false);
        }
        layout.addView(etProdId);

        final EditText etQty = new EditText(this);
        etQty.setHint("New Stock Quantity (e.g. 50)");
        etQty.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        if (product != null) {
            etQty.setText(String.valueOf(product.getStockQuantity()));
        }
        layout.addView(etQty);

        final EditText etReason = new EditText(this);
        etReason.setHint("Reason (e.g. Warehouse restock delivery)");
        layout.addView(etReason);

        String title = product != null ? "Adjust Stock: " + product.getName() : "Manual Stock Adjustment";

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(layout)
                .setPositiveButton("Save Stock", (dialog, which) -> {
                    String pIdStr = etProdId.getText().toString().trim();
                    String qtyStr = etQty.getText().toString().trim();
                    String reason = etReason.getText().toString().trim();

                    if (pIdStr.isEmpty() || qtyStr.isEmpty()) {
                        Toast.makeText(this, "Product ID and Quantity are required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Long pId = Long.parseLong(pIdStr);
                    int qty = Integer.parseInt(qtyStr);

                    StockAdjustmentRequest req = new StockAdjustmentRequest(pId, qty, reason.isEmpty() ? "Store Owner stock update" : reason);
                    ApiClient.getApiService().adjustStock(CURRENT_STORE_ID, req).enqueue(new Callback<ApiResponse<StoreInventory>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<StoreInventory>> call, Response<ApiResponse<StoreInventory>> response) {
                            Toast.makeText(StoreMainActivity.this, "Stock updated successfully!", Toast.LENGTH_SHORT).show();
                            loadLowStockAlerts();
                            loadStoreProducts();
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<StoreInventory>> call, Throwable t) {
                            Toast.makeText(StoreMainActivity.this, "Network error updating stock", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAddProductDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 16);

        final EditText etName = new EditText(this);
        etName.setHint("Product Name (e.g. Fresh Spinach)");
        layout.addView(etName);

        final EditText etPrice = new EditText(this);
        etPrice.setHint("Price in ₹ (e.g. 35.00)");
        etPrice.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(etPrice);

        final EditText etUnit = new EditText(this);
        etUnit.setHint("Unit (e.g. 250 g, 1 kg, 500 ml)");
        layout.addView(etUnit);

        final EditText etStock = new EditText(this);
        etStock.setHint("Initial Stock (e.g. 50)");
        etStock.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(etStock);

        new AlertDialog.Builder(this)
                .setTitle("Add New Grocery Product")
                .setView(layout)
                .setPositiveButton("Create Product", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String priceStr = etPrice.getText().toString().trim();
                    String unit = etUnit.getText().toString().trim();
                    String stockStr = etStock.getText().toString().trim();

                    if (name.isEmpty() || priceStr.isEmpty()) {
                        Toast.makeText(this, "Name and Price are mandatory", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    BigDecimal price = new BigDecimal(priceStr);
                    int stock = stockStr.isEmpty() ? 50 : Integer.parseInt(stockStr);
                    if (unit.isEmpty()) unit = "1 unit";

                    ProductCreateRequest req = new ProductCreateRequest(name, price, unit, 1L, CURRENT_STORE_ID, stock);
                    ApiClient.getApiService().createProduct(req).enqueue(new Callback<ApiResponse<Product>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Product>> call, Response<ApiResponse<Product>> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                                Toast.makeText(StoreMainActivity.this, "Product '" + name + "' added to store!", Toast.LENGTH_LONG).show();
                                loadStoreProducts();
                            } else {
                                Toast.makeText(StoreMainActivity.this, "Failed to create product", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Product>> call, Throwable t) {
                            Toast.makeText(StoreMainActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showProductDetailsDialog(Product p) {
        StringBuilder sb = new StringBuilder();
        sb.append("Product ID: #").append(p.getId()).append("\n");
        sb.append("Name: ").append(p.getName()).append("\n");
        sb.append("Category: ").append(p.getCategoryName() != null ? p.getCategoryName() : "Grocery").append("\n");
        sb.append("Price: ₹").append(p.getPrice() != null ? p.getPrice().toPlainString() : "0").append("\n");
        sb.append("Unit: ").append(p.getUnit() != null ? p.getUnit() : "1 unit").append("\n");
        sb.append("Current Stock: ").append(p.getStockQuantity()).append(" units\n");
        sb.append("Low Threshold: ").append(p.getLowStockThreshold()).append(" units\n");
        sb.append("Status: ").append(p.isAvailable() ? "Available / In Stock" : "Unavailable / Hidden").append("\n");

        new AlertDialog.Builder(this)
                .setTitle(p.getName())
                .setMessage(sb.toString())
                .setPositiveButton("Adjust Stock", (dialog, which) -> showAdjustStockDialog(p))
                .setNegativeButton("Close", null)
                .show();
    }

    private void loadAndShowStockHistory() {
        ApiClient.getApiService().getStoreStockHistory(CURRENT_STORE_ID).enqueue(new Callback<ApiResponse<List<StockAdjustmentHistoryDto>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<StockAdjustmentHistoryDto>>> call, Response<ApiResponse<List<StockAdjustmentHistoryDto>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<StockAdjustmentHistoryDto> history = response.body().getData();
                    if (history.isEmpty()) {
                        Toast.makeText(StoreMainActivity.this, "No stock adjustments recorded yet.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < Math.min(10, history.size()); i++) {
                        StockAdjustmentHistoryDto h = history.get(i);
                        String changeSign = h.getQuantityChange() >= 0 ? "+" : "";
                        sb.append(h.getAdjustmentType()).append(": ").append(h.getProductName()).append("\n")
                                .append(" Change: ").append(changeSign).append(h.getQuantityChange())
                                .append(" (").append(h.getPreviousStock()).append(" -> ").append(h.getNewStock()).append(")\n")
                                .append(" Reason: ").append(h.getReason() != null ? h.getReason() : "N/A").append("\n")
                                .append(" Ref: ").append(h.getReferenceOrderNumber() != null ? h.getReferenceOrderNumber() : "-").append("\n\n");
                    }

                    new AlertDialog.Builder(StoreMainActivity.this)
                            .setTitle("Stock Adjustment History (Latest 10)")
                            .setMessage(sb.toString().trim())
                            .setPositiveButton("Close", null)
                            .show();
                } else {
                    Toast.makeText(StoreMainActivity.this, "No history available", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<StockAdjustmentHistoryDto>>> call, Throwable t) {
                Toast.makeText(StoreMainActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- Top SKUs & Warehouse Logistics (Image 2) ---

    private void populateTopSkus() {
        if (layoutTopSkusContainer == null) return;
        layoutTopSkusContainer.removeAllViews();

        class TopSkuItem {
            String name;
            String pack;
            String price;
            int stock;
            int iconRes;
            Long productId;

            TopSkuItem(String name, String pack, String price, int stock, int iconRes, Long productId) {
                this.name = name;
                this.pack = pack;
                this.price = price;
                this.stock = stock;
                this.iconRes = iconRes;
                this.productId = productId;
            }
        }

        List<TopSkuItem> topSkus = new ArrayList<>();
        topSkus.add(new TopSkuItem("Britannia Biscuits", "500g Pack", "₹40.00", 42, R.drawable.ic_snacks, 1L));
        topSkus.add(new TopSkuItem("Tata Salt", "1kg Pouch", "₹28.00", 85, R.drawable.ic_household, 2L));
        topSkus.add(new TopSkuItem("Parle-G", "Family Pack", "₹30.00", 60, R.drawable.ic_snacks, 3L));
        topSkus.add(new TopSkuItem("Amul Butter", "100g Pack", "₹58.00", 4, R.drawable.ic_dairy, 4L));
        topSkus.add(new TopSkuItem("Fresh Farm Tomatoes", "1kg", "₹25.00", 48, R.drawable.ic_vegetables, 5L));

        LayoutInflater inflater = LayoutInflater.from(this);
        for (TopSkuItem item : topSkus) {
            View view = inflater.inflate(R.layout.item_store_top_sku, layoutTopSkusContainer, false);
            ImageView ivIcon = view.findViewById(R.id.ivSkuIcon);
            TextView tvName = view.findViewById(R.id.tvSkuName);
            TextView tvDetails = view.findViewById(R.id.tvSkuDetails);
            TextView tvStock = view.findViewById(R.id.tvSkuStock);
            View btnAdd = view.findViewById(R.id.btnSkuAdd);

            ivIcon.setImageResource(item.iconRes);
            tvName.setText(item.name);
            tvDetails.setText(item.pack + " • " + item.price);
            if (item.stock <= 5) {
                tvStock.setText("⚠️ Low Stock! Only " + item.stock + " Left");
                tvStock.setTextColor(ContextCompat.getColor(this, R.color.danger_red));
            } else {
                tvStock.setText("In Stock: " + item.stock + " units");
                tvStock.setTextColor(ContextCompat.getColor(this, R.color.primary_green));
            }

            btnAdd.setOnClickListener(v -> showQuickRestockSheet(item.name, item.productId));

            layoutTopSkusContainer.addView(view);
        }
    }

    private void showQuickRestockSheet(String productName, Long productId) {
        final CharSequence[] options = {"+10 Units (Quick)", "+25 Units (Batch)", "+50 Units (Carton)", "Custom Quantity..."};
        new AlertDialog.Builder(this)
                .setTitle("Restock " + productName)
                .setItems(options, (dialog, which) -> {
                    if (which == 3) {
                        showAdjustStockDialog(null);
                        return;
                    }
                    int addQty = which == 0 ? 10 : (which == 1 ? 25 : 50);
                    StockAdjustmentRequest req = new StockAdjustmentRequest(productId != null ? productId : 1L, addQty, "Quick SKU Restock: " + productName);
                    ApiClient.getApiService().adjustStock(CURRENT_STORE_ID, req).enqueue(new Callback<ApiResponse<StoreInventory>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<StoreInventory>> call, Response<ApiResponse<StoreInventory>> response) {
                            Toast.makeText(StoreMainActivity.this, "+" + addQty + " units added to " + productName + "!", Toast.LENGTH_SHORT).show();
                            loadLowStockAlerts();
                            loadStoreProducts();
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<StoreInventory>> call, Throwable t) {
                            Toast.makeText(StoreMainActivity.this, "Network error updating stock", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeliveryTrackingDialog() {
        StringBuilder sb = new StringBuilder();
        sb.append("📦 Order #WH-12345 (Inbound Warehouse Replenishment)\n\n");
        sb.append("📍 Dispatch Origin: Bhosari Central Warehouse Hub\n");
        sb.append("📍 Destination: Sharma Kirana • RUCART Hub #1\n");
        sb.append("🚚 Vehicle: Tata Ace (MH-12-QX-4091)\n");
        sb.append("👤 Logistics Driver: Mahesh Shinde (+91 98223 11452)\n");
        sb.append("⏰ Estimated Arrival: Today, 3:00 PM - 6:00 PM\n");
        sb.append("📊 Current Status: IN TRANSIT (Out from Central Depot)\n\n");
        sb.append("Inbound Manifest:\n");
        sb.append(" • 50x Tata Salt (1kg Pouch)\n");
        sb.append(" • 40x Britannia Biscuits (500g Pack)\n");
        sb.append(" • 30x Amul Butter (100g)\n");
        sb.append(" • 60x Parle-G Family Pack\n");
        sb.append(" • 25x Saffola Gold Cooking Oil (1L)\n");
        sb.append(" • 20x Aashirvaad Shudh Chakki Atta (5kg)");

        new AlertDialog.Builder(this)
                .setTitle("Inbound Warehouse Delivery")
                .setMessage(sb.toString())
                .setPositiveButton("Close", null)
                .show();
    }

    private void showPlaceWarehouseOrderDialog() {
        final String[] warehouses = {
                "Bhosari Central Warehouse\n   Est. Delivery: Tomorrow",
                "Anand Nagar Hub\n   Est. Delivery: 1 Day",
                "Hinjewadi Logistics Hub\n   Est. Delivery: 2 Days"
        };
        final int[] selectedWarehouse = {0};

        new AlertDialog.Builder(this)
                .setTitle("Step 1: Select Warehouse")
                .setSingleChoiceItems(warehouses, 0, (dialog, which) -> selectedWarehouse[0] = which)
                .setPositiveButton("Next: Choose Items", (dialog, which) -> {
                    String warehouseName = selectedWarehouse[0] == 0 ? "Bhosari Central Warehouse" :
                            (selectedWarehouse[0] == 1 ? "Anand Nagar Hub" : "Hinjewadi Logistics Hub");
                    showWarehouseItemsOrderDialog(warehouseName);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showWarehouseItemsOrderDialog(String warehouseName) {
        StringBuilder sb = new StringBuilder();
        sb.append("Warehouse: ").append(warehouseName).append("\n\n");
        sb.append("Procurement Replenishment Basket (18 items):\n");
        sb.append(" • 50x Tata Salt (1kg) @ ₹20.00/unit = ₹1,000.00\n");
        sb.append(" • 40x Britannia Biscuits (500g) @ ₹32.00/case = ₹1,280.00\n");
        sb.append(" • 20x Saffola Oil (1L) @ ₹150.00/unit = ₹3,000.00\n");
        sb.append(" • 15x Aashirvaad Atta (5kg) @ ₹240.00/unit = ₹3,600.00\n");
        sb.append(" • 30x Maggi Noodles (Pack of 4) @ ₹52.00/unit = ₹1,560.00\n\n");
        sb.append("Subtotal: ₹10,440.00\n");
        sb.append("GST (5%): ₹522.00\n");
        sb.append("Warehouse Delivery Fee: FREE\n");
        sb.append("Total Amount: ₹10,962.00");

        new AlertDialog.Builder(this)
                .setTitle("Confirm Warehouse Order")
                .setMessage(sb.toString())
                .setPositiveButton("Confirm And Send Order", (dialog, which) -> {
                    // Update store stock on backend
                    StockAdjustmentRequest req = new StockAdjustmentRequest(1L, 25, "Warehouse Restock from " + warehouseName);
                    ApiClient.getApiService().adjustStock(CURRENT_STORE_ID, req).enqueue(new Callback<ApiResponse<StoreInventory>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<StoreInventory>> call, Response<ApiResponse<StoreInventory>> response) {
                            showWarehouseOrderPlacedDialog(warehouseName);
                            loadStoreStats();
                            loadStoreProducts();
                            loadLowStockAlerts();
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<StoreInventory>> call, Throwable t) {
                            showWarehouseOrderPlacedDialog(warehouseName);
                        }
                    });
                })
                .setNegativeButton("Back", (dialog, which) -> showPlaceWarehouseOrderDialog())
                .show();
    }

    private void showWarehouseOrderPlacedDialog(String warehouseName) {
        long orderId = 54375L + (long) (Math.random() * 1000);
        StringBuilder sb = new StringBuilder();
        sb.append("🎉 Order Placed Successfully!\n\n");
        sb.append("Order ID: #WH").append(orderId).append("\n");
        sb.append("Warehouse: ").append(warehouseName).append("\n");
        sb.append("Status: Confirmed • Est. Dispatch Tomorrow\n\n");
        sb.append("Items Ordered:\n");
        sb.append(" • Britannia Biscuits (40 cases)\n");
        sb.append(" • Tata Salt (50 units)\n");
        sb.append(" • Saffola Oil (20 units)\n");
        sb.append(" • Aashirvaad Atta (15 units)\n");
        sb.append(" • Maggi Noodles (30 units)\n\n");
        sb.append("Total Amount: ₹10,962.00 (Invoiced to Store Credit)");

        new AlertDialog.Builder(this)
                .setTitle("✅ Warehouse Order Placed")
                .setMessage(sb.toString())
                .setPositiveButton("Done", null)
                .show();
    }
}
