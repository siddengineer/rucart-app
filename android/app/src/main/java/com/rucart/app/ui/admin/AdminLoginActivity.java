package com.rucart.app.ui.admin;

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

public class AdminLoginActivity extends AppCompatActivity {

    private EditText etAdminPhone;
    private EditText etAdminPassword;
    private AppCompatButton btnAdminLogin;
    private ProgressBar pbAdminLogin;
    private TextView tvBackToRoles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        initViews();
        setupListeners();
    }

    private void initViews() {
        etAdminPhone = findViewById(R.id.etAdminPhone);
        etAdminPassword = findViewById(R.id.etAdminPassword);
        btnAdminLogin = findViewById(R.id.btnAdminLogin);
        pbAdminLogin = findViewById(R.id.pbAdminLogin);
        tvBackToRoles = findViewById(R.id.tvBackToRoles);
    }

    private void setupListeners() {

        btnAdminLogin.setOnClickListener(v -> performAdminLogin());

        tvBackToRoles.setOnClickListener(v -> {
            startActivity(new Intent(this, RoleSelectionActivity.class));
            finish();
        });
    }

    private void performAdminLogin() {
        String phone = etAdminPhone.getText().toString().trim();
        String password = etAdminPassword.getText().toString().trim();

        if (phone.isEmpty()) {
            etAdminPhone.setError("Please enter admin mobile or username");
            etAdminPhone.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etAdminPassword.setError("Please enter password");
            etAdminPassword.requestFocus();
            return;
        }

        btnAdminLogin.setEnabled(false);
        pbAdminLogin.setVisibility(View.VISIBLE);

        LoginRequest request = new LoginRequest(phone, password);
        ApiClient.getApiService().login(request).enqueue(new Callback<ApiResponse<AuthResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AuthResponse>> call, Response<ApiResponse<AuthResponse>> response) {
                btnAdminLogin.setEnabled(true);
                pbAdminLogin.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    AuthResponse auth = response.body().getData();

                    // Part 1: Non-admin denied handling
                    if (auth.getRole() != Role.ADMIN) {
                        new AlertDialog.Builder(AdminLoginActivity.this)
                                .setTitle("Access Denied")
                                .setMessage("Role '" + auth.getRole() + "' is not authorized to access the Administrator Console. Administrator privileges are strictly required.")
                                .setPositiveButton("OK", null)
                                .show();
                        return;
                    }

                    // Authorized as ADMIN
                    SessionManager.getInstance().saveUserSession(
                            auth.getUserId(),
                            auth.getName(),
                            auth.getPhone(),
                            "ADMIN",
                            auth.getToken()
                    );

                    Toast.makeText(AdminLoginActivity.this, "Administrator Access Verified", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AdminLoginActivity.this, AdminMainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    String msg = "Invalid phone or password";
                    if (response.body() != null && response.body().getMessage() != null) {
                        msg = response.body().getMessage();
                    }
                    new AlertDialog.Builder(AdminLoginActivity.this)
                            .setTitle("Authentication Error")
                            .setMessage(msg)
                            .setPositiveButton("Try Again", null)
                            .show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AuthResponse>> call, Throwable t) {
                btnAdminLogin.setEnabled(true);
                pbAdminLogin.setVisibility(View.GONE);
                Toast.makeText(AdminLoginActivity.this, "Connection failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
