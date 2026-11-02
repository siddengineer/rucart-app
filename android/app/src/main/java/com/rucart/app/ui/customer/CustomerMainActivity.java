package com.rucart.app.ui.customer;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.rucart.app.R;
import com.rucart.app.util.CartManager;

public class CustomerMainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private final CartManager.CartListener cartListener = (totalCount, grandTotal) -> updateCartBadge(totalCount);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_main);

        bottomNav = findViewById(R.id.bottomNav);

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_customer_home) {
                selectedFragment = new CustomerHomeFragment();
            } else if (itemId == R.id.nav_customer_categories) {
                selectedFragment = new CustomerCategoriesFragment();
            } else if (itemId == R.id.nav_customer_cart) {
                selectedFragment = new CustomerCartFragment();
            } else if (itemId == R.id.nav_customer_orders) {
                selectedFragment = new CustomerOrdersFragment();
            } else if (itemId == R.id.nav_customer_account) {
                selectedFragment = new CustomerAccountFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, selectedFragment)
                        .commit();
                return true;
            }
            return false;
        });

        // Load default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new CustomerHomeFragment())
                    .commit();
        }

        CartManager.getInstance().addListener(cartListener);
        updateCartBadge(CartManager.getInstance().getItemCount());
    }

    public void navigateToTab(int tabId) {
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(tabId);
        }
    }

    private void updateCartBadge(int count) {
        if (bottomNav == null) return;
        BadgeDrawable badge = bottomNav.getOrCreateBadge(R.id.nav_customer_cart);
        if (count > 0) {
            badge.setVisible(true);
            badge.setNumber(count);
            badge.setBackgroundColor(getColor(R.color.primary_green));
        } else {
            badge.setVisible(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CartManager.getInstance().removeListener(cartListener);
    }
}
