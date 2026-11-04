package com.rucart.app.ui.delivery;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import com.rucart.app.R;
import com.rucart.app.api.ApiClient;
import com.rucart.app.model.ApiResponse;
import com.rucart.app.model.AuthResponse;
import com.rucart.app.model.LoginRequest;
import com.rucart.app.model.Role;
import com.rucart.app.ui.auth.RoleSelectionActivity;
import com.rucart.app.util.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeliveryLoginActivity extends AppCompatActivity {

    private EditText etDeliveryPhone;
    private EditText etDeliveryPassword;
    private AppCompatButton btnDeliveryLogin;
    private ProgressBar pbDeliveryLogin;
    private TextView tvBackToRoles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_login);

        initViews();
        setupListeners();
    }

    private void initViews() {
        etDeliveryPhone = findViewById(R.id.etDeliveryPhone);
        etDeliveryPassword = findViewById(R.id.etDeliveryPassword);
        btnDeliveryLogin = findViewById(R.id.btnDeliveryLogin);
        pbDeliveryLogin = findViewById(R.id.pbDeliveryLogin);
        tvBackToRoles = findViewById(R.id.tvBackToRoles);
    }

    private void setupListeners() {

        btnDeliveryLogin.setOnClickListener(v -> performDeliveryLogin());

        tvBackToRoles.setOnClickListener(v -> {
            startActivity(new Intent(this, RoleSelectionActivity.class));
            finish();
        });
    }

    private void performDeliveryLogin() {
        String phone = etDeliveryPhone.getText().toString().trim();
        String password = etDeliveryPassword.getText().toString().trim();

        if (phone.isEmpty()) {
            etDeliveryPhone.setError("Please enter registered phone number");
            etDeliveryPhone.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etDeliveryPassword.setError("Please enter password");
            etDeliveryPassword.requestFocus();
            return;
        }

        btnDeliveryLogin.setEnabled(false);
        pbDeliveryLogin.setVisibility(View.VISIBLE);

        LoginRequest request = new LoginRequest(phone, password);
        ApiClient.getApiService().login(request).enqueue(new Callback<ApiResponse<AuthResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AuthResponse>> call, Response<ApiResponse<AuthResponse>> response) {
                btnDeliveryLogin.setEnabled(true);
                pbDeliveryLogin.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    AuthResponse auth = response.body().getData();

                    // Reject users who do not have the delivery-partner role
                    if (auth.getRole() != Role.DELIVERY_PARTNER) {
                        new AlertDialog.Builder(DeliveryLoginActivity.this)
                                .setTitle("Access Denied")
                                .setMessage("Account role '" + auth.getRole() + "' is not authorized to access the Delivery Partner app. A registered DELIVERY_PARTNER account is required.")
                                .setPositiveButton("OK", null)
                                .show();
                        return;
                    }

                    // Save session with verified JWT token
                    SessionManager.getInstance().saveUserSession(
                            auth.getUserId(),
                            auth.getName(),
                            auth.getPhone(),
                            auth.getRole().name(),
                            auth.getToken()
                    );

                    Toast.makeText(DeliveryLoginActivity.this, "Welcome back, " + auth.getName() + "!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(DeliveryLoginActivity.this, DeliveryMainActivity.class));
                    finish();
                } else {
                    String errorMsg = "Invalid credentials. Please verify your phone and password.";
                    if (response.body() != null && response.body().getMessage() != null) {
                        errorMsg = response.body().getMessage();
                    }
                    new AlertDialog.Builder(DeliveryLoginActivity.this)
                            .setTitle("Login Failed")
                            .setMessage(errorMsg)
                            .setPositiveButton("Retry", null)
                            .show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AuthResponse>> call, Throwable t) {
                btnDeliveryLogin.setEnabled(true);
                pbDeliveryLogin.setVisibility(View.GONE);
                new AlertDialog.Builder(DeliveryLoginActivity.this)
                        .setTitle("Connection Error")
                        .setMessage("Could not connect to RUCART backend server. Please check your network connection.")
                        .setPositiveButton("Dismiss", null)
                        .show();
            }
        });
    }
}
