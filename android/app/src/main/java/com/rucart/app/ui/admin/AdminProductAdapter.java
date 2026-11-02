package com.rucart.app.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.rucart.app.R;
import com.rucart.app.model.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminProductAdapter
        extends RecyclerView.Adapter<AdminProductAdapter.ProductViewHolder> {

    public interface ProductActionListener {
        void onToggleAvailability(Product product);

        void onEditProduct(Product product);
    }

    private List<Product> products = new ArrayList<>();
    private final ProductActionListener listener;

    public AdminProductAdapter(ProductActionListener listener) {
        this.listener = listener;
    }

    public void setProducts(List<Product> list) {
        this.products = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_product, parent, false);

        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ProductViewHolder holder,
            int position
    ) {
        Product product = products.get(position);

        // Product name
        String name = product.getName();

        holder.tvAdminProductName.setText(
                name != null && !name.trim().isEmpty()
                        ? name
                        : "Unnamed Product"
        );

        // Brand is not present in the Product model.
        holder.tvAdminProductBrand.setText("RUCART");

        // Product price
        holder.tvAdminProductPrice.setText(
                "₹" + (product.getPrice() != null
                        ? product.getPrice().toPlainString()
                        : "0.00")
        );

        // Product unit
        String unit = product.getUnit();

        holder.tvAdminProductUnit.setText(
                "/ " + (unit != null && !unit.trim().isEmpty()
                        ? unit
                        : "unit")
        );

        // Product stock
        int stock = product.getStockQuantity();

        holder.tvAdminProductStock.setText(
                "Stock: " + stock
        );

        // Product category
        String category = product.getCategoryName();

        holder.tvAdminProductCategory.setText(
                "Category: " + (category != null && !category.trim().isEmpty()
                        ? category
                        : "Uncategorized")
        );

        // Availability
        boolean available = product.isAvailable();

        if (available) {
            holder.tvAdminProductAvailableBadge.setText("AVAILABLE");
            holder.tvAdminProductAvailableBadge.setTextColor(
                    holder.itemView.getContext()
                            .getColor(R.color.accent_green)
            );
            holder.btnToggleProductStatus.setText("Deactivate");
        } else {
            holder.tvAdminProductAvailableBadge.setText("UNAVAILABLE");
            holder.tvAdminProductAvailableBadge.setTextColor(
                    holder.itemView.getContext()
                            .getColor(R.color.accent_red)
            );
            holder.btnToggleProductStatus.setText("Activate");
        }

        // Toggle availability
        holder.btnToggleProductStatus.setOnClickListener(v -> {
            if (listener != null) {
                listener.onToggleAvailability(product);
            }
        });

        // Edit product
        holder.btnEditProduct.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditProduct(product);
            }
        });

        // Open edit screen when the product row is clicked.
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditProduct(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {

        TextView tvAdminProductBrand;
        TextView tvAdminProductName;
        TextView tvAdminProductAvailableBadge;
        TextView tvAdminProductPrice;
        TextView tvAdminProductUnit;
        TextView tvAdminProductStock;
        TextView tvAdminProductCategory;

        AppCompatButton btnToggleProductStatus;
        AppCompatButton btnEditProduct;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);

            tvAdminProductBrand =
                    itemView.findViewById(R.id.tvAdminProductBrand);

            tvAdminProductName =
                    itemView.findViewById(R.id.tvAdminProductName);

            tvAdminProductAvailableBadge =
                    itemView.findViewById(R.id.tvAdminProductAvailableBadge);

            tvAdminProductPrice =
                    itemView.findViewById(R.id.tvAdminProductPrice);

            tvAdminProductUnit =
                    itemView.findViewById(R.id.tvAdminProductUnit);

            tvAdminProductStock =
                    itemView.findViewById(R.id.tvAdminProductStock);

            tvAdminProductCategory =
                    itemView.findViewById(R.id.tvAdminProductCategory);

            btnToggleProductStatus =
                    itemView.findViewById(R.id.btnToggleProductStatus);

            btnEditProduct =
                    itemView.findViewById(R.id.btnEditProduct);
        }
    }
}