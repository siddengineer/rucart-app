package com.rucart.app.ui.customer.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.rucart.app.R;
import com.rucart.app.model.CartItem;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartItems = new ArrayList<>();
    private final OnCartItemActionListener listener;

    public interface OnCartItemActionListener {
        void onIncrement(CartItem item);
        void onDecrement(CartItem item);
    }

    public CartAdapter(OnCartItemActionListener listener) {
        this.listener = listener;
    }

    public void setCartItems(List<CartItem> items) {
        this.cartItems = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.tvName.setText(item.getProduct().getName());
        holder.tvUnit.setText(item.getProduct().getUnit() != null ? item.getProduct().getUnit() : "1 pc");

        BigDecimal lineTotal = item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        holder.tvPrice.setText("₹" + lineTotal.toPlainString());
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));

        if (holder.ivImage != null && item.getProduct().getName() != null) {
            String nameLower = item.getProduct().getName().toLowerCase();
            if (nameLower.contains("tomato")) {
                holder.ivImage.setImageResource(R.drawable.img_product_tomato);
            } else if (nameLower.contains("onion")) {
                holder.ivImage.setImageResource(R.drawable.img_product_onion);
            } else if (nameLower.contains("potato") && !nameLower.contains("chip")) {
                holder.ivImage.setImageResource(R.drawable.img_product_potato);
            } else if (nameLower.contains("apple")) {
                holder.ivImage.setImageResource(R.drawable.img_product_apple);
            } else if (nameLower.contains("banana")) {
                holder.ivImage.setImageResource(R.drawable.img_product_banana);
            } else if (nameLower.contains("milk")) {
                holder.ivImage.setImageResource(R.drawable.img_product_milk);
            } else if (nameLower.contains("bread")) {
                holder.ivImage.setImageResource(R.drawable.img_product_bread);
            } else if (nameLower.contains("egg")) {
                holder.ivImage.setImageResource(R.drawable.img_product_eggs);
            } else if (nameLower.contains("rice")) {
                holder.ivImage.setImageResource(R.drawable.img_product_rice);
            } else if (nameLower.contains("noodle") || nameLower.contains("maggi")) {
                holder.ivImage.setImageResource(R.drawable.img_product_noodles);
            } else if (nameLower.contains("biscuit") || nameLower.contains("cookie")) {
                holder.ivImage.setImageResource(R.drawable.img_product_biscuits);
            } else if (nameLower.contains("butter")) {
                holder.ivImage.setImageResource(R.drawable.img_product_butter);
            } else if (nameLower.contains("oil") || nameLower.contains("saffola")) {
                holder.ivImage.setImageResource(R.drawable.img_product_oil);
            } else if (nameLower.contains("salt")) {
                holder.ivImage.setImageResource(R.drawable.img_product_salt);
            } else {
                holder.ivImage.setImageResource(R.drawable.img_product_tomato);
            }
        }

        holder.btnPlus.setOnClickListener(v -> {
            if (listener != null) {
                listener.onIncrement(item);
            }
        });

        holder.btnMinus.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDecrement(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvUnit, tvPrice, tvQuantity, btnMinus, btnPlus;
        ImageView ivImage;

        CartViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivCartItemImage);
            tvName = itemView.findViewById(R.id.tvCartItemName);
            tvUnit = itemView.findViewById(R.id.tvCartItemUnit);
            tvPrice = itemView.findViewById(R.id.tvCartItemPrice);
            tvQuantity = itemView.findViewById(R.id.tvCartQuantity);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnPlus = itemView.findViewById(R.id.btnPlus);
        }
    }
}
