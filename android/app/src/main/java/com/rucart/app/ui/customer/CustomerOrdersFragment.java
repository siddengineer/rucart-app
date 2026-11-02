package com.rucart.app.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.rucart.app.R;
import com.rucart.app.api.ApiClient;
import com.rucart.app.model.ApiResponse;
import com.rucart.app.model.Order;
import com.rucart.app.ui.customer.adapter.OrderAdapter;
import com.rucart.app.util.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class CustomerOrdersFragment extends Fragment {

    private RecyclerView rvCustomerOrders;
    private OrderAdapter orderAdapter;
    private ProgressBar pbOrders;
    private TextView tvEmptyOrders;
    private View layoutGuestOrders;
    private Button btnOrdersLogin;
    private View layoutFilterChips;
    private TextView chipAll, chipActive, chipDelivered, chipCancelled;

    private List<Order> allOrders = new ArrayList<>();
    private String currentFilter = "ALL";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customer_orders, container, false);

        rvCustomerOrders = view.findViewById(R.id.rvCustomerOrders);
        pbOrders = view.findViewById(R.id.pbCustomerOrders);
        tvEmptyOrders = view.findViewById(R.id.tvEmptyOrders);
        layoutGuestOrders = view.findViewById(R.id.layoutGuestOrders);
        btnOrdersLogin = view.findViewById(R.id.btnOrdersLogin);

        chipAll = view.findViewById(R.id.chipFilterAll);
        chipActive = view.findViewById(R.id.chipFilterActive);
        chipDelivered = view.findViewById(R.id.chipFilterDelivered);
        chipCancelled = view.findViewById(R.id.chipFilterCancelled);

        if (btnOrdersLogin != null) {
            btnOrdersLogin.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), com.rucart.app.ui.auth.LoginActivity.class);
                intent.putExtra(com.rucart.app.ui.auth.LoginActivity.EXTRA_RETURN_TO_CALLER, true);
                startActivity(intent);
            });
        }

        rvCustomerOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        orderAdapter = new OrderAdapter(order -> {
            Intent intent = new Intent(getContext(), OrderDetailActivity.class);
            intent.putExtra(OrderDetailActivity.EXTRA_ORDER, order);
            startActivity(intent);
        });
        rvCustomerOrders.setAdapter(orderAdapter);

        setupFilterListeners();
        loadCustomerOrders();

        return view;
    }

    private void setupFilterListeners() {
        chipAll.setOnClickListener(v -> setFilter("ALL"));
        chipActive.setOnClickListener(v -> setFilter("ACTIVE"));
        chipDelivered.setOnClickListener(v -> setFilter("DELIVERED"));
        chipCancelled.setOnClickListener(v -> setFilter("CANCELLED"));
    }

    private void setFilter(String filter) {
        this.currentFilter = filter;
        updateChipStyles();
        applyFilter();
    }

    private void updateChipStyles() {
        if (getContext() == null) return;
        int activeBg = R.drawable.bg_button_primary;
        int activeText = getContext().getColor(R.color.white);
        int inactiveBg = R.drawable.bg_badge_green;
        int inactiveText = getContext().getColor(R.color.primary_green_dark);

        chipAll.setBackgroundResource("ALL".equals(currentFilter) ? activeBg : inactiveBg);
        chipAll.setTextColor("ALL".equals(currentFilter) ? activeText : inactiveText);

        chipActive.setBackgroundResource("ACTIVE".equals(currentFilter) ? activeBg : inactiveBg);
        chipActive.setTextColor("ACTIVE".equals(currentFilter) ? activeText : inactiveText);

        chipDelivered.setBackgroundResource("DELIVERED".equals(currentFilter) ? activeBg : inactiveBg);
        chipDelivered.setTextColor("DELIVERED".equals(currentFilter) ? activeText : inactiveText);

        chipCancelled.setBackgroundResource("CANCELLED".equals(currentFilter) ? activeBg : inactiveBg);
        chipCancelled.setTextColor("CANCELLED".equals(currentFilter) ? activeText : inactiveText);
    }

    private void applyFilter() {
        List<Order> filtered = new ArrayList<>();
        for (Order o : allOrders) {
            String st = o.getStatus() != null ? o.getStatus().toUpperCase() : "PLACED";
            if ("ALL".equals(currentFilter)) {
                filtered.add(o);
            } else if ("ACTIVE".equals(currentFilter)) {
                if (!"DELIVERED".equals(st) && !"CANCELLED".equals(st) && !"REFUNDED".equals(st)) {
                    filtered.add(o);
                }
            } else if ("DELIVERED".equals(currentFilter)) {
                if ("DELIVERED".equals(st)) {
                    filtered.add(o);
                }
            } else if ("CANCELLED".equals(currentFilter)) {
                if ("CANCELLED".equals(st) || "REFUND_PENDING".equals(st) || "REFUNDED".equals(st)) {
                    filtered.add(o);
                }
            }
        }
        orderAdapter.setOrders(filtered);
        tvEmptyOrders.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCustomerOrders();
    }

    private void loadCustomerOrders() {
        if (!SessionManager.getInstance().isLoggedIn()) {
            if (layoutGuestOrders != null) layoutGuestOrders.setVisibility(View.VISIBLE);
            if (rvCustomerOrders != null) rvCustomerOrders.setVisibility(View.GONE);
            if (tvEmptyOrders != null) tvEmptyOrders.setVisibility(View.GONE);
            if (pbOrders != null) pbOrders.setVisibility(View.GONE);
            return;
        }

        if (layoutGuestOrders != null) layoutGuestOrders.setVisibility(View.GONE);
        if (rvCustomerOrders != null) rvCustomerOrders.setVisibility(View.VISIBLE);

        Long customerId = SessionManager.getInstance().getUserId();
        if (customerId == null) return;

        if (pbOrders != null) pbOrders.setVisibility(View.VISIBLE);
        if (tvEmptyOrders != null) tvEmptyOrders.setVisibility(View.GONE);

        ApiClient.getApiService().getCustomerOrders(customerId).enqueue(new Callback<ApiResponse<List<Order>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Order>>> call, Response<ApiResponse<List<Order>>> response) {
                if (pbOrders != null) pbOrders.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    allOrders = response.body().getData();
                    applyFilter();
                } else {
                    applyFilter();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Order>>> call, Throwable t) {
                if (pbOrders != null) pbOrders.setVisibility(View.GONE);
                applyFilter();
            }
        });
    }
}
