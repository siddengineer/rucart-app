package com.rucart.app.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.rucart.app.R;
import com.rucart.app.model.Order;

public class OrderSuccessActivity extends AppCompatActivity {

    public static final String EXTRA_ORDER = "extra_order";

    private Order order;

    private TextView tvOrderNumber, tvStoreName, tvAddress, tvOtp, tvTotal;
    private Button btnTrackOrder, btnContinueShopping;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_success);

        order = (Order) getIntent().getSerializableExtra(EXTRA_ORDER);

        initViews();
        bindData();
        setupListeners();
    }

    private void initViews() {
        tvOrderNumber = findViewById(R.id.tvSuccessOrderNumber);
        tvStoreName = findViewById(R.id.tvSuccessStoreName);
        tvAddress = findViewById(R.id.tvSuccessAddress);
        tvOtp = findViewById(R.id.tvSuccessOtp);
        tvTotal = findViewById(R.id.tvSuccessTotal);
        btnTrackOrder = findViewById(R.id.btnTrackOrder);
        btnContinueShopping = findViewById(R.id.btnContinueShopping);
    }

    private void bindData() {
        if (order == null) return;

        tvOrderNumber.setText("Order #" + (order.getOrderNumber() != null ? order.getOrderNumber() : "RUC-" + order.getId()));
        tvStoreName.setText(order.getStoreName() != null ? order.getStoreName() : "RUCART Express Store");
        tvAddress.setText(order.getDeliveryAddress() != null ? order.getDeliveryAddress() : "Shivajinagar, Pune");
        tvOtp.setText(order.getOtp() != null ? order.getOtp() : "4912");

        if (order.getGrandTotal() != null) {
            tvTotal.setText("₹" + order.getGrandTotal().toPlainString());
        } else if (order.getTotalAmount() != null) {
            tvTotal.setText("₹" + order.getTotalAmount().toPlainString());
        } else {
            tvTotal.setText("₹0.00");
        }
    }

    private void setupListeners() {
        btnTrackOrder.setOnClickListener(v -> {
            Intent intent = new Intent(OrderSuccessActivity.this, OrderDetailActivity.class);
            intent.putExtra(OrderDetailActivity.EXTRA_ORDER, order);
            startActivity(intent);
            finish();
        });

        btnContinueShopping.setOnClickListener(v -> {
            Intent intent = new Intent(OrderSuccessActivity.this, CustomerMainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(OrderSuccessActivity.this, CustomerMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
