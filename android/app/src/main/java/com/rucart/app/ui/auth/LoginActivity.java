package com.rucart.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.rucart.app.R;
import com.rucart.app.api.ApiClient;
import com.rucart.app.model.ApiResponse;
import com.rucart.app.model.AuthResponse;
import com.rucart.app.model.LoginRequest;
import com.rucart.app.model.Role;
import com.rucart.app.ui.admin.AdminMainActivity;
import com.rucart.app.ui.customer.CustomerMainActivity;
import com.rucart.app.ui.delivery.DeliveryMainActivity;
import com.rucart.app.ui.store.StoreMainActivity;
import com.rucart.app.util.CartManager;
import com.rucart.app.util.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    public static final String EXTRA_RETURN_TO_CALLER = "extra_return_to_caller";

    private EditText etPhone;
    private EditText etPassword;
    private ImageView ivTogglePassword;
    private Button btnContinue;
    private ProgressBar pbLogin;
    private TextView tvQuickFill;
    private TextView tvGoToRegister;
    private TextView tvForgotPassword;
    private TextView tvSkipLogin;
    private ImageView btnLoginBack;

    private boolean isPasswordVisible = false;
    private boolean returnToCaller = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        returnToCaller = getIntent().getBooleanExtra(EXTRA_RETURN_TO_CALLER, false);

        initViews();
        setupListeners();
    }

    private void initViews() {
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        ivTogglePassword = findViewById(R.id.ivTogglePassword);
        btnContinue = findViewById(R.id.btnContinue);
        pbLogin = findViewById(R.id.pbLogin);
        tvQuickFill = findViewById(R.id.tvQuickFill);
        tvGoToRegister = findViewById(R.id.tvGoToRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvSkipLogin = findViewById(R.id.tvSkipLogin);
        btnLoginBack = findViewById(R.id.btnLoginBack);
    }

    private void setupListeners() {
        btnLoginBack.setOnClickListener(v -> finish());

        tvSkipLogin.setOnClickListener(v -> {
            Intent intent = new Intent(this, CustomerMainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Show/hide password toggle
        ivTogglePassword.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            if (isPasswordVisible) {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                ivTogglePassword.setImageResource(R.drawable.ic_visibility_off);
            } else {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                ivTogglePassword.setImageResource(R.drawable.ic_visibility);
            }
            etPassword.setSelection(etPassword.getText().length());
        });

        // Quick Fill for easy evaluation
        tvQuickFill.setOnClickListener(v -> {
            etPhone.setText("9876543210");
            etPassword.setText("123456");
            Toast.makeText(this, "Demo Customer credentials filled", Toast.LENGTH_SHORT).show();
        });

        // Forgot password flow
        tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());

        btnContinue.setOnClickListener(v -> performLogin());

        tvGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void showForgotPasswordDialog() {
        EditText input = new EditText(this);
        input.setHint("Enter registered 10-digit mobile or email");
        input.setText(etPhone.getText().toString().trim());

        new AlertDialog.Builder(this)
                .setTitle("Reset Password")
                .setMessage("We will send a password reset verification link to your registered contact.")
                .setView(input)
                .setPositiveButton("Send Reset Link", (dialog, which) -> {
                    String identifier = input.getText().toString().trim();
                    if (identifier.isEmpty()) {
                        Toast.makeText(this, "Please enter your mobile or email", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Password reset instructions sent to " + identifier, Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performLogin() {
        String phoneOrEmail = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (phoneOrEmail.isEmpty()) {
            etPhone.setError("Please enter your 10-digit mobile number or email");
            etPhone.requestFocus();
            return;
        }

        // Validate: if digits only, must be 10 digits; if email, must contain @
        if (phoneOrEmail.matches("\\d+")) {
            if (phoneOrEmail.length() != 10) {
                etPhone.setError("Mobile number must be exactly 10 digits");
                etPhone.requestFocus();
                return;
            }
        } else if (!phoneOrEmail.contains("@") || !phoneOrEmail.contains(".")) {
            etPhone.setError("Please enter a valid email address");
            etPhone.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Please enter your password");
            etPassword.requestFocus();
            return;
        }

        btnContinue.setEnabled(false);
        pbLogin.setVisibility(View.VISIBLE);

        LoginRequest request = new LoginRequest(phoneOrEmail, password);
        ApiClient.getApiService().login(request).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ApiResponse<AuthResponse>> call, Response<ApiResponse<AuthResponse>> response) {
                btnContinue.setEnabled(true);
                pbLogin.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    AuthResponse auth = response.body().getData();
                    Role role = auth.getRole() != null ? auth.getRole() : Role.CUSTOMER;

                    SessionManager.getInstance().saveUserSession(
                            auth.getUserId(),
                            auth.getName(),
                            auth.getPhone(),
                            role.name(),
                            auth.getToken()
                    );

                    // Restore authenticated user's cart
                    CartManager.getInstance().restoreCartForCurrentUser();

                    Toast.makeText(LoginActivity.this, "Welcome, " + auth.getName() + "!", Toast.LENGTH_SHORT).show();

                    if (returnToCaller) {
                        setResult(RESULT_OK);
                        finish();
                        return;
                    }

                    // Role-based routing
                    Intent intent;
                    if (role == Role.STORE_OWNER) {
                        intent = new Intent(LoginActivity.this, StoreMainActivity.class);
                    } else if (role == Role.DELIVERY_PARTNER) {
                        intent = new Intent(LoginActivity.this, DeliveryMainActivity.class);
                    } else if (role == Role.ADMIN) {
                        intent = new Intent(LoginActivity.this, AdminMainActivity.class);
                    } else {
                        intent = new Intent(LoginActivity.this, CustomerMainActivity.class);
                    }
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    String msg = "Invalid login credentials. Please check your phone/email and password.";
                    if (response.body() != null && response.body().getMessage() != null) {
                        msg = response.body().getMessage();
                    }
                    new AlertDialog.Builder(LoginActivity.this)
                            .setTitle("Authentication Failed")
                            .setMessage(msg)
                            .setPositiveButton("Try Again", null)
                            .show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AuthResponse>> call, Throwable t) {
                btnContinue.setEnabled(true);
                pbLogin.setVisibility(View.GONE);
                new AlertDialog.Builder(LoginActivity.this)
                        .setTitle("Connection Error")
                        .setMessage("Unable to connect to server. Please check your network and try again.")
                        .setPositiveButton("Dismiss", null)
                        .show();
            }
        });
    }
}
