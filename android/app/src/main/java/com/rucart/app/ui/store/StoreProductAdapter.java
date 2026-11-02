package com.rucart.app.ui.store;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.rucart.app.R;
import com.rucart.app.model.Product;

import java.util.ArrayList;
import java.util.List;

public class StoreProductAdapter extends RecyclerView.Adapter<StoreProductAdapter.ViewHolder> {

    public interface OnStoreProductActionListener {
        void onAdjustStock(Product product);
        void onToggleAvailability(Product product, boolean isAvailable);
        void onProductClick(Product product);
    }

    private final Context context;
    private final OnStoreProductActionListener listener;
    private final List<Product> productList = new ArrayList<>();

    public StoreProductAdapter(Context context, OnStoreProductActionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setProducts(List<Product> products) {
        this.productList.clear();
        if (products != null) {
            this.productList.addAll(products);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_store_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product p = productList.get(position);

        holder.tvName.setText(p.getName() != null ? p.getName() : "Product");
        String category = p.getCategoryName() != null ? p.getCategoryName() : "Grocery";
        String unit = p.getUnit() != null ? p.getUnit() : "unit";
        holder.tvCategoryUnit.setText(category + " • " + unit);

        String priceStr = p.getPrice() != null ? "₹" + p.getPrice().toPlainString() : "₹0";
        holder.tvPrice.setText(priceStr);

        int stock = p.getStockQuantity();
        int threshold = p.getLowStockThreshold() > 0 ? p.getLowStockThreshold() : 10;
        holder.tvThreshold.setText("Low alert threshold: " + threshold + " " + unit);

        if (!p.isAvailable() || stock == 0) {
            holder.tvStockBadge.setText("Out of Stock");
            holder.tvStockBadge.setBackgroundResource(R.drawable.bg_badge_red);
            holder.tvStockBadge.setTextColor(ContextCompat.getColor(context, R.color.danger_red));
        } else if (stock <= threshold) {
            holder.tvStockBadge.setText("Low: " + stock + " left");
            holder.tvStockBadge.setBackgroundResource(R.drawable.bg_badge_orange);
            holder.tvStockBadge.setTextColor(ContextCompat.getColor(context, R.color.accent_orange));
        } else {
            holder.tvStockBadge.setText(stock + " in stock");
            holder.tvStockBadge.setBackgroundResource(R.drawable.bg_badge_green);
            holder.tvStockBadge.setTextColor(ContextCompat.getColor(context, R.color.primary_green_dark));
        }

        // Real Product Image
        if (p.getName() != null) {
            String nameLower = p.getName().toLowerCase();
            if (nameLower.contains("tomato")) {
                holder.ivIcon.setImageResource(R.drawable.img_product_tomato);
            } else if (nameLower.contains("onion")) {
                holder.ivIcon.setImageResource(R.drawable.img_product_onion);
            } else if (nameLower.contains("potato") && !nameLower.contains("chip")) {
                holder.ivIcon.setImageResource(R.drawable.img_product_potato);
            } else if (nameLower.contains("apple")) {
                holder.ivIcon.setImageResource(R.drawable.img_product_apple);
            } else if (nameLower.contains("banana")) {
                holder.ivIcon.setImageResource(R.drawable.img_product_banana);
            } else if (nameLower.contains("milk")) {
                holder.ivIcon.setImageResource(R.drawable.img_product_milk);
            } else if (nameLower.contains("bread")) {
                holder.ivIcon.setImageResource(R.drawable.img_product_bread);
            } else if (nameLower.contains("egg")) {
                holder.ivIcon.setImageResource(R.drawable.img_product_eggs);
            } else if (nameLower.contains("rice")) {
                holder.ivIcon.setImageResource(R.drawable.img_product_rice);
            } else if (nameLower.contains("noodle") || nameLower.contains("maggi")) {
                holder.ivIcon.setImageResource(R.drawable.img_product_noodles);
            } else if (nameLower.contains("biscuit") || nameLower.contains("cookie") || nameLower.contains("britannia") || nameLower.contains("parle")) {
                holder.ivIcon.setImageResource(R.drawable.img_product_biscuits);
            } else if (nameLower.contains("butter")) {
                holder.ivIcon.setImageResource(R.drawable.img_product_butter);
            } else if (nameLower.contains("oil") || nameLower.contains("saffola")) {
                holder.ivIcon.setImageResource(R.drawable.img_product_oil);
            } else if (nameLower.contains("salt")) {
                holder.ivIcon.setImageResource(R.drawable.img_product_salt);
            } else if (nameLower.contains("chip") || nameLower.contains("snack")) {
                holder.ivIcon.setImageResource(R.drawable.img_product_chips);
            } else if (nameLower.contains("drink") || nameLower.contains("juice") || nameLower.contains("cola") || nameLower.contains("beverage")) {
                holder.ivIcon.setImageResource(R.drawable.img_product_beverage);
            } else if (nameLower.contains("clean") || nameLower.contains("detergent") || nameLower.contains("soap") || nameLower.contains("wash")) {
                holder.ivIcon.setImageResource(R.drawable.img_product_detergent);
            } else {
                holder.ivIcon.setImageResource(R.drawable.img_product_tomato);
            }
        }

        // Switch
        holder.switchAvailable.setOnCheckedChangeListener(null);
        holder.switchAvailable.setChecked(p.isAvailable());
        holder.switchAvailable.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onToggleAvailability(p, isChecked);
            }
        });

        holder.btnAdjustStock.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAdjustStock(p);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProductClick(p);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvName, tvCategoryUnit, tvPrice, tvStockBadge, tvThreshold;
        SwitchMaterial switchAvailable;
        Button btnAdjustStock;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivStoreProductIcon);
            tvName = itemView.findViewById(R.id.tvStoreProductName);
            tvCategoryUnit = itemView.findViewById(R.id.tvStoreProductCategoryUnit);
            tvPrice = itemView.findViewById(R.id.tvStoreProductPrice);
            tvStockBadge = itemView.findViewById(R.id.tvStoreProductStockBadge);
            tvThreshold = itemView.findViewById(R.id.tvStoreProductThreshold);
            switchAvailable = itemView.findViewById(R.id.switchStoreProductAvailable);
            btnAdjustStock = itemView.findViewById(R.id.btnStoreAdjustStock);
        }
    }
}
