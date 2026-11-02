package com.rucart.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;
import com.rucart.app.R;
import com.rucart.app.ui.admin.AdminLoginActivity;
import com.rucart.app.ui.admin.AdminMainActivity;
import com.rucart.app.ui.customer.CustomerMainActivity;
import com.rucart.app.ui.delivery.DeliveryLoginActivity;
import com.rucart.app.ui.delivery.DeliveryMainActivity;
import com.rucart.app.ui.store.StoreLoginActivity;
import com.rucart.app.ui.store.StoreMainActivity;
import com.rucart.app.util.SessionManager;

public class RoleSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);

        MaterialCardView cardCustomer = findViewById(R.id.cardCustomer);
        MaterialCardView cardStoreOwner = findViewById(R.id.cardStoreOwner);
        MaterialCardView cardDeliveryPartner = findViewById(R.id.cardDeliveryPartner);
        MaterialCardView cardAdmin = findViewById(R.id.cardAdmin);

        // Customer explores immediately (guest mode) or authenticated
        cardCustomer.setOnClickListener(v -> {
            startActivity(new Intent(this, CustomerMainActivity.class));
        });

        // Store Owner: requires authorized login
        cardStoreOwner.setOnClickListener(v -> {
            SessionManager session = SessionManager.getInstance();
            if (session.isLoggedIn() && ("STORE_OWNER".equals(session.getUserRole()) || "ADMIN".equals(session.getUserRole()))) {
                startActivity(new Intent(this, StoreMainActivity.class));
            } else {
                startActivity(new Intent(this, StoreLoginActivity.class));
            }
        });

        // Delivery Partner: requires authorized login
        cardDeliveryPartner.setOnClickListener(v -> {
            SessionManager session = SessionManager.getInstance();
            if (session.isLoggedIn() && ("DELIVERY_PARTNER".equals(session.getUserRole()) || "ADMIN".equals(session.getUserRole()))) {
                startActivity(new Intent(this, DeliveryMainActivity.class));
            } else {
                startActivity(new Intent(this, DeliveryLoginActivity.class));
            }
        });

        // Administrator: requires authorized login
        cardAdmin.setOnClickListener(v -> {
            SessionManager session = SessionManager.getInstance();
            if (session.isLoggedIn() && "ADMIN".equals(session.getUserRole())) {
                startActivity(new Intent(this, AdminMainActivity.class));
            } else {
                startActivity(new Intent(this, AdminLoginActivity.class));
            }
        });
    }
}
