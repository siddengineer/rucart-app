package com.rucart.app.ui.customer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.rucart.app.R;
import com.rucart.app.model.Store;

import java.util.ArrayList;
import java.util.List;

public class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.StoreViewHolder> {

    public interface OnStoreSelectedListener {
        void onStoreSelected(Store store);
    }

    private final List<Store> stores = new ArrayList<>();
    private final OnStoreSelectedListener listener;

    public StoreAdapter(OnStoreSelectedListener listener) {
        this.listener = listener;
    }

    public void setStores(List<Store> newStores) {
        stores.clear();
        if (newStores != null) {
            stores.addAll(newStores);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_store_card, parent, false);
        return new StoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoreViewHolder holder, int position) {
        Store store = stores.get(position);
        Context context = holder.itemView.getContext();

        holder.tvName.setText(store.getName());
        holder.tvAddress.setText(store.getAddress() != null ? store.getAddress() : "Pune");

        String hours = "🕒 " + (store.getOpeningTime() != null ? store.getOpeningTime() : "07:00 AM")
                + " - " + (store.getClosingTime() != null ? store.getClosingTime() : "11:00 PM");
        holder.tvHours.setText(hours);

        if (store.getRating() != null) {
            holder.tvRating.setText(String.format("⭐ %.1f", store.getRating()));
        } else {
            holder.tvRating.setText("⭐ 4.5");
        }

        if (store.isOpen()) {
            holder.tvStatusBadge.setText("OPEN");
            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_green);
            holder.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.primary_green_dark));
            holder.btnSelect.setEnabled(true);
            holder.btnSelect.setText("Select This Store");
            holder.btnSelect.setBackgroundResource(R.drawable.bg_button_primary);
        } else {
            holder.tvStatusBadge.setText("CLOSED");
            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_red);
            holder.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.danger_red));
            holder.btnSelect.setEnabled(false);
            holder.btnSelect.setText("Store Currently Closed");
            holder.btnSelect.setBackgroundResource(R.drawable.bg_button_secondary);
        }

        if (store.getServiceAreas() != null && !store.getServiceAreas().isEmpty()) {
            holder.tvServiceAreas.setVisibility(View.VISIBLE);
            holder.tvServiceAreas.setText("📍 Delivers to: " + store.getServiceAreas());
        } else {
            holder.tvServiceAreas.setVisibility(View.GONE);
        }

        holder.btnSelect.setOnClickListener(v -> {
            if (listener != null) {
                listener.onStoreSelected(store);
            }
        });
    }

    @Override
    public int getItemCount() {
        return stores.size();
    }

    static class StoreViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvStatusBadge, tvAddress, tvHours, tvRating, tvServiceAreas;
        Button btnSelect;

        public StoreViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCardStoreName);
            tvStatusBadge = itemView.findViewById(R.id.tvCardStoreStatusBadge);
            tvAddress = itemView.findViewById(R.id.tvCardStoreAddress);
            tvHours = itemView.findViewById(R.id.tvCardStoreHours);
            tvRating = itemView.findViewById(R.id.tvCardStoreRating);
            tvServiceAreas = itemView.findViewById(R.id.tvCardStoreServiceAreas);
            btnSelect = itemView.findViewById(R.id.btnSelectStore);
        }
    }
}
