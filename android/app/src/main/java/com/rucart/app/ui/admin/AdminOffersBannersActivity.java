package com.rucart.app.ui.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import com.google.android.material.tabs.TabLayout;
import com.rucart.app.R;
import com.rucart.app.api.ApiClient;
import com.rucart.app.model.ApiResponse;
import com.rucart.app.model.Offer;
import com.rucart.app.model.PromotionalBanner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminOffersBannersActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private LinearLayout layoutOffers;
    private LinearLayout layoutBanners;
    private SwipeRefreshLayout swipeRefreshOffers;
    private SwipeRefreshLayout swipeRefreshBanners;
    private RecyclerView rvOffers;
    private RecyclerView rvBanners;
    private ExtendedFloatingActionButton fabAddOfferBanner;

    private OfferAdapter offerAdapter;
    private BannerAdapter bannerAdapter;

    private boolean isShowingOffers = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_offers_banners);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        tabLayout = findViewById(R.id.tabLayoutOffersBanners);
        layoutOffers = findViewById(R.id.layoutOffers);
        layoutBanners = findViewById(R.id.layoutBanners);
        swipeRefreshOffers = findViewById(R.id.swipeRefreshOffers);
        swipeRefreshBanners = findViewById(R.id.swipeRefreshBanners);
        rvOffers = findViewById(R.id.rvOffers);
        rvBanners = findViewById(R.id.rvBanners);
        fabAddOfferBanner = findViewById(R.id.fabAddOfferBanner);

        offerAdapter = new OfferAdapter();
        rvOffers.setLayoutManager(new LinearLayoutManager(this));
        rvOffers.setAdapter(offerAdapter);

        bannerAdapter = new BannerAdapter();
        rvBanners.setLayoutManager(new LinearLayoutManager(this));
        rvBanners.setAdapter(bannerAdapter);

        setupListeners();
        loadOffers();
        loadBanners();
    }

    private void setupListeners() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    isShowingOffers = true;
                    layoutOffers.setVisibility(View.VISIBLE);
                    layoutBanners.setVisibility(View.GONE);
                    fabAddOfferBanner.setText("New Offer");
                    loadOffers();
                } else {
                    isShowingOffers = false;
                    layoutOffers.setVisibility(View.GONE);
                    layoutBanners.setVisibility(View.VISIBLE);
                    fabAddOfferBanner.setText("New Banner");
                    loadBanners();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        swipeRefreshOffers.setOnRefreshListener(this::loadOffers);
        swipeRefreshBanners.setOnRefreshListener(this::loadBanners);

        fabAddOfferBanner.setOnClickListener(v -> {
            if (isShowingOffers) {
                showCreateOfferDialog();
            } else {
                showCreateBannerDialog();
            }
        });
    }

    private void loadOffers() {
        swipeRefreshOffers.setRefreshing(true);
        ApiClient.getApiService().getAllAdminOffers().enqueue(new Callback<ApiResponse<List<Offer>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Offer>>> call, Response<ApiResponse<List<Offer>>> response) {
                swipeRefreshOffers.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    offerAdapter.setOffers(response.body().getData());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Offer>>> call, Throwable t) {
                swipeRefreshOffers.setRefreshing(false);
            }
        });
    }

    private void loadBanners() {
        swipeRefreshBanners.setRefreshing(true);
        ApiClient.getApiService().getAllAdminBanners().enqueue(new Callback<ApiResponse<List<PromotionalBanner>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<PromotionalBanner>>> call, Response<ApiResponse<List<PromotionalBanner>>> response) {
                swipeRefreshBanners.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    bannerAdapter.setBanners(response.body().getData());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<PromotionalBanner>>> call, Throwable t) {
                swipeRefreshBanners.setRefreshing(false);
            }
        });
    }

    private void showCreateOfferDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(30, 20, 30, 20);

        final EditText etTitle = new EditText(this);
        etTitle.setHint("Offer Title (e.g. Summer Savings)");
        layout.addView(etTitle);

        final EditText etCode = new EditText(this);
        etCode.setHint("Coupon Code (e.g. SAVE20)");
        layout.addView(etCode);

        final EditText etDiscount = new EditText(this);
        etDiscount.setHint("Discount Value (e.g. 20)");
        layout.addView(etDiscount);

        final EditText etMinOrder = new EditText(this);
        etMinOrder.setHint("Min Order Amount (e.g. 199)");
        layout.addView(etMinOrder);

        new AlertDialog.Builder(this)
                .setTitle("Create Promotional Offer")
                .setView(layout)
                .setPositiveButton("Create", (dialog, which) -> {
                    String title = etTitle.getText().toString().trim();
                    String code = etCode.getText().toString().trim();
                    String discStr = etDiscount.getText().toString().trim();
                    String minStr = etMinOrder.getText().toString().trim();

                    if (title.isEmpty() || code.isEmpty() || discStr.isEmpty()) {
                        Toast.makeText(this, "Title, code and discount are required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Offer offer = new Offer();
                    offer.setTitle(title);
                    offer.setCode(code);
                    offer.setDescription("Save on your order with code " + code);
                    offer.setDiscountType("PERCENTAGE");
                    offer.setDiscountValue(new BigDecimal(discStr));
                    offer.setMinOrderAmount(minStr.isEmpty() ? BigDecimal.ZERO : new BigDecimal(minStr));
                    offer.setActive(true);

                    ApiClient.getApiService().createOffer(offer).enqueue(new Callback<ApiResponse<Offer>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Offer>> call, Response<ApiResponse<Offer>> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(AdminOffersBannersActivity.this, "Offer created successfully", Toast.LENGTH_SHORT).show();
                                loadOffers();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Offer>> call, Throwable t) {
                            Toast.makeText(AdminOffersBannersActivity.this, "Error creating offer", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showCreateBannerDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(30, 20, 30, 20);

        final EditText etTitle = new EditText(this);
        etTitle.setHint("Banner Title");
        layout.addView(etTitle);

        final EditText etImageUrl = new EditText(this);
        etImageUrl.setHint("Banner Image URL (e.g. /banners/festive.jpg)");
        layout.addView(etImageUrl);

        final EditText etOrder = new EditText(this);
        etOrder.setHint("Display Order (1, 2, 3...)");
        layout.addView(etOrder);

        new AlertDialog.Builder(this)
                .setTitle("Publish Promotional Banner")
                .setView(layout)
                .setPositiveButton("Publish", (dialog, which) -> {
                    String title = etTitle.getText().toString().trim();
                    String imgUrl = etImageUrl.getText().toString().trim();
                    String orderStr = etOrder.getText().toString().trim();

                    if (title.isEmpty() || imgUrl.isEmpty()) {
                        Toast.makeText(this, "Title and Image URL are required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    PromotionalBanner banner = new PromotionalBanner();
                    banner.setTitle(title);
                    banner.setImageUrl(imgUrl);
                    banner.setDisplayOrder(orderStr.isEmpty() ? 1 : Integer.parseInt(orderStr));
                    banner.setActive(true);

                    ApiClient.getApiService().createBanner(banner).enqueue(new Callback<ApiResponse<PromotionalBanner>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<PromotionalBanner>> call, Response<ApiResponse<PromotionalBanner>> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(AdminOffersBannersActivity.this, "Banner published successfully", Toast.LENGTH_SHORT).show();
                                loadBanners();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<PromotionalBanner>> call, Throwable t) {
                            Toast.makeText(AdminOffersBannersActivity.this, "Error publishing banner", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // --- Offer Adapter ---
    private class OfferAdapter extends RecyclerView.Adapter<OfferAdapter.ViewHolder> {
        private List<Offer> items = new ArrayList<>();

        void setOffers(List<Offer> list) {
            this.items = list != null ? list : new ArrayList<>();
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_offer, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Offer offer = items.get(position);
            holder.tvOfferTitle.setText(offer.getTitle());
            holder.tvOfferCode.setText("Code: " + offer.getCode() + " (" + offer.getDiscountValue() +
                    ("PERCENTAGE".equalsIgnoreCase(offer.getDiscountType()) ? "% OFF" : " ₹ OFF") + ")");
            holder.tvOfferDesc.setText(offer.getDescription() != null ? offer.getDescription() : "Standard discount");
            holder.tvOfferDates.setText("Min Order: ₹ " + offer.getMinOrderAmount() +
                    (offer.getExpiryDate() != null ? " • Valid until: " + offer.getExpiryDate() : ""));

            holder.tvOfferActiveBadge.setText(offer.isActive() ? "ACTIVE" : "INACTIVE");
            holder.tvOfferActiveBadge.setBackgroundColor(getResources().getColor(offer.isActive() ? R.color.primary_green : R.color.danger_red));

            holder.btnOfferToggleActive.setText(offer.isActive() ? "Deactivate" : "Activate");
            holder.btnOfferToggleActive.setOnClickListener(v -> {
                offer.setActive(!offer.isActive());
                ApiClient.getApiService().updateOffer(offer.getId(), offer).enqueue(new Callback<ApiResponse<Offer>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Offer>> call, Response<ApiResponse<Offer>> response) {
                        loadOffers();
                    }
                    @Override
                    public void onFailure(Call<ApiResponse<Offer>> call, Throwable t) {}
                });
            });

            holder.btnOfferDelete.setOnClickListener(v -> {
                ApiClient.getApiService().deleteOffer(offer.getId()).enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                        loadOffers();
                    }
                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {}
                });
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvOfferTitle, tvOfferCode, tvOfferDesc, tvOfferDates, tvOfferActiveBadge;
            MaterialButton btnOfferToggleActive, btnOfferDelete;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvOfferTitle = itemView.findViewById(R.id.tvOfferTitle);
                tvOfferCode = itemView.findViewById(R.id.tvOfferCode);
                tvOfferDesc = itemView.findViewById(R.id.tvOfferDesc);
                tvOfferDates = itemView.findViewById(R.id.tvOfferDates);
                tvOfferActiveBadge = itemView.findViewById(R.id.tvOfferActiveBadge);
                btnOfferToggleActive = itemView.findViewById(R.id.btnOfferToggleActive);
                btnOfferDelete = itemView.findViewById(R.id.btnOfferDelete);
            }
        }
    }

    // --- Banner Adapter ---
    private class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.ViewHolder> {
        private List<PromotionalBanner> items = new ArrayList<>();

        void setBanners(List<PromotionalBanner> list) {
            this.items = list != null ? list : new ArrayList<>();
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_banner, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            PromotionalBanner banner = items.get(position);
            holder.tvBannerTitle.setText(banner.getTitle());
            holder.tvBannerOrderBadge.setText("Order #" + banner.getDisplayOrder());
            holder.tvBannerImageUrl.setText("Image: " + banner.getImageUrl());

            holder.btnBannerToggleActive.setText(banner.isActive() ? "Active" : "Inactive");
            holder.btnBannerToggleActive.setTextColor(getResources().getColor(banner.isActive() ? R.color.primary_green : R.color.danger_red));

            holder.btnBannerToggleActive.setOnClickListener(v -> {
                banner.setActive(!banner.isActive());
                ApiClient.getApiService().updateBanner(banner.getId(), banner).enqueue(new Callback<ApiResponse<PromotionalBanner>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<PromotionalBanner>> call, Response<ApiResponse<PromotionalBanner>> response) {
                        loadBanners();
                    }
                    @Override
                    public void onFailure(Call<ApiResponse<PromotionalBanner>> call, Throwable t) {}
                });
            });

            holder.btnBannerDelete.setOnClickListener(v -> {
                ApiClient.getApiService().deleteBanner(banner.getId()).enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                        loadBanners();
                    }
                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {}
                });
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvBannerTitle, tvBannerOrderBadge, tvBannerImageUrl;
            MaterialButton btnBannerToggleActive, btnBannerDelete;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvBannerTitle = itemView.findViewById(R.id.tvBannerTitle);
                tvBannerOrderBadge = itemView.findViewById(R.id.tvBannerOrderBadge);
                tvBannerImageUrl = itemView.findViewById(R.id.tvBannerImageUrl);
                btnBannerToggleActive = itemView.findViewById(R.id.btnBannerToggleActive);
                btnBannerDelete = itemView.findViewById(R.id.btnBannerDelete);
            }
        }
    }
}
