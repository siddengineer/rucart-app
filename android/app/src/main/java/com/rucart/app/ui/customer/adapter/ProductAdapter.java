package com.rucart.app.ui.customer.adapter;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.rucart.app.R;
import com.rucart.app.model.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> products = new ArrayList<>();
    private final OnProductAddListener addListener;
    private OnProductClickListener clickListener;

    public interface OnProductAddListener {
        void onProductAdd(Product product);
    }

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public ProductAdapter(OnProductAddListener addListener) {
        this.addListener = addListener;
    }

    public void setOnProductClickListener(OnProductClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setProducts(List<Product> products) {
        this.products = products != null ? products : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);
        holder.tvName.setText(product.getName());
        holder.tvUnit.setText(product.getUnit() != null ? product.getUnit() : "1 pc");
        holder.tvPrice.setText("₹" + (product.getPrice() != null ? product.getPrice().toPlainString() : "0"));

        if (product.getName() != null) {
            String nameLower = product.getName().toLowerCase();
            if (nameLower.contains("tomato")) {
                holder.ivProduct.setImageResource(R.drawable.img_product_tomato);
            } else if (nameLower.contains("onion")) {
                holder.ivProduct.setImageResource(R.drawable.img_product_onion);
            } else if (nameLower.contains("potato") && !nameLower.contains("chip")) {
                holder.ivProduct.setImageResource(R.drawable.img_product_potato);
            } else if (nameLower.contains("apple")) {
                holder.ivProduct.setImageResource(R.drawable.img_product_apple);
            } else if (nameLower.contains("banana")) {
                holder.ivProduct.setImageResource(R.drawable.img_product_banana);
            } else if (nameLower.contains("milk")) {
                holder.ivProduct.setImageResource(R.drawable.img_product_milk);
            } else if (nameLower.contains("bread")) {
                holder.ivProduct.setImageResource(R.drawable.img_product_bread);
            } else if (nameLower.contains("egg")) {
                holder.ivProduct.setImageResource(R.drawable.img_product_eggs);
            } else if (nameLower.contains("rice")) {
                holder.ivProduct.setImageResource(R.drawable.img_product_rice);
            } else if (nameLower.contains("noodle") || nameLower.contains("maggi")) {
                holder.ivProduct.setImageResource(R.drawable.img_product_noodles);
            } else if (nameLower.contains("biscuit") || nameLower.contains("cookie") || nameLower.contains("britannia") || nameLower.contains("parle")) {
                holder.ivProduct.setImageResource(R.drawable.img_product_biscuits);
            } else if (nameLower.contains("butter")) {
                holder.ivProduct.setImageResource(R.drawable.img_product_butter);
            } else if (nameLower.contains("oil") || nameLower.contains("saffola")) {
                holder.ivProduct.setImageResource(R.drawable.img_product_oil);
            } else if (nameLower.contains("salt")) {
                holder.ivProduct.setImageResource(R.drawable.img_product_salt);
            } else if (nameLower.contains("chip") || nameLower.contains("snack")) {
                holder.ivProduct.setImageResource(R.drawable.img_product_chips);
            } else if (nameLower.contains("drink") || nameLower.contains("juice") || nameLower.contains("cola") || nameLower.contains("beverage")) {
                holder.ivProduct.setImageResource(R.drawable.img_product_beverage);
            } else if (nameLower.contains("clean") || nameLower.contains("detergent") || nameLower.contains("soap") || nameLower.contains("wash")) {
                holder.ivProduct.setImageResource(R.drawable.img_product_detergent);
            } else {
                holder.ivProduct.setImageResource(R.drawable.img_product_tomato);
            }
        }

        if (product.getImageUrl() != null && product.getImageUrl().startsWith("http")) {
            com.bumptech.glide.Glide.with(holder.itemView.getContext())
                    .load(product.getImageUrl())
                    .placeholder(holder.ivProduct.getDrawable())
                    .error(holder.ivProduct.getDrawable())
                    .into(holder.ivProduct);
        }

        if (product.getOriginalPrice() != null && product.getOriginalPrice().compareTo(product.getPrice()) > 0) {
            holder.tvOriginalPrice.setVisibility(View.VISIBLE);
            holder.tvOriginalPrice.setText("₹" + product.getOriginalPrice().toPlainString());
            holder.tvOriginalPrice.setPaintFlags(holder.tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.tvOriginalPrice.setVisibility(View.GONE);
        }

        holder.btnAdd.setOnClickListener(v -> {
            if (addListener != null) {
                addListener.onProductAdd(product);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onProductClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct;
        TextView tvName, tvUnit, tvPrice, tvOriginalPrice;
        Button btnAdd;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.ivProduct);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvUnit = itemView.findViewById(R.id.tvProductUnit);
            tvPrice = itemView.findViewById(R.id.tvProductPrice);
            tvOriginalPrice = itemView.findViewById(R.id.tvProductOriginalPrice);
            btnAdd = itemView.findViewById(R.id.btnAddProduct);
        }
    }
}
