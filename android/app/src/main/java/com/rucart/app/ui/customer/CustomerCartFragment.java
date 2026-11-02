package com.rucart.app.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.rucart.app.R;
import com.rucart.app.model.CartItem;
import com.rucart.app.model.CustomerAddress;
import com.rucart.app.ui.auth.LoginActivity;
import com.rucart.app.ui.customer.adapter.CartAdapter;
import com.rucart.app.util.AuthPromptDialog;
import com.rucart.app.util.CartManager;
import com.rucart.app.util.SessionManager;

import java.math.BigDecimal;
import java.util.List;

public class CustomerCartFragment extends Fragment {

    private RecyclerView rvCartItems;
    private CartAdapter cartAdapter;
    private LinearLayout layoutEmptyCart, layoutGuestCart, layoutCheckoutFooter;
    private MaterialCardView cardAddress, cardBill;
    private TextView tvCartAddress, btnCartChangeAddress;
    private TextView tvItemTotal, tvDeliveryFee, tvGrandTotal;
    private Button btnCheckout, btnExploreGroceries, btnCartLogin;

    private final BigDecimal DELIVERY_FEE = new BigDecimal("20.00");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customer_cart, container, false);

        initViews(view);
        setupListeners();
        setupRecyclerView();

        refreshUI();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshUI();
    }

    private void initViews(View view) {
        rvCartItems = view.findViewById(R.id.rvCartItems);
        layoutEmptyCart = view.findViewById(R.id.layoutEmptyCart);
        layoutGuestCart = view.findViewById(R.id.layoutGuestCart);
        layoutCheckoutFooter = view.findViewById(R.id.layoutCheckoutFooter);
        cardAddress = view.findViewById(R.id.cardAddress);
        cardBill = view.findViewById(R.id.cardBill);
        tvCartAddress = view.findViewById(R.id.tvCartAddress);
        btnCartChangeAddress = view.findViewById(R.id.btnCartChangeAddress);
        tvItemTotal = view.findViewById(R.id.tvItemTotal);
        tvDeliveryFee = view.findViewById(R.id.tvDeliveryFee);
        tvGrandTotal = view.findViewById(R.id.tvGrandTotal);
        btnCheckout = view.findViewById(R.id.btnCheckout);
        btnExploreGroceries = view.findViewById(R.id.btnExploreGroceries);
        btnCartLogin = view.findViewById(R.id.btnCartLogin);
    }

    private void setupRecyclerView() {
        rvCartItems.setLayoutManager(new LinearLayoutManager(getContext()));
        cartAdapter = new CartAdapter(new CartAdapter.OnCartItemActionListener() {
            @Override
            public void onIncrement(CartItem item) {
                CartManager.getInstance().addProduct(item.getProduct());
                refreshUI();
            }

            @Override
            public void onDecrement(CartItem item) {
                CartManager.getInstance().decrementProduct(item.getProduct());
                refreshUI();
            }
        });
        rvCartItems.setAdapter(cartAdapter);
    }

    private void setupListeners() {
        btnCheckout.setOnClickListener(v -> {
            if (!SessionManager.getInstance().isLoggedIn()) {
                AuthPromptDialog.showLoginRequired(getContext(), "proceed to checkout");
                return;
            }
            Intent intent = new Intent(getContext(), CheckoutActivity.class);
            startActivity(intent);
        });

        if (btnCartLogin != null) {
            btnCartLogin.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), LoginActivity.class);
                intent.putExtra(LoginActivity.EXTRA_RETURN_TO_CALLER, true);
                startActivity(intent);
            });
        }

        btnCartChangeAddress.setOnClickListener(v -> {
            if (!SessionManager.getInstance().isLoggedIn()) {
                AuthPromptDialog.showLoginRequired(getContext(), "manage delivery addresses");
                return;
            }
            Intent intent = new Intent(getContext(), AddressManagementActivity.class);
            startActivity(intent);
        });

        btnExploreGroceries.setOnClickListener(v -> {
            if (getActivity() instanceof CustomerMainActivity) {
                ((CustomerMainActivity) getActivity()).navigateToTab(R.id.nav_customer_categories);
            }
        });
    }

    private void refreshUI() {
        if (!SessionManager.getInstance().isLoggedIn()) {
            if (layoutGuestCart != null) layoutGuestCart.setVisibility(View.VISIBLE);
            layoutEmptyCart.setVisibility(View.GONE);
            rvCartItems.setVisibility(View.GONE);
            cardAddress.setVisibility(View.GONE);
            cardBill.setVisibility(View.GONE);
            layoutCheckoutFooter.setVisibility(View.GONE);
            return;
        }

        if (layoutGuestCart != null) layoutGuestCart.setVisibility(View.GONE);

        List<CartItem> items = CartManager.getInstance().getCartItems();
        if (items.isEmpty()) {
            layoutEmptyCart.setVisibility(View.VISIBLE);
            rvCartItems.setVisibility(View.GONE);
            cardAddress.setVisibility(View.GONE);
            cardBill.setVisibility(View.GONE);
            layoutCheckoutFooter.setVisibility(View.GONE);
        } else {
            layoutEmptyCart.setVisibility(View.GONE);
            rvCartItems.setVisibility(View.VISIBLE);
            cardAddress.setVisibility(View.VISIBLE);
            cardBill.setVisibility(View.VISIBLE);
            layoutCheckoutFooter.setVisibility(View.VISIBLE);

            cartAdapter.setCartItems(items);

            CustomerAddress address = SessionManager.getInstance().getSelectedCustomerAddress();
            if (address != null) {
                tvCartAddress.setText(address.getFormattedAddress());
            } else {
                tvCartAddress.setText("Select delivery address at checkout");
            }

            BigDecimal itemTotal = CartManager.getInstance().getCartTotal();
            BigDecimal grandTotal = itemTotal.add(DELIVERY_FEE);

            tvItemTotal.setText("₹" + itemTotal.toPlainString());
            tvDeliveryFee.setText("₹" + DELIVERY_FEE.toPlainString());
            tvGrandTotal.setText("₹" + grandTotal.toPlainString());
        }
    }
}
