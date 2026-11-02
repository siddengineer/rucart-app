package com.rucart.app.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.rucart.app.R;
import com.rucart.app.api.ApiClient;
import com.rucart.app.model.ApiResponse;
import com.rucart.app.model.Store;
import com.rucart.app.ui.customer.adapter.ServiceAreaAdapter;
import com.rucart.app.ui.customer.adapter.StoreAdapter;
import com.rucart.app.util.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class StoreSelectionActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvCurrentLocality;
    private RecyclerView rvServiceAreas;
    private RecyclerView rvAvailableStores;
    private ProgressBar pbStoresLoading;
    private TextView tvNoStoresMessage;

    private ServiceAreaAdapter areaAdapter;
    private StoreAdapter storeAdapter;

    private String currentSelectedArea = "Shivajinagar";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_selection);

        initViews();
        setupListeners();
        loadServiceAreas();
        loadStoresForArea(currentSelectedArea);
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBackStoreSelect);
        tvCurrentLocality = findViewById(R.id.tvCurrentLocality);
        rvServiceAreas = findViewById(R.id.rvServiceAreas);
        rvAvailableStores = findViewById(R.id.rvAvailableStores);
        pbStoresLoading = findViewById(R.id.pbStoresLoading);
        tvNoStoresMessage = findViewById(R.id.tvNoStoresMessage);

        currentSelectedArea = SessionManager.getInstance().getSelectedServiceArea();
        tvCurrentLocality.setText(currentSelectedArea);

        rvServiceAreas.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        areaAdapter = new ServiceAreaAdapter(this::onAreaSelected);
        rvServiceAreas.setAdapter(areaAdapter);

        rvAvailableStores.setLayoutManager(new LinearLayoutManager(this));
        storeAdapter = new StoreAdapter(this::onStoreChosen);
        rvAvailableStores.setAdapter(storeAdapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void onAreaSelected(String area) {
        this.currentSelectedArea = area;
        tvCurrentLocality.setText(area + ", Pune");
        SessionManager.getInstance().setSelectedServiceArea(area + ", Pune");
        loadStoresForArea(area);
    }

    private void onStoreChosen(Store store) {
        SessionManager.getInstance().setSelectedStore(store.getId(), store.getName());
        Toast.makeText(this, "Selected Store: " + store.getName(), Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK, new Intent().putExtra("selected_store_id", store.getId()));
        finish();
    }

    private void loadServiceAreas() {
        ApiClient.getApiService().getServiceAreas().enqueue(new Callback<ApiResponse<List<String>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<String>>> call, Response<ApiResponse<List<String>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<String> areas = response.body().getData();
                    areaAdapter.setAreas(areas, currentSelectedArea);
                } else {
                    fallbackServiceAreas();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<String>>> call, Throwable t) {
                fallbackServiceAreas();
            }
        });
    }

    private void fallbackServiceAreas() {
        List<String> areas = List.of("Shivajinagar", "FC Road", "Kothrud", "Paud Road", "Viman Nagar", "Hinjewadi", "Baner", "Aundh");
        areaAdapter.setAreas(areas, currentSelectedArea);
    }

    private void loadStoresForArea(String area) {
        pbStoresLoading.setVisibility(View.VISIBLE);
        tvNoStoresMessage.setVisibility(View.GONE);

        ApiClient.getApiService().getStoresByArea(area).enqueue(new Callback<ApiResponse<List<Store>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Store>>> call, Response<ApiResponse<List<Store>>> response) {
                pbStoresLoading.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null && !response.body().getData().isEmpty()) {
                    List<Store> stores = response.body().getData();
                    storeAdapter.setStores(stores);
                    tvNoStoresMessage.setVisibility(View.GONE);
                } else {
                    // Fallback to all open stores
                    loadAllOpenStores();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Store>>> call, Throwable t) {
                pbStoresLoading.setVisibility(View.GONE);
                loadAllOpenStores();
            }
        });
    }

    private void loadAllOpenStores() {
        ApiClient.getApiService().getStores().enqueue(new Callback<ApiResponse<List<Store>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Store>>> call, Response<ApiResponse<List<Store>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<Store> stores = response.body().getData();
                    storeAdapter.setStores(stores);
                    tvNoStoresMessage.setVisibility(stores.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    tvNoStoresMessage.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Store>>> call, Throwable t) {
                tvNoStoresMessage.setVisibility(View.VISIBLE);
            }
        });
    }
}
