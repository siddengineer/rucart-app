package com.rucart.app.ui.customer;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.rucart.app.R;
import com.rucart.app.api.ApiClient;
import com.rucart.app.model.ApiResponse;
import com.rucart.app.model.CustomerAddress;
import com.rucart.app.util.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddAddressActivity extends AppCompatActivity {

    public static final String EXTRA_ADDRESS = "extra_address";

    private Spinner spinnerTag, spinnerDeliveryArea;
    private EditText etRecipient, etPhone, etLine1, etLandmark, etPinCode;
    private CheckBox cbSetDefault;
    private Button btnSave;
    private CustomerAddress existingAddress;

    private static final String[] TAGS = new String[]{"Home", "Office", "Other"};
    private final List<String> serviceAreaList = new ArrayList<>(Arrays.asList(
            "Shivajinagar, Pune",
            "Kothrud, Pune",
            "Viman Nagar, Pune",
            "Hinjewadi, Pune",
            "Baner, Pune",
            "Wakad, Pune",
            "Aundh, Pune",
            "Hadapsar, Pune"
    ));
    private ArrayAdapter<String> areaAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_address);

        existingAddress = (CustomerAddress) getIntent().getSerializableExtra(EXTRA_ADDRESS);

        initViews();
        setupSpinners();
        loadConfiguredServiceAreas();

        if (existingAddress != null) {
            populateExistingData();
        } else {
            prefillUserDetails();
        }

        setupListeners();
    }

    private void initViews() {
        spinnerTag = findViewById(R.id.spinnerTag);
        spinnerDeliveryArea = findViewById(R.id.spinnerDeliveryArea);
        etRecipient = findViewById(R.id.etAddrRecipient);
        etPhone = findViewById(R.id.etAddrPhone);
        etLine1 = findViewById(R.id.etAddrLine1);
        etLandmark = findViewById(R.id.etAddrLandmark);
        etPinCode = findViewById(R.id.etAddrPinCode);
        cbSetDefault = findViewById(R.id.cbSetDefault);
        btnSave = findViewById(R.id.btnSaveAddress);

        if (existingAddress != null) {
            btnSave.setText("Update Address");
            setTitle("Edit Address");
        }
    }

    private void setupSpinners() {
        ArrayAdapter<String> tagAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, TAGS);
        spinnerTag.setAdapter(tagAdapter);

        areaAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, serviceAreaList);
        spinnerDeliveryArea.setAdapter(areaAdapter);
    }

    private void loadConfiguredServiceAreas() {
        ApiClient.getApiService().getServiceAreas().enqueue(new Callback<ApiResponse<List<String>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<String>>> call, Response<ApiResponse<List<String>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<String> areas = response.body().getData();
                    if (!areas.isEmpty()) {
                        serviceAreaList.clear();
                        serviceAreaList.addAll(areas);
                        areaAdapter.notifyDataSetChanged();
                        if (existingAddress != null && existingAddress.getAddressLine2() != null) {
                            selectAreaInSpinner(existingAddress.getAddressLine2());
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<String>>> call, Throwable t) {
            }
        });
    }

    private void selectAreaInSpinner(String areaName) {
        for (int i = 0; i < serviceAreaList.size(); i++) {
            if (serviceAreaList.get(i).equalsIgnoreCase(areaName) || serviceAreaList.get(i).contains(areaName)) {
                spinnerDeliveryArea.setSelection(i);
                break;
            }
        }
    }

    private void populateExistingData() {
        if (existingAddress.getTitle() != null) {
            for (int i = 0; i < TAGS.length; i++) {
                if (TAGS[i].equalsIgnoreCase(existingAddress.getTitle())) {
                    spinnerTag.setSelection(i);
                    break;
                }
            }
        }

        if (existingAddress.getRecipientName() != null) etRecipient.setText(existingAddress.getRecipientName());
        if (existingAddress.getPhoneNumber() != null) etPhone.setText(existingAddress.getPhoneNumber());
        if (existingAddress.getAddressLine1() != null) etLine1.setText(existingAddress.getAddressLine1());
        if (existingAddress.getLandmark() != null) etLandmark.setText(existingAddress.getLandmark());
        if (existingAddress.getPostalCode() != null) etPinCode.setText(existingAddress.getPostalCode());
        cbSetDefault.setChecked(existingAddress.isDefault());
    }

    private void prefillUserDetails() {
        String name = SessionManager.getInstance().getUserName();
        String phone = SessionManager.getInstance().getUserPhone();
        if (name != null) etRecipient.setText(name);
        if (phone != null) etPhone.setText(phone);
        etPinCode.setText("411005");
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveAddress());
    }

    private void saveAddress() {
        String tag = (String) spinnerTag.getSelectedItem();
        String recipient = etRecipient.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String line1 = etLine1.getText().toString().trim();
        String landmark = etLandmark.getText().toString().trim();
        String area = (String) spinnerDeliveryArea.getSelectedItem();
        String pinCode = etPinCode.getText().toString().trim();
        boolean isDefault = cbSetDefault.isChecked();

        if (recipient.isEmpty()) {
            etRecipient.setError("Recipient name is required");
            etRecipient.requestFocus();
            return;
        }

        if (phone.length() != 10) {
            etPhone.setError("Enter 10-digit phone number");
            etPhone.requestFocus();
            return;
        }

        if (line1.isEmpty()) {
            etLine1.setError("Address line is required");
            etLine1.requestFocus();
            return;
        }

        if (pinCode.length() != 6) {
            etPinCode.setError("Enter 6-digit PIN code");
            etPinCode.requestFocus();
            return;
        }

        btnSave.setEnabled(false);

        CustomerAddress address = new CustomerAddress(
                tag,
                recipient,
                phone,
                line1,
                area,
                "Pune",
                pinCode,
                isDefault
        );
        address.setLandmark(landmark);

        if (existingAddress != null && existingAddress.getId() != null) {
            // Edit existing address (PUT)
            address.setId(existingAddress.getId());
            ApiClient.getApiService().updateCustomerAddress(existingAddress.getId(), address)
                    .enqueue(new Callback<ApiResponse<CustomerAddress>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<CustomerAddress>> call, Response<ApiResponse<CustomerAddress>> response) {
                            btnSave.setEnabled(true);
                            if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                                CustomerAddress updated = response.body().getData();
                                SessionManager.getInstance().setSelectedAddress(updated);
                                Toast.makeText(AddAddressActivity.this, "Address updated successfully!", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(AddAddressActivity.this, "Update failed: " + (response.body() != null ? response.body().getMessage() : "HTTP " + response.code()), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<CustomerAddress>> call, Throwable t) {
                            btnSave.setEnabled(true);
                            Toast.makeText(AddAddressActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // Add new address (POST)
            ApiClient.getApiService().addCustomerAddress(address).enqueue(new Callback<ApiResponse<CustomerAddress>>() {
                @Override
                public void onResponse(Call<ApiResponse<CustomerAddress>> call, Response<ApiResponse<CustomerAddress>> response) {
                    btnSave.setEnabled(true);
                    if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                        CustomerAddress saved = response.body().getData();
                        SessionManager.getInstance().setSelectedAddress(saved);
                        Toast.makeText(AddAddressActivity.this, "Address added successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        address.setId(System.currentTimeMillis());
                        SessionManager.getInstance().setSelectedAddress(address);
                        Toast.makeText(AddAddressActivity.this, "Address saved", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<CustomerAddress>> call, Throwable t) {
                    btnSave.setEnabled(true);
                    address.setId(System.currentTimeMillis());
                    SessionManager.getInstance().setSelectedAddress(address);
                    Toast.makeText(AddAddressActivity.this, "Address saved locally", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }
    }
}
