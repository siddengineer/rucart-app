package com.rucart.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.rucart.app.R;
import com.rucart.app.api.ApiClient;
import com.rucart.app.model.ApiResponse;
import com.rucart.app.model.AuthResponse;
import com.rucart.app.model.CustomerAddress;
import com.rucart.app.model.RegisterRequest;
import com.rucart.app.model.Role;
import com.rucart.app.ui.customer.CustomerMainActivity;
import com.rucart.app.util.CartManager;
import com.rucart.app.util.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etPhone, etEmail, etPassword, etConfirmPassword, etAddress;
    private ImageView ivRegTogglePassword, ivRegToggleConfirmPassword, btnRegisterBack;
    private Spinner spinnerArea;
    private Button btnRegister;
    private ProgressBar pbRegister;
    private TextView tvBackToLogin;

    private boolean isPassVisible = false;
    private boolean isConfirmPassVisible = false;

    private static final String[] PUNE_AREAS = new String[]{
            "Shivajinagar, Pune",
            "Kothrud, Pune",
            "Viman Nagar, Pune",
            "Hinjewadi, Pune",
            "Baner, Pune",
            "Wakad, Pune",
            "Aundh, Pune",
            "Hadapsar, Pune"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        setupAreaSpinner();
        setupListeners();
    }

    private void initViews() {
        btnRegisterBack = findViewById(R.id.btnRegisterBack);
        etName = findViewById(R.id.etRegName);
        etPhone = findViewById(R.id.etRegPhone);
        etEmail = findViewById(R.id.etRegEmail);
        etPassword = findViewById(R.id.etRegPassword);
        etConfirmPassword = findViewById(R.id.etRegConfirmPassword);
        etAddress = findViewById(R.id.etRegAddress);
        ivRegTogglePassword = findViewById(R.id.ivRegTogglePassword);
        ivRegToggleConfirmPassword = findViewById(R.id.ivRegToggleConfirmPassword);
        spinnerArea = findViewById(R.id.spinnerArea);
        btnRegister = findViewById(R.id.btnRegisterSubmit);
        pbRegister = findViewById(R.id.pbRegister);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);
    }

    private void setupAreaSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                PUNE_AREAS
        );
        spinnerArea.setAdapter(adapter);
    }

    private void setupListeners() {
        btnRegisterBack.setOnClickListener(v -> finish());
        tvBackToLogin.setOnClickListener(v -> finish());

        ivRegTogglePassword.setOnClickListener(v -> {
            isPassVisible = !isPassVisible;
            if (isPassVisible) {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                ivRegTogglePassword.setImageResource(R.drawable.ic_visibility_off);
            } else {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                ivRegTogglePassword.setImageResource(R.drawable.ic_visibility);
            }
            etPassword.setSelection(etPassword.getText().length());
        });

        ivRegToggleConfirmPassword.setOnClickListener(v -> {
            isConfirmPassVisible = !isConfirmPassVisible;
            if (isConfirmPassVisible) {
                etConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                ivRegToggleConfirmPassword.setImageResource(R.drawable.ic_visibility_off);
            } else {
                etConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                ivRegToggleConfirmPassword.setImageResource(R.drawable.ic_visibility);
            }
            etConfirmPassword.setSelection(etConfirmPassword.getText().length());
        });

        btnRegister.setOnClickListener(v -> performRegister());

        android.view.View btnRegisterWithOtp = findViewById(R.id.btnRegisterWithOtp);
        if (btnRegisterWithOtp != null) {
            btnRegisterWithOtp.setOnClickListener(v -> {
                String phone = etPhone.getText().toString().trim();
                android.content.Intent intent = new android.content.Intent(RegisterActivity.this, Msg91OtpActivity.class);
                if (!phone.isEmpty()) {
                    intent.putExtra(Msg91OtpActivity.EXTRA_PHONE, phone);
                }
                startActivity(intent);
            });
        }
    }

    private void performRegister() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String addressLine = etAddress.getText().toString().trim();
        String selectedArea = (String) spinnerArea.getSelectedItem();

        if (name.isEmpty()) {
            etName.setError("Full name is required");
            etName.requestFocus();
            return;
        }

        if (phone.length() != 10) {
            etPhone.setError("Enter a valid 10-digit mobile number");
            etPhone.requestFocus();
            return;
        }

        if (!email.isEmpty() && (!email.contains("@") || !email.contains("."))) {
            etEmail.setError("Enter a valid email address");
            etEmail.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        if (addressLine.isEmpty()) {
            etAddress.setError("Delivery address is required");
            etAddress.requestFocus();
            return;
        }

        String fullAddress = addressLine + ", " + selectedArea;

        btnRegister.setEnabled(false);
        pbRegister.setVisibility(View.VISIBLE);

        RegisterRequest request = new RegisterRequest(name, phone, email.isEmpty() ? null : email, password, Role.CUSTOMER, fullAddress);

        ApiClient.getApiService().register(request).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<AuthResponse>> call, @NonNull Response<ApiResponse<AuthResponse>> response) {
                btnRegister.setEnabled(true);
                pbRegister.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    AuthResponse auth = response.body().getData();
                    SessionManager.getInstance().saveUserSession(
                            auth.getUserId(),
                            auth.getName(),
                            auth.getPhone(),
                            auth.getRole() != null ? auth.getRole().name() : "CUSTOMER",
                            auth.getToken()
                    );

                    CustomerAddress initialAddr = new CustomerAddress(
                            "Home",
                            auth.getName(),
                            auth.getPhone(),
                            addressLine,
                            selectedArea,
                            "Pune",
                            "411001",
                            true
                    );
                    SessionManager.getInstance().setSelectedAddress(initialAddr);
                    CartManager.getInstance().restoreCartForCurrentUser();

                    Toast.makeText(RegisterActivity.this, "Welcome to RUCART, " + auth.getName() + "!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterActivity.this, CustomerMainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    String msg = "Registration failed. Mobile might already be registered.";
                    if (response.body() != null && response.body().getMessage() != null) {
                        msg = response.body().getMessage();
                    }
                    new AlertDialog.Builder(RegisterActivity.this)
                            .setTitle("Registration Error")
                            .setMessage(msg)
                            .setPositiveButton("OK", null)
                            .show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<AuthResponse>> call, @NonNull Throwable t) {
                btnRegister.setEnabled(true);
                pbRegister.setVisibility(View.GONE);
                new AlertDialog.Builder(RegisterActivity.this)
                        .setTitle("Connection Error")
                        .setMessage("Could not connect to server: " + t.getMessage())
                        .setPositiveButton("Dismiss", null)
                        .show();
            }
        });
    }
}
