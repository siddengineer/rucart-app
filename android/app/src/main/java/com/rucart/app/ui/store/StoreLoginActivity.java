package com.rucart.app.ui.store;

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
import com.rucart.app.util.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StoreLoginActivity extends AppCompatActivity {

    private EditText etStorePhone;
    private EditText etStorePassword;
    private ImageView ivStoreTogglePassword;
    private Button btnStoreLogin;
    private ProgressBar pbStoreLogin;
    private TextView tvStoreQuickFill;
    private ImageView btnStoreLoginBack;

    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_login);

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnStoreLoginBack = findViewById(R.id.btnStoreLoginBack);
        etStorePhone = findViewById(R.id.etStorePhone);
        etStorePassword = findViewById(R.id.etStorePassword);
        ivStoreTogglePassword = findViewById(R.id.ivStoreTogglePassword);
        btnStoreLogin = findViewById(R.id.btnStoreLogin);
        pbStoreLogin = findViewById(R.id.pbStoreLogin);
        tvStoreQuickFill = findViewById(R.id.tvStoreQuickFill);
    }

    private void setupListeners() {
        btnStoreLoginBack.setOnClickListener(v -> finish());

        ivStoreTogglePassword.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            if (isPasswordVisible) {
                etStorePassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                ivStoreTogglePassword.setImageResource(R.drawable.ic_visibility_off);
            } else {
                etStorePassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                ivStoreTogglePassword.setImageResource(R.drawable.ic_visibility);
            }
            etStorePassword.setSelection(etStorePassword.getText().length());
        });

        tvStoreQuickFill.setOnClickListener(v -> {
            etStorePhone.setText("9876543211");
            etStorePassword.setText("123456");
            Toast.makeText(this, "Store Owner demo credentials filled", Toast.LENGTH_SHORT).show();
        });

        btnStoreLogin.setOnClickListener(v -> performStoreLogin());
    }

    private void performStoreLogin() {
        String phoneOrUser = etStorePhone.getText().toString().trim();
        String password = etStorePassword.getText().toString().trim();

        if (phoneOrUser.isEmpty()) {
            etStorePhone.setError("Please enter phone or username");
            etStorePhone.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etStorePassword.setError("Please enter password");
            etStorePassword.requestFocus();
            return;
        }

        btnStoreLogin.setEnabled(false);
        pbStoreLogin.setVisibility(View.VISIBLE);

        LoginRequest request = new LoginRequest(phoneOrUser, password);
        ApiClient.getApiService().login(request).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ApiResponse<AuthResponse>> call, Response<ApiResponse<AuthResponse>> response) {
                btnStoreLogin.setEnabled(true);
                pbStoreLogin.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    AuthResponse auth = response.body().getData();

                    if (auth.getRole() != Role.STORE_OWNER && auth.getRole() != Role.ADMIN) {
                        new AlertDialog.Builder(StoreLoginActivity.this)
                                .setTitle("Access Denied")
                                .setMessage("Role '" + auth.getRole() + "' is not authorized as a Store Owner. A registered STORE_OWNER account is required.")
                                .setPositiveButton("OK", null)
                                .show();
                        return;
                    }

                    SessionManager.getInstance().saveUserSession(
                            auth.getUserId(),
                            auth.getName(),
                            auth.getPhone(),
                            auth.getRole().name(),
                            auth.getToken()
                    );

                    Toast.makeText(StoreLoginActivity.this, "Welcome, " + auth.getName() + "!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(StoreLoginActivity.this, StoreMainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    String msg = "Invalid Store Owner credentials. Please verify your phone/password.";
                    if (response.body() != null && response.body().getMessage() != null) {
                        msg = response.body().getMessage();
                    }
                    new AlertDialog.Builder(StoreLoginActivity.this)
                            .setTitle("Authentication Error")
                            .setMessage(msg)
                            .setPositiveButton("Try Again", null)
                            .show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AuthResponse>> call, Throwable t) {
                btnStoreLogin.setEnabled(true);
                pbStoreLogin.setVisibility(View.GONE);
                new AlertDialog.Builder(StoreLoginActivity.this)
                        .setTitle("Connection Error")
                        .setMessage("Could not connect to server: " + t.getMessage())
                        .setPositiveButton("Dismiss", null)
                        .show();
            }
        });
    }
}
