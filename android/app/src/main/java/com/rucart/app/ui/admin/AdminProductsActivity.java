
package com.rucart.app.ui.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.InputType;
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
import com.rucart.app.model.Category;
import com.rucart.app.model.CategoryRequest;
import com.rucart.app.model.Product;
import com.rucart.app.model.ProductCreateRequest;
import com.rucart.app.model.ProductUpdateRequest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminProductsActivity extends AppCompatActivity
        implements AdminProductAdapter.ProductActionListener {

    private ImageButton btnBackProducts;
    private AppCompatButton btnManageCategories, btnAddProduct;
    private EditText etSearchProducts;
    private TextView filterProductAll, filterProductAvailable;
    private TextView filterProductLowStock, tvEmptyProducts;
    private ProgressBar pbProductsLoading;
    private RecyclerView rvAdminProducts;

    private AdminProductAdapter adapter;

    private List<Product> allProducts = new ArrayList<>();
    private List<Category> allCategories = new ArrayList<>();

    private String currentFilter = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_products);

        if (getIntent().getBooleanExtra("filter_low_stock", false)) {
            currentFilter = "LOW_STOCK";
        }

        initViews();
        setupListeners();
        updateFilterChips();

        loadCategories();
        loadProducts();
    }

    private void initViews() {
        btnBackProducts = findViewById(R.id.btnBackProducts);
        btnManageCategories = findViewById(R.id.btnManageCategories);
        btnAddProduct = findViewById(R.id.btnAddProduct);

        etSearchProducts = findViewById(R.id.etSearchProducts);

        filterProductAll = findViewById(R.id.filterProductAll);
        filterProductAvailable = findViewById(R.id.filterProductAvailable);
        filterProductLowStock = findViewById(R.id.filterProductLowStock);

        tvEmptyProducts = findViewById(R.id.tvEmptyProducts);
        pbProductsLoading = findViewById(R.id.pbProductsLoading);
        rvAdminProducts = findViewById(R.id.rvAdminProducts);

        rvAdminProducts.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AdminProductAdapter(this);
        rvAdminProducts.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBackProducts.setOnClickListener(v -> finish());

        btnAddProduct.setOnClickListener(v -> showAddProductDialog());

        btnManageCategories.setOnClickListener(
                v -> showCategoryManagementDialog()
        );

        filterProductAll.setOnClickListener(v -> {
            currentFilter = "ALL";
            updateFilterChips();
            applyFilter();
        });

        filterProductAvailable.setOnClickListener(v -> {
            currentFilter = "AVAILABLE";
            updateFilterChips();
            applyFilter();
        });

        filterProductLowStock.setOnClickListener(v -> {
            currentFilter = "LOW_STOCK";
            updateFilterChips();
            applyFilter();
        });

        etSearchProducts.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after
            ) {
                // No action required.
            }

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count
            ) {
                applyFilter();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No action required.
            }
        });
    }

    private void updateFilterChips() {
        filterProductAll.setBackgroundResource(
                currentFilter.equals("ALL")
                        ? R.drawable.bg_chip_selected
                        : R.drawable.bg_chip_unselected
        );

        filterProductAll.setTextColor(
                getColor(
                        currentFilter.equals("ALL")
                                ? R.color.white
                                : R.color.text_secondary
                )
        );

        filterProductAvailable.setBackgroundResource(
                currentFilter.equals("AVAILABLE")
                        ? R.drawable.bg_chip_selected
                        : R.drawable.bg_chip_unselected
        );

        filterProductAvailable.setTextColor(
                getColor(
                        currentFilter.equals("AVAILABLE")
                                ? R.color.white
                                : R.color.text_secondary
                )
        );

        filterProductLowStock.setBackgroundResource(
                currentFilter.equals("LOW_STOCK")
                        ? R.drawable.bg_chip_selected
                        : R.drawable.bg_chip_unselected
        );

        filterProductLowStock.setTextColor(
                getColor(
                        currentFilter.equals("LOW_STOCK")
                                ? R.color.white
                                : R.color.text_secondary
                )
        );
    }

    private void loadCategories() {
        ApiClient.getApiService()
                .getAdminCategories()
                .enqueue(new Callback<ApiResponse<List<Category>>>() {

                    @Override
                    public void onResponse(
                            Call<ApiResponse<List<Category>>> call,
                            Response<ApiResponse<List<Category>>> response
                    ) {
                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().getData() != null) {

                            allCategories = response.body().getData();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ApiResponse<List<Category>>> call,
                            Throwable t
                    ) {
                        Toast.makeText(
                                AdminProductsActivity.this,
                                "Unable to load categories",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void loadProducts() {
        pbProductsLoading.setVisibility(View.VISIBLE);

        ApiClient.getApiService()
                .getAdminProducts(null, null)
                .enqueue(new Callback<ApiResponse<List<Product>>>() {

                    @Override
                    public void onResponse(
                            Call<ApiResponse<List<Product>>> call,
                            Response<ApiResponse<List<Product>>> response
                    ) {
                        pbProductsLoading.setVisibility(View.GONE);

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().getData() != null) {

                            allProducts = response.body().getData();
                            applyFilter();

                        } else {
                            Toast.makeText(
                                    AdminProductsActivity.this,
                                    "Failed to load products",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ApiResponse<List<Product>>> call,
                            Throwable t
                    ) {
                        pbProductsLoading.setVisibility(View.GONE);

                        Toast.makeText(
                                AdminProductsActivity.this,
                                "Network error: " + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private void applyFilter() {
        String query = etSearchProducts.getText()
                .toString()
                .trim()
                .toLowerCase(Locale.ROOT);

        List<Product> filtered = allProducts.stream()
                .filter(p -> p != null)
                .filter(p -> {
                    if ("AVAILABLE".equals(currentFilter)
                            && !p.isAvailable()) {
                        return false;
                    }

                    if ("LOW_STOCK".equals(currentFilter)) {
                        int stock = p.getStockQuantity();

                        if (stock > p.getLowStockThreshold()) {
                            return false;
                        }
                    }

                    if (!query.isEmpty()) {
                        String name = p.getName() != null
                                ? p.getName().toLowerCase(Locale.ROOT)
                                : "";

                        String category = p.getCategoryName() != null
                                ? p.getCategoryName().toLowerCase(Locale.ROOT)
                                : "";

                        return name.contains(query)
                                || category.contains(query);
                    }

                    return true;
                })
                .collect(Collectors.toList());

        adapter.setProducts(filtered);

        tvEmptyProducts.setVisibility(
                filtered.isEmpty() ? View.VISIBLE : View.GONE
        );
    }

    @Override
    public void onToggleAvailability(Product product) {
        if (product == null || product.getId() == null) {
            Toast.makeText(
                    this,
                    "Invalid product",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        boolean newAvailability = !product.isAvailable();

        ApiClient.getApiService()
                .toggleProductAvailabilityAdmin(
                        product.getId(),
                        newAvailability
                )
                .enqueue(new Callback<ApiResponse<Product>>() {

                    @Override
                    public void onResponse(
                            Call<ApiResponse<Product>> call,
                            Response<ApiResponse<Product>> response
                    ) {
                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isSuccess()) {

                            product.setAvailable(newAvailability);

                            applyFilter();

                            Toast.makeText(
                                    AdminProductsActivity.this,
                                    product.getName()
                                            + " availability updated",
                                    Toast.LENGTH_SHORT
                            ).show();

                        } else {
                            Toast.makeText(
                                    AdminProductsActivity.this,
                                    "Failed to update status",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ApiResponse<Product>> call,
                            Throwable t
                    ) {
                        Toast.makeText(
                                AdminProductsActivity.this,
                                "Error: " + t.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    @Override
    public void onEditProduct(Product product) {
        if (product == null || product.getId() == null) {
            Toast.makeText(
                    this,
                    "Invalid product",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 10);

        EditText etName = new EditText(this);
        etName.setHint("Product Name");
        etName.setText(product.getName());
        layout.addView(etName);

        EditText etPrice = new EditText(this);
        etPrice.setHint("Price");
        etPrice.setInputType(
                InputType.TYPE_CLASS_NUMBER
                        | InputType.TYPE_NUMBER_FLAG_DECIMAL
        );

        etPrice.setText(
                product.getPrice() != null
                        ? product.getPrice().toPlainString()
                        : ""
        );

        layout.addView(etPrice);

        EditText etUnit = new EditText(this);
        etUnit.setHint("Unit");
        etUnit.setText(product.getUnit());
        layout.addView(etUnit);

        new AlertDialog.Builder(this)
                .setTitle("Edit Product: " + product.getName())
                .setView(layout)
                .setPositiveButton("Save Changes", (dialog, which) -> {

                    String name = etName.getText()
                            .toString().trim();

                    String priceText = etPrice.getText()
                            .toString().trim();

                    String unit = etUnit.getText()
                            .toString().trim();

                    if (name.isEmpty() || priceText.isEmpty()) {
                        Toast.makeText(
                                this,
                                "Name and price are required",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    BigDecimal price;

                    try {
                        price = new BigDecimal(priceText);

                        if (price.compareTo(BigDecimal.ZERO) < 0) {
                            throw new NumberFormatException(
                                    "Price cannot be negative"
                            );
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(
                                this,
                                "Enter a valid non-negative price",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    ProductUpdateRequest request =
                            new ProductUpdateRequest();

                    request.setName(name);
                    request.setPrice(price);
                    request.setUnit(unit);

                    ApiClient.getApiService()
                            .updateAdminProduct(
                                    product.getId(),
                                    request
                            )
                            .enqueue(
                                    new Callback<ApiResponse<Product>>() {

                                        @Override
                                        public void onResponse(
                                                Call<ApiResponse<Product>> call,
                                                Response<ApiResponse<Product>> response
                                        ) {
                                            if (response.isSuccessful()
                                                    && response.body() != null
                                                    && response.body().isSuccess()) {

                                                Toast.makeText(
                                                        AdminProductsActivity.this,
                                                        "Product updated!",
                                                        Toast.LENGTH_SHORT
                                                ).show();

                                                loadProducts();

                                            } else {
                                                Toast.makeText(
                                                        AdminProductsActivity.this,
                                                        "Failed to update product",
                                                        Toast.LENGTH_SHORT
                                                ).show();
                                            }
                                        }

                                        @Override
                                        public void onFailure(
                                                Call<ApiResponse<Product>> call,
                                                Throwable t
                                        ) {
                                            Toast.makeText(
                                                    AdminProductsActivity.this,
                                                    "Error: " + t.getMessage(),
                                                    Toast.LENGTH_SHORT
                                            ).show();
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
        etName.setHint("Product Name (e.g. Fresh Apples)");
        layout.addView(etName);

        EditText etPrice = new EditText(this);
        etPrice.setHint("Price in ₹ (e.g. 120.00)");
        etPrice.setInputType(
                InputType.TYPE_CLASS_NUMBER
                        | InputType.TYPE_NUMBER_FLAG_DECIMAL
        );
        layout.addView(etPrice);

        EditText etUnit = new EditText(this);
        etUnit.setHint("Unit (e.g. 1 kg)");
        layout.addView(etUnit);

        EditText etStock = new EditText(this);
        etStock.setHint("Initial Stock Quantity (e.g. 80)");
        etStock.setInputType(InputType.TYPE_CLASS_NUMBER);
        layout.addView(etStock);

        new AlertDialog.Builder(this)
                .setTitle("Add New Product")
                .setView(layout)
                .setPositiveButton("Create", (dialog, which) -> {

                    String name = etName.getText()
                            .toString().trim();

                    String priceText = etPrice.getText()
                            .toString().trim();

                    String unit = etUnit.getText()
                            .toString().trim();

                    String stockText = etStock.getText()
                            .toString().trim();

                    if (name.isEmpty() || priceText.isEmpty()) {
                        Toast.makeText(
                                this,
                                "Name and price are required",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    if (allCategories.isEmpty()) {
                        Toast.makeText(
                                this,
                                "Create or load a category first",
                                Toast.LENGTH_LONG
                        ).show();
                        return;
                    }

                    BigDecimal price;
                    int stock;

                    try {
                        price = new BigDecimal(priceText);

                        if (price.compareTo(BigDecimal.ZERO) <= 0) {
                            throw new NumberFormatException(
                                    "Price must be positive"
                            );
                        }

                        stock = stockText.isEmpty()
                                ? 50
                                : Integer.parseInt(stockText);

                        if (stock < 0) {
                            throw new NumberFormatException(
                                    "Stock cannot be negative"
                            );
                        }

                    } catch (NumberFormatException e) {
                        Toast.makeText(
                                this,
                                "Enter a valid price and stock quantity",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    ProductCreateRequest request =
                            new ProductCreateRequest();

                    request.setName(name);
                    request.setPrice(price);
                    request.setOriginalPrice(
                            price.multiply(BigDecimal.valueOf(1.2))
                    );
                    request.setUnit(
                            unit.isEmpty() ? "1 unit" : unit
                    );
                    request.setCategoryId(
                            allCategories.get(0).getId()
                    );
                    request.setInitialStock(stock);
                    request.setLowStockThreshold(10);
                    request.setAvailable(true);

                    ApiClient.getApiService()
                            .createAdminProduct(request)
                            .enqueue(
                                    new Callback<ApiResponse<Product>>() {

                                        @Override
                                        public void onResponse(
                                                Call<ApiResponse<Product>> call,
                                                Response<ApiResponse<Product>> response
                                        ) {
                                            if (response.isSuccessful()
                                                    && response.body() != null
                                                    && response.body().isSuccess()) {

                                                Toast.makeText(
                                                        AdminProductsActivity.this,
                                                        "Product created!",
                                                        Toast.LENGTH_SHORT
                                                ).show();

                                                loadProducts();

                                            } else {
                                                Toast.makeText(
                                                        AdminProductsActivity.this,
                                                        "Failed to create product",
                                                        Toast.LENGTH_SHORT
                                                ).show();
                                            }
                                        }

                                        @Override
                                        public void onFailure(
                                                Call<ApiResponse<Product>> call,
                                                Throwable t
                                        ) {
                                            Toast.makeText(
                                                    AdminProductsActivity.this,
                                                    "Error: " + t.getMessage(),
                                                    Toast.LENGTH_SHORT
                                            ).show();
                                        }
                                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showCategoryManagementDialog() {
        StringBuilder builder = new StringBuilder();

        if (allCategories.isEmpty()) {
            builder.append("No categories found.");
        } else {
            for (Category category : allCategories) {
                builder.append("• ")
                        .append(category.getName())
                        .append("\n");
            }
        }

        new AlertDialog.Builder(this)
                .setTitle(
                        "Platform Categories ("
                                + allCategories.size()
                                + ")"
                )
                .setMessage(builder.toString())
                .setPositiveButton(
                        "+ Add Category",
                        (dialog, which) -> showAddCategoryDialog()
                )
                .setNegativeButton("Close", null)
                .show();
    }

    private void showAddCategoryDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 10);

        EditText etName = new EditText(this);
        etName.setHint("Category Name (e.g. Dairy & Bakery)");
        layout.addView(etName);

        EditText etDescription = new EditText(this);
        etDescription.setHint("Description (optional)");
        layout.addView(etDescription);

        new AlertDialog.Builder(this)
                .setTitle("Create New Category")
                .setView(layout)
                .setPositiveButton("Create", (dialog, which) -> {

                    String name = etName.getText()
                            .toString().trim();

                    String description = etDescription.getText()
                            .toString().trim();

                    if (name.isEmpty()) {
                        Toast.makeText(
                                this,
                                "Category name is required",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    CategoryRequest request = new CategoryRequest(
                            name,
                            description,
                            "ic_category",
                            null,
                            1
                    );

                    ApiClient.getApiService()
                            .createAdminCategory(request)
                            .enqueue(
                                    new Callback<ApiResponse<Category>>() {

                                        @Override
                                        public void onResponse(
                                                Call<ApiResponse<Category>> call,
                                                Response<ApiResponse<Category>> response
                                        ) {
                                            if (response.isSuccessful()
                                                    && response.body() != null
                                                    && response.body().isSuccess()) {

                                                Toast.makeText(
                                                        AdminProductsActivity.this,
                                                        "Category created!",
                                                        Toast.LENGTH_SHORT
                                                ).show();

                                                loadCategories();

                                            } else {
                                                Toast.makeText(
                                                        AdminProductsActivity.this,
                                                        "Failed to create category",
                                                        Toast.LENGTH_SHORT
                                                ).show();
                                            }
                                        }

                                        @Override
                                        public void onFailure(
                                                Call<ApiResponse<Category>> call,
                                                Throwable t
                                        ) {
                                            Toast.makeText(
                                                    AdminProductsActivity.this,
                                                    "Error: " + t.getMessage(),
                                                    Toast.LENGTH_SHORT
                                            ).show();
                                        }
                                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
