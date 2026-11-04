package com.rucart.app.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.rucart.app.R;
import com.rucart.app.api.ApiClient;
import com.rucart.app.model.*;
import com.rucart.app.util.CartManager;
import com.rucart.app.util.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CheckoutActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvAddressTitle, tvFullAddress, btnChangeAddress;
    private TextView tvCheckoutStoreName, tvCheckoutStoreArea, tvCheckoutStoreStatus, btnChangeStoreCheckout;
    private EditText etInstructions;
    private RadioGroup rgPaymentMethod;
    private TextView tvItemTotal, tvDeliveryFee, tvGrandTotal;
    private Button btnPlaceOrder;
    private ProgressBar pbCheckout;

    private final BigDecimal DELIVERY_FEE = new BigDecimal("20.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        if (CartManager.getInstance().getCartItems().isEmpty()) {
            Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupListeners();
        updateTotals();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateAddressDisplay();
        updateStoreDisplay();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBackCheckout);
        tvAddressTitle = findViewById(R.id.tvCheckoutAddressTitle);
        tvFullAddress = findViewById(R.id.tvCheckoutFullAddress);
        btnChangeAddress = findViewById(R.id.btnChangeAddress);

        tvCheckoutStoreName = findViewById(R.id.tvCheckoutStoreName);
        tvCheckoutStoreArea = findViewById(R.id.tvCheckoutStoreArea);
        tvCheckoutStoreStatus = findViewById(R.id.tvCheckoutStoreStatus);
        btnChangeStoreCheckout = findViewById(R.id.btnChangeStoreCheckout);

        etInstructions = findViewById(R.id.etInstructions);
        rgPaymentMethod = findViewById(R.id.rgPaymentMethod);
        tvItemTotal = findViewById(R.id.tvCheckoutItemTotal);
        tvDeliveryFee = findViewById(R.id.tvCheckoutDeliveryFee);
        tvGrandTotal = findViewById(R.id.tvCheckoutGrandTotal);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrderFinal);
        pbCheckout = findViewById(R.id.pbCheckout);
    }

    private void updateAddressDisplay() {
        String address = SessionManager.getInstance().getSelectedAddress();
        if (address != null && !address.trim().isEmpty()) {
            tvAddressTitle.setText("Deliver to Selected Address");
            tvFullAddress.setText(address);
        } else {
            tvAddressTitle.setText("Delivery Address");
            tvFullAddress.setText("No delivery address selected. Tap 'CHANGE' to choose or add an address.");
        }
    }

    private void updateStoreDisplay() {
        String storeName = SessionManager.getInstance().getSelectedStoreName();
        String serviceArea = SessionManager.getInstance().getSelectedServiceArea();
        tvCheckoutStoreName.setText(storeName != null ? storeName : "RUCART Express Store");
        tvCheckoutStoreArea.setText("Service Area: " + (serviceArea != null ? serviceArea : "Shivajinagar, Pune"));
        tvCheckoutStoreStatus.setText("🟢 STORE ONLINE • 07:00 AM - 11:00 PM");
    }

    private void updateTotals() {
        BigDecimal itemTotal = CartManager.getInstance().getCartTotal();
        BigDecimal grandTotal = itemTotal.add(DELIVERY_FEE);

        tvItemTotal.setText("₹" + itemTotal.toPlainString());
        tvDeliveryFee.setText("₹" + DELIVERY_FEE.toPlainString());
        tvGrandTotal.setText("₹" + grandTotal.toPlainString());

        updateOrderButtonText(grandTotal);
    }

    private void updateOrderButtonText(BigDecimal grandTotal) {
        if (rgPaymentMethod == null || btnPlaceOrder == null) return;
        int checkedId = rgPaymentMethod.getCheckedRadioButtonId();
        if (checkedId == R.id.rbCOD) {
            btnPlaceOrder.setText("PLACE CASH ON DELIVERY ORDER (₹" + grandTotal.toPlainString() + ")");
        } else {
            btnPlaceOrder.setText("PROCEED TO PAY ₹" + grandTotal.toPlainString() + "");
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        rgPaymentMethod.setOnCheckedChangeListener((group, checkedId) -> {
            BigDecimal itemTotal = CartManager.getInstance().getCartTotal();
            BigDecimal grandTotal = itemTotal.add(DELIVERY_FEE);
            updateOrderButtonText(grandTotal);
        });

        btnChangeAddress.setOnClickListener(v -> {
            startActivity(new Intent(CheckoutActivity.this, AddressManagementActivity.class));
        });

        btnChangeStoreCheckout.setOnClickListener(v -> {
            startActivity(new Intent(CheckoutActivity.this, StoreSelectionActivity.class));
        });

        btnPlaceOrder.setOnClickListener(v -> performOrderPlacement());
    }

    private void performOrderPlacement() {
        List<CartItem> cartItems = CartManager.getInstance().getCartItems();
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnPlaceOrder.setEnabled(false);
        pbCheckout.setVisibility(View.VISIBLE);

        String paymentMethod = "ONLINE";
        int checkedId = rgPaymentMethod.getCheckedRadioButtonId();
        if (checkedId == R.id.rbCOD) {
            paymentMethod = "CASH_ON_DELIVERY";
        }

        String fullAddressText = SessionManager.getInstance().getSelectedAddress();
        if (fullAddressText == null || fullAddressText.trim().isEmpty()) {
            btnPlaceOrder.setEnabled(true);
            pbCheckout.setVisibility(View.GONE);
            Toast.makeText(this, "Please select or add a delivery address first", Toast.LENGTH_LONG).show();
            startActivity(new Intent(CheckoutActivity.this, AddressManagementActivity.class));
            return;
        }
        String instructions = etInstructions.getText().toString().trim();
        if (!instructions.isEmpty()) {
            fullAddressText += " (Note: " + instructions + ")";
        }

        Long selectedStoreId = SessionManager.getInstance().getSelectedStoreId();
        String selectedServiceArea = SessionManager.getInstance().getSelectedServiceArea();

        OrderCreateRequest req = new OrderCreateRequest();
        req.setCustomerId(SessionManager.getInstance().getUserId());
        req.setStoreId(selectedStoreId);
        req.setDeliveryArea(selectedServiceArea);
        req.setDeliveryAddress(fullAddressText);
        req.setCustomerAddressId(SessionManager.getInstance().getSelectedAddressId());
        req.setPaymentMethod(paymentMethod);
        req.setIdempotencyKey(UUID.randomUUID().toString());

        List<OrderItemRequest> itemRequests = new ArrayList<>();
        for (CartItem ci : cartItems) {
            itemRequests.add(new OrderItemRequest(ci.getProduct().getId(), ci.getQuantity()));
        }
        req.setItems(itemRequests);

        // Pre-validate inventory before submitting order
        InventoryValidationRequest valReq = new InventoryValidationRequest(req.getStoreId(), itemRequests);
        ApiClient.getApiService().validateInventory(valReq).enqueue(new Callback<ApiResponse<InventoryValidationResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<InventoryValidationResponse>> call, Response<ApiResponse<InventoryValidationResponse>> valResp) {
                if (valResp.isSuccessful() && valResp.body() != null && valResp.body().getData() != null) {
                    InventoryValidationResponse validation = valResp.body().getData();
                    if (!validation.isValid()) {
                        btnPlaceOrder.setEnabled(true);
                        pbCheckout.setVisibility(View.GONE);

                        StringBuilder err = new StringBuilder();
                        err.append("Some items in your cart cannot be fulfilled by this store:\n\n");
                        if (validation.getItems() != null) {
                            for (InventoryValidationResponse.ItemValidation iv : validation.getItems()) {
                                if (Boolean.FALSE.equals(iv.getAvailable())) {
                                    err.append("• ").append(iv.getProductName()).append(": ").append(iv.getStatusMessage()).append("\n");
                                }
                            }
                        }
                        err.append("\nPlease adjust your quantities or choose another store.");

                        new AlertDialog.Builder(CheckoutActivity.this)
                                .setTitle("Inventory Alert")
                                .setMessage(err.toString())
                                .setPositiveButton("Edit Cart", (dialog, which) -> finish())
                                .setNegativeButton("Change Store", (dialog, which) -> {
                                    startActivity(new Intent(CheckoutActivity.this, StoreSelectionActivity.class));
                                })
                                .show();
                        return;
                    }
                }

                // Inventory validated -> proceed with atomic server-priced order creation
                sendCreateOrderRequest(req);
            }

            @Override
            public void onFailure(Call<ApiResponse<InventoryValidationResponse>> call, Throwable t) {
                // Network issue on pre-validation -> proceed to order submission where server validates
                sendCreateOrderRequest(req);
            }
        });
    }

    private void sendCreateOrderRequest(OrderCreateRequest req) {
        ApiClient.getApiService().createOrder(req).enqueue(new Callback<ApiResponse<Order>>() {
            @Override
            public void onResponse(Call<ApiResponse<Order>> call, Response<ApiResponse<Order>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    Order order = response.body().getData();
                    CartManager.getInstance().clearCart();

                    boolean isOnline = !"CASH_ON_DELIVERY".equalsIgnoreCase(order.getPaymentMethod());

                    if (isOnline) {
                        // Initiate payment session with backend gateway provider
                        Map<String, Object> body = new HashMap<>();
                        body.put("orderId", order.getId());

                        ApiClient.getApiService().initiateOnlinePayment(body).enqueue(new Callback<ApiResponse<PaymentOrderResult>>() {
                            @Override
                            public void onResponse(Call<ApiResponse<PaymentOrderResult>> pCall, Response<ApiResponse<PaymentOrderResult>> pResp) {
                                btnPlaceOrder.setEnabled(true);
                                pbCheckout.setVisibility(View.GONE);

                                if (pResp.isSuccessful() && pResp.body() != null && pResp.body().getData() != null) {
                                    PaymentOrderResult paymentOrder = pResp.body().getData();
                                    Intent intent = new Intent(CheckoutActivity.this, PaymentProcessingActivity.class);
                                    intent.putExtra(PaymentProcessingActivity.EXTRA_ORDER, order);
                                    intent.putExtra(PaymentProcessingActivity.EXTRA_PAYMENT_ORDER, paymentOrder);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // If initiate failed, send to result activity as failed
                                    Intent failIntent = new Intent(CheckoutActivity.this, PaymentResultActivity.class);
                                    failIntent.putExtra(PaymentResultActivity.EXTRA_STATUS, PaymentResultActivity.STATUS_FAILED);
                                    failIntent.putExtra(PaymentResultActivity.EXTRA_ORDER, order);
                                    failIntent.putExtra(PaymentResultActivity.EXTRA_ERROR_MESSAGE, "Failed to start payment gateway session. Please try again.");
                                    startActivity(failIntent);
                                    finish();
                                }
                            }

                            @Override
                            public void onFailure(Call<ApiResponse<PaymentOrderResult>> pCall, Throwable t) {
                                btnPlaceOrder.setEnabled(true);
                                pbCheckout.setVisibility(View.GONE);
                                Intent failIntent = new Intent(CheckoutActivity.this, PaymentResultActivity.class);
                                failIntent.putExtra(PaymentResultActivity.EXTRA_STATUS, PaymentResultActivity.STATUS_FAILED);
                                failIntent.putExtra(PaymentResultActivity.EXTRA_ORDER, order);
                                failIntent.putExtra(PaymentResultActivity.EXTRA_ERROR_MESSAGE, "Network error: " + t.getMessage());
                                startActivity(failIntent);
                                finish();
                            }
                        });
                    } else {
                        btnPlaceOrder.setEnabled(true);
                        pbCheckout.setVisibility(View.GONE);

                        // Cash on Delivery
                        Intent intent = new Intent(CheckoutActivity.this, PaymentResultActivity.class);
                        intent.putExtra(PaymentResultActivity.EXTRA_STATUS, PaymentResultActivity.STATUS_COD);
                        intent.putExtra(PaymentResultActivity.EXTRA_ORDER, order);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    btnPlaceOrder.setEnabled(true);
                    pbCheckout.setVisibility(View.GONE);
                    String errorMsg = "Could not place order. Please verify item stock or try again.";
                    if (response.body() != null && response.body().getMessage() != null) {
                        errorMsg = response.body().getMessage();
                    }
                    showOrderPlacementFailureDialog(errorMsg, req);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Order>> call, Throwable t) {
                btnPlaceOrder.setEnabled(true);
                pbCheckout.setVisibility(View.GONE);
                showOrderPlacementFailureDialog("Network error: " + t.getMessage(), req);
            }
        });
    }

    private void showOrderPlacementFailureDialog(String message, OrderCreateRequest req) {
        new AlertDialog.Builder(this)
                .setTitle("Order Placement Failed")
                .setMessage(message)
                .setPositiveButton("Retry", (dialog, which) -> sendCreateOrderRequest(req))
                .setNegativeButton("Cancel", null)
                .setCancelable(false)
                .show();
    }
}
