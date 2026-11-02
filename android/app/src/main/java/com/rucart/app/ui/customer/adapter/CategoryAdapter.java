package com.rucart.app.ui.customer.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.rucart.app.R;
import com.rucart.app.model.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> categories = new ArrayList<>();
    private final OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    public CategoryAdapter(OnCategoryClickListener listener) {
        this.listener = listener;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories != null ? categories : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.tvName.setText(category.getName());

        String name = category.getName() != null ? category.getName().toLowerCase() : "";
        if (name.contains("veg") || name.contains("fruit")) {
            holder.ivIcon.setImageResource(R.drawable.cat_veg_fruits);
        } else if (name.contains("dairy") || name.contains("milk") || name.contains("egg") || name.contains("bread")) {
            holder.ivIcon.setImageResource(R.drawable.cat_dairy_breakfast);
        } else if (name.contains("biscuit") || name.contains("bakery")) {
            holder.ivIcon.setImageResource(R.drawable.cat_bakery_biscuits);
        } else if (name.contains("instant") || name.contains("noodle") || name.contains("breakfast")) {
            holder.ivIcon.setImageResource(R.drawable.cat_instant_food);
        } else if (name.contains("tea") || name.contains("coffee")) {
            holder.ivIcon.setImageResource(R.drawable.cat_tea_coffee);
        } else if (name.contains("drink") || name.contains("juice") || name.contains("cold") || name.contains("beverage")) {
            holder.ivIcon.setImageResource(R.drawable.cat_cold_drinks);
        } else if (name.contains("sweet") || name.contains("chocolate")) {
            holder.ivIcon.setImageResource(R.drawable.cat_sweet_tooth);
        } else if (name.contains("atta") || name.contains("rice") || name.contains("dal")) {
            holder.ivIcon.setImageResource(R.drawable.cat_atta_rice_dal);
        } else if (name.contains("oil") || name.contains("masala") || name.contains("spice")) {
            holder.ivIcon.setImageResource(R.drawable.cat_masala_oil);
        } else if (name.contains("clean") || name.contains("house")) {
            holder.ivIcon.setImageResource(R.drawable.cat_cleaning);
        } else if (name.contains("personal") || name.contains("care") || name.contains("soap")) {
            holder.ivIcon.setImageResource(R.drawable.cat_personal_care);
        } else if (name.contains("baby")) {
            holder.ivIcon.setImageResource(R.drawable.cat_baby_care);
        } else if (name.contains("pet")) {
            holder.ivIcon.setImageResource(R.drawable.cat_pet_care);
        } else if (name.contains("snack") || name.contains("chip") || name.contains("munch")) {
            holder.ivIcon.setImageResource(R.drawable.cat_snacks_munchies);
        } else {
            holder.ivIcon.setImageResource(R.drawable.cat_veg_fruits);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategoryClick(category);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvName;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivCategoryIcon);
            tvName = itemView.findViewById(R.id.tvCategoryName);
        }
    }
}
