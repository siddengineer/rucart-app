package com.rucart.app.ui.customer;

import android.graphics.Paint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.rucart.app.R;
import com.rucart.app.model.Product;
import com.rucart.app.util.CartManager;

public class ProductDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PRODUCT = "extra_product";

    private Product product;
    private int quantity = 1;

    private ImageView btnBack, btnCartAction, ivProductHero;
    private TextView tvBrandName, tvStockStatus, tvDetailTitle, tvDetailUnit;
    private TextView tvDetailPrice, tvDetailMRP, tvDiscountBadge, tvDetailDescription;
    private TextView btnDetailMinus, btnDetailPlus, tvDetailQuantity;
    private Button btnAddToCart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        product = (Product) getIntent().getSerializableExtra(EXTRA_PRODUCT);
        if (product == null) {
            Toast.makeText(this, "Product information unavailable", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        bindProductData();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnCartAction = findViewById(R.id.btnCartAction);
        tvBrandName = findViewById(R.id.tvBrandName);
        tvStockStatus = findViewById(R.id.tvStockStatus);
        tvDetailTitle = findViewById(R.id.tvDetailTitle);
        tvDetailUnit = findViewById(R.id.tvDetailUnit);
        tvDetailPrice = findViewById(R.id.tvDetailPrice);
        tvDetailMRP = findViewById(R.id.tvDetailMRP);
        tvDiscountBadge = findViewById(R.id.tvDiscountBadge);
        tvDetailDescription = findViewById(R.id.tvDetailDescription);
        btnDetailMinus = findViewById(R.id.btnDetailMinus);
        btnDetailPlus = findViewById(R.id.btnDetailPlus);
        tvDetailQuantity = findViewById(R.id.tvDetailQuantity);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        ivProductHero = findViewById(R.id.ivProductHero);
    }

    private void bindProductData() {
        tvDetailTitle.setText(product.getName());
        tvDetailUnit.setText(product.getUnit() != null ? product.getUnit() : "1 unit");
        double priceVal = product.getPrice() != null ? product.getPrice().doubleValue() : 0.0;
        tvDetailPrice.setText("₹" + (int) priceVal);

        if (ivProductHero != null && product.getName() != null) {
            String nameLower = product.getName().toLowerCase();
            if (nameLower.contains("tomato")) {
                ivProductHero.setImageResource(R.drawable.img_product_tomato);
            } else if (nameLower.contains("onion")) {
                ivProductHero.setImageResource(R.drawable.img_product_onion);
            } else if (nameLower.contains("potato") && !nameLower.contains("chip")) {
                ivProductHero.setImageResource(R.drawable.img_product_potato);
            } else if (nameLower.contains("apple")) {
                ivProductHero.setImageResource(R.drawable.img_product_apple);
            } else if (nameLower.contains("banana")) {
                ivProductHero.setImageResource(R.drawable.img_product_banana);
            } else if (nameLower.contains("milk")) {
                ivProductHero.setImageResource(R.drawable.img_product_milk);
            } else if (nameLower.contains("bread")) {
                ivProductHero.setImageResource(R.drawable.img_product_bread);
            } else if (nameLower.contains("egg")) {
                ivProductHero.setImageResource(R.drawable.img_product_eggs);
            } else if (nameLower.contains("rice")) {
                ivProductHero.setImageResource(R.drawable.img_product_rice);
            } else if (nameLower.contains("noodle") || nameLower.contains("maggi")) {
                ivProductHero.setImageResource(R.drawable.img_product_noodles);
            } else if (nameLower.contains("biscuit") || nameLower.contains("cookie") || nameLower.contains("britannia") || nameLower.contains("parle")) {
                ivProductHero.setImageResource(R.drawable.img_product_biscuits);
            } else if (nameLower.contains("butter")) {
                ivProductHero.setImageResource(R.drawable.img_product_butter);
            } else if (nameLower.contains("oil") || nameLower.contains("saffola")) {
                ivProductHero.setImageResource(R.drawable.img_product_oil);
            } else if (nameLower.contains("salt")) {
                ivProductHero.setImageResource(R.drawable.img_product_salt);
            } else if (nameLower.contains("chip") || nameLower.contains("snack")) {
                ivProductHero.setImageResource(R.drawable.img_product_chips);
            } else if (nameLower.contains("drink") || nameLower.contains("juice") || nameLower.contains("cola") || nameLower.contains("beverage")) {
                ivProductHero.setImageResource(R.drawable.img_product_beverage);
            } else if (nameLower.contains("clean") || nameLower.contains("detergent") || nameLower.contains("soap") || nameLower.contains("wash")) {
                ivProductHero.setImageResource(R.drawable.img_product_detergent);
            } else {
                ivProductHero.setImageResource(R.drawable.img_product_tomato);
            }

            if (product.getImageUrl() != null && product.getImageUrl().startsWith("http")) {
                com.bumptech.glide.Glide.with(this)
                        .load(product.getImageUrl())
                        .placeholder(ivProductHero.getDrawable())
                        .error(ivProductHero.getDrawable())
                        .into(ivProductHero);
            }
        }

        if (product.getCategoryName() != null && !product.getCategoryName().isEmpty()) {
            tvBrandName.setText("RUCART • " + product.getCategoryName());
        } else {
            tvBrandName.setText("RUCART Fresh");
        }

        if (product.getDescription() != null && !product.getDescription().isEmpty()) {
            tvDetailDescription.setText(product.getDescription());
        }

        double mrp = product.getMrp() > 0 ? product.getMrp() : Math.round(priceVal * 1.25);
        tvDetailMRP.setText("₹" + (int) mrp);
        tvDetailMRP.setPaintFlags(tvDetailMRP.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        if (mrp > priceVal && priceVal > 0) {
            int discountPercent = (int) Math.round(((mrp - priceVal) / mrp) * 100);
            tvDiscountBadge.setText(discountPercent + "% OFF");
        } else {
            tvDiscountBadge.setText("BEST PRICE");
        }

        // Stock availability & low-stock check
        if (!product.isInStock()) {
            tvStockStatus.setText("Out of Stock");
            tvStockStatus.setTextColor(ContextCompat.getColor(this, R.color.danger_red));
            btnAddToCart.setEnabled(false);
            btnAddToCart.setText("Out of Stock");
            btnAddToCart.setAlpha(0.6f);
        } else if (product.isLowStock()) {
            tvStockStatus.setText("Only " + product.getStockQuantity() + " left!");
            tvStockStatus.setTextColor(ContextCompat.getColor(this, R.color.accent_orange));
        } else {
            tvStockStatus.setText("In Stock (" + product.getStockQuantity() + " available)");
        }

        tvDetailQuantity.setText(String.valueOf(quantity));
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnCartAction.setOnClickListener(v -> {
            finish();
        });

        btnDetailMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                tvDetailQuantity.setText(String.valueOf(quantity));
            }
        });

        btnDetailPlus.setOnClickListener(v -> {
            int maxStock = product.getStockQuantity();
            if (quantity >= maxStock) {
                Toast.makeText(this, "Only " + maxStock + " units available in stock", Toast.LENGTH_SHORT).show();
                return;
            }
            quantity++;
            tvDetailQuantity.setText(String.valueOf(quantity));
        });

        btnAddToCart.setOnClickListener(v -> {
            if (!com.rucart.app.util.SessionManager.getInstance().isLoggedIn()) {
                com.rucart.app.util.AuthPromptDialog.showLoginRequired(this, "add " + product.getName() + " to your cart");
                return;
            }
            for (int i = 0; i < quantity; i++) {
                CartManager.getInstance().addProduct(product);
            }
            Toast.makeText(this, "Added " + quantity + " " + product.getName() + " to cart", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
