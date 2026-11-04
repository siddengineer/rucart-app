package com.rucart.app.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.rucart.app.R;
import com.rucart.app.api.ApiClient;
import com.rucart.app.model.ApiResponse;
import com.rucart.app.model.Category;
import com.rucart.app.model.Product;
import com.rucart.app.ui.customer.adapter.CategoryAdapter;
import com.rucart.app.ui.customer.adapter.ProductAdapter;
import com.rucart.app.util.CartManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CustomerHomeFragment extends Fragment {

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvCategories, rvProducts;
    private CategoryAdapter categoryAdapter;
    private ProductAdapter productAdapter;
    private EditText etSearch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customer_home, container, false);

        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        rvCategories = view.findViewById(R.id.rvCategories);
        rvProducts = view.findViewById(R.id.rvProducts);
        etSearch = view.findViewById(R.id.etSearch);
        Button btnShopNow = view.findViewById(R.id.btnShopNow);
        TextView tvSeeAllCategories = view.findViewById(R.id.tvSeeAllCategories);

        // Setup Categories RecyclerView (Horizontal)
        rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        categoryAdapter = new CategoryAdapter(category -> filterProductsByCategory(category.getId()));
        rvCategories.setAdapter(categoryAdapter);

        // Setup Products RecyclerView (Vertical)
        rvProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        productAdapter = new ProductAdapter(product -> {
            if (!com.rucart.app.util.SessionManager.getInstance().isLoggedIn()) {
                com.rucart.app.util.AuthPromptDialog.showLoginRequired(getContext(), "add " + product.getName() + " to your cart");
                return;
            }
            CartManager.getInstance().addProduct(product);
            Toast.makeText(getContext(), product.getName() + " added to cart", Toast.LENGTH_SHORT).show();
        });
        productAdapter.setOnProductClickListener(product -> {
            Intent intent = new Intent(getContext(), ProductDetailActivity.class);
            intent.putExtra(ProductDetailActivity.EXTRA_PRODUCT, product);
            startActivity(intent);
        });
        rvProducts.setAdapter(productAdapter);

        // Pull to refresh
        swipeRefresh.setOnRefreshListener(this::loadData);

        // Search listener
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchProducts(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        tvSeeAllCategories.setOnClickListener(v -> {
            if (getActivity() instanceof CustomerMainActivity) {
                ((CustomerMainActivity) getActivity()).navigateToTab(R.id.nav_customer_categories);
            }
        });

        View btnSelectArea = view.findViewById(R.id.btnSelectServiceAreaHome);
        TextView tvDeliveryArea = view.findViewById(R.id.tvHomeDeliveryArea);
        TextView tvStoreBadge = view.findViewById(R.id.tvHomeStoreBadge);
        View btnNotifications = view.findViewById(R.id.btnHomeNotifications);
        TextView tvNotificationBadge = view.findViewById(R.id.tvHomeNotificationBadge);

        if (btnNotifications != null) {
            btnNotifications.setOnClickListener(v -> {
                startActivity(new Intent(getContext(), NotificationsActivity.class));
            });
        }

        if (btnSelectArea != null) {
            btnSelectArea.setOnClickListener(v -> {
                startActivity(new Intent(getContext(), StoreSelectionActivity.class));
            });
        }

        String area = com.rucart.app.util.SessionManager.getInstance().getSelectedServiceArea();
        if (tvDeliveryArea != null && area != null) {
            tvDeliveryArea.setText(area);
        }

        btnShopNow.setOnClickListener(v -> {
            if (getActivity() instanceof CustomerMainActivity) {
                ((CustomerMainActivity) getActivity()).navigateToTab(R.id.nav_customer_categories);
            }
        });

        loadData();
        updateNotificationCount(tvNotificationBadge);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getView() != null) {
            TextView tvDeliveryArea = getView().findViewById(R.id.tvHomeDeliveryArea);
            if (tvDeliveryArea != null) {
                tvDeliveryArea.setText(com.rucart.app.util.SessionManager.getInstance().getSelectedServiceArea());
            }
            TextView tvNotificationBadge = getView().findViewById(R.id.tvHomeNotificationBadge);
            updateNotificationCount(tvNotificationBadge);
        }
    }

    private void updateNotificationCount(TextView badgeView) {
        if (badgeView == null) return;
        ApiClient.getApiService().getUnreadNotificationCount().enqueue(new Callback<ApiResponse<Long>>() {
            @Override
            public void onResponse(Call<ApiResponse<Long>> call, Response<ApiResponse<Long>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    long count = response.body().getData();
                    if (count > 0) {
                        badgeView.setVisibility(View.VISIBLE);
                        badgeView.setText(count > 99 ? "99+" : String.valueOf(count));
                    } else {
                        badgeView.setVisibility(View.GONE);
                    }
                } else {
                    badgeView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Long>> call, Throwable t) {
                badgeView.setVisibility(View.GONE);
            }
        });
    }

    private void loadData() {
        swipeRefresh.setRefreshing(true);
        loadCategories();
        loadProducts(null, null);
        loadOffersAndBanners();
    }

    private void loadOffersAndBanners() {
        if (getView() == null) return;
        TextView tvTitle = getView().findViewById(R.id.tvHomeBannerTitle);
        TextView tvSub = getView().findViewById(R.id.tvHomeBannerSubtitle);

        ApiClient.getApiService().getActiveOffers().enqueue(new Callback<ApiResponse<List<com.rucart.app.model.Offer>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<com.rucart.app.model.Offer>>> call, Response<ApiResponse<List<com.rucart.app.model.Offer>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null && !response.body().getData().isEmpty()) {
                    com.rucart.app.model.Offer offer = response.body().getData().get(0);
                    if (tvTitle != null) tvTitle.setText(offer.getTitle());
                    if (tvSub != null) tvSub.setText("Code: " + offer.getCode() + " • " + (offer.getDescription() != null ? offer.getDescription() : "Special Discount"));
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<List<com.rucart.app.model.Offer>>> call, Throwable t) {}
        });
    }

    private void loadCategories() {
        ApiClient.getApiService().getCategories().enqueue(new Callback<ApiResponse<List<Category>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Category>>> call, Response<ApiResponse<List<Category>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    categoryAdapter.setCategories(getFullBlinkitCategories(response.body().getData()));
                } else {
                    categoryAdapter.setCategories(getFullBlinkitCategories(null));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Category>>> call, Throwable t) {
                categoryAdapter.setCategories(getFullBlinkitCategories(null));
            }
        });
    }

    private void loadProducts(Long categoryId, String search) {
        ApiClient.getApiService().getProducts(categoryId, null, search).enqueue(new Callback<ApiResponse<List<Product>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Product>>> call, Response<ApiResponse<List<Product>>> response) {
                swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    productAdapter.setProducts(response.body().getData());
                } else {
                    productAdapter.setProducts(getFallbackProducts());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Product>>> call, Throwable t) {
                swipeRefresh.setRefreshing(false);
                productAdapter.setProducts(getFallbackProducts());
            }
        });
    }

    private void filterProductsByCategory(Long categoryId) {
        loadProducts(categoryId, null);
    }

    private void searchProducts(String query) {
        loadProducts(null, query);
    }

    private List<Category> getFullBlinkitCategories(List<Category> backendCategories) {
        List<Category> list = new ArrayList<>();
        if (backendCategories != null && !backendCategories.isEmpty()) {
            list.addAll(backendCategories);
        } else {
            list.addAll(getFallbackCategories());
        }
        addIfMissing(list, 101L, "Bakery & Biscuits", "cat_bakery_biscuits");
        addIfMissing(list, 102L, "Breakfast & Instant", "cat_instant_food");
        addIfMissing(list, 103L, "Tea & Coffee", "cat_tea_coffee");
        addIfMissing(list, 104L, "Cold Drinks & Juices", "cat_cold_drinks");
        addIfMissing(list, 105L, "Sweet Tooth", "cat_sweet_tooth");
        addIfMissing(list, 106L, "Atta, Rice & Dal", "cat_atta_rice_dal");
        addIfMissing(list, 107L, "Masala & Oil", "cat_masala_oil");
        addIfMissing(list, 108L, "Cleaning Essentials", "cat_cleaning");
        addIfMissing(list, 109L, "Personal Care", "cat_personal_care");
        addIfMissing(list, 110L, "Baby Care", "cat_baby_care");
        addIfMissing(list, 111L, "Pet Care", "cat_pet_care");
        return list;
    }

    private void addIfMissing(List<Category> list, Long id, String name, String icon) {
        for (Category c : list) {
            if (c.getName() != null && c.getName().equalsIgnoreCase(name)) return;
        }
        list.add(new Category(id, name, icon));
    }

    private List<Category> getFallbackCategories() {
        List<Category> list = new ArrayList<>();
        list.add(new Category(1L, "Fruits & Veg", "ic_vegetables"));
        list.add(new Category(2L, "Dairy & Eggs", "ic_dairy"));
        list.add(new Category(3L, "Snacks & Drinks", "ic_snacks"));
        list.add(new Category(4L, "Household", "ic_household"));
        return list;
    }

    private List<Product> getFallbackProducts() {
        List<Product> list = new ArrayList<>();
        list.add(new Product(1L, "Tomato Local", new BigDecimal("24.00"), new BigDecimal("28.00"), "1 kg"));
        list.add(new Product(2L, "Fresh Onion", new BigDecimal("28.00"), new BigDecimal("32.00"), "1 kg"));
        list.add(new Product(3L, "Potato Jyoti", new BigDecimal("22.00"), new BigDecimal("26.00"), "1 kg"));
        list.add(new Product(4L, "Shimla Apple", new BigDecimal("130.00"), new BigDecimal("150.00"), "1 kg"));
        list.add(new Product(5L, "Robusta Banana", new BigDecimal("40.00"), new BigDecimal("48.00"), "1 kg"));
        list.add(new Product(6L, "Fresh Toned Milk", new BigDecimal("35.00"), new BigDecimal("36.00"), "1 L"));
        list.add(new Product(7L, "Whole Wheat Bread", new BigDecimal("40.00"), new BigDecimal("45.00"), "400 g"));
        list.add(new Product(8L, "Farm Eggs Pack", new BigDecimal("70.00"), new BigDecimal("80.00"), "6 pcs"));
        return list;
    }
}
