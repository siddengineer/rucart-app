package com.rucart.app.ui.splash;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.rucart.app.R;
import com.rucart.app.ui.admin.AdminMainActivity;
import com.rucart.app.ui.auth.RoleSelectionActivity;
import com.rucart.app.ui.customer.CustomerMainActivity;
import com.rucart.app.ui.delivery.DeliveryMainActivity;
import com.rucart.app.ui.store.StoreMainActivity;
import com.rucart.app.util.SessionManager;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            SessionManager session = SessionManager.getInstance();
            Intent intent;
            if (session != null && session.isLoggedIn()) {
                String role = session.getUserRole();
                if ("STORE_OWNER".equals(role)) {
                    intent = new Intent(SplashActivity.this, StoreMainActivity.class);
                } else if ("DELIVERY_PARTNER".equals(role)) {
                    intent = new Intent(SplashActivity.this, DeliveryMainActivity.class);
                } else if ("ADMIN".equals(role)) {
                    intent = new Intent(SplashActivity.this, AdminMainActivity.class);
                } else {
                    intent = new Intent(SplashActivity.this, CustomerMainActivity.class);
                }
            } else {
                // Open Customer Grocery Shopping Experience directly as default landing screen
                intent = new Intent(SplashActivity.this, CustomerMainActivity.class);
            }
            startActivity(intent);
            finish();
        }, 1200);
    }
}
