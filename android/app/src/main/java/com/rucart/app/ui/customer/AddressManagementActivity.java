package com.rucart.app.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.rucart.app.R;
import com.rucart.app.api.ApiClient;
import com.rucart.app.model.ApiResponse;
import com.rucart.app.model.CustomerAddress;
import com.rucart.app.ui.customer.adapter.AddressAdapter;
import com.rucart.app.util.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class AddressManagementActivity extends AppCompatActivity {

    private ImageView btnBack;
    private Button btnAddNewAddress;
    private RecyclerView rvAddresses;
    private ProgressBar pbAddresses;
    private AddressAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_management);

        initViews();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAddresses();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBackAddress);
        btnAddNewAddress = findViewById(R.id.btnAddNewAddress);
        rvAddresses = findViewById(R.id.rvAddresses);
        pbAddresses = findViewById(R.id.pbAddresses);

        rvAddresses.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AddressAdapter(new AddressAdapter.AddressActionsListener() {
            @Override
            public void onAddressSelected(CustomerAddress address) {
                SessionManager.getInstance().setSelectedAddress(address);
                Toast.makeText(AddressManagementActivity.this, "Delivery address updated to " + address.getTitle(), Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onAddressEdit(CustomerAddress address) {
                Intent intent = new Intent(AddressManagementActivity.this, AddAddressActivity.class);
                intent.putExtra(AddAddressActivity.EXTRA_ADDRESS, address);
                startActivity(intent);
            }

            @Override
            public void onAddressDelete(CustomerAddress address) {
                confirmDeleteAddress(address);
            }
        });
        rvAddresses.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnAddNewAddress.setOnClickListener(v -> {
            startActivity(new Intent(AddressManagementActivity.this, AddAddressActivity.class));
        });
    }

    private void loadAddresses() {
        pbAddresses.setVisibility(View.VISIBLE);

        CustomerAddress current = SessionManager.getInstance().getSelectedCustomerAddress();
        Long currentId = current != null ? current.getId() : null;

        ApiClient.getApiService().getCustomerAddresses().enqueue(new Callback<ApiResponse<List<CustomerAddress>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<CustomerAddress>>> call, Response<ApiResponse<List<CustomerAddress>>> response) {
                pbAddresses.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null && !response.body().getData().isEmpty()) {
                    List<CustomerAddress> list = response.body().getData();
                    adapter.setAddresses(list, currentId != null ? currentId : list.get(0).getId());
                    if (current == null && !list.isEmpty()) {
                        SessionManager.getInstance().setSelectedAddress(list.get(0));
                    }
                } else {
                    adapter.setAddresses(new ArrayList<>(), null);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<CustomerAddress>>> call, Throwable t) {
                pbAddresses.setVisibility(View.GONE);
                adapter.setAddresses(new ArrayList<>(), null);
            }
        });
    }

    private void confirmDeleteAddress(CustomerAddress address) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Address")
                .setMessage("Are you sure you want to delete this address (" + address.getTitle() + ")?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (address.getId() != null) {
                        ApiClient.getApiService().deleteAddress(address.getId()).enqueue(new Callback<ApiResponse<Void>>() {
                            @Override
                            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                                Toast.makeText(AddressManagementActivity.this, "Address deleted", Toast.LENGTH_SHORT).show();
                                loadAddresses();
                            }

                            @Override
                            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                                Toast.makeText(AddressManagementActivity.this, "Failed to delete: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
