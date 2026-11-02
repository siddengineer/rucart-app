package com.rucart.app.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;
import com.rucart.app.R;
import com.rucart.app.model.Store;

import java.util.ArrayList;
import java.util.List;

public class AdminStoreAdapter extends RecyclerView.Adapter<AdminStoreAdapter.StoreViewHolder> {

    public interface StoreActionListener {
        void onToggleOpen(Store store);
        void onToggleActive(Store store);
        void onEditStore(Store store);
    }

    private List<Store> stores = new ArrayList<>();
    private final StoreActionListener listener;

    public AdminStoreAdapter(StoreActionListener listener) {
        this.listener = listener;
    }

    public void setStores(List<Store> list) {
        this.stores = (list != null) ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_store, parent, false);
        return new StoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoreViewHolder holder, int position) {
        Store store = stores.get(position);
        holder.tvStoreName.setText(store.getName());
        holder.tvStoreAddress.setText(store.getAddress() + ", " + store.getCity());
        holder.tvServiceAreas.setText("Serves: " + (store.getServiceAreas() != null ? store.getServiceAreas() : "Local"));
        holder.tvOperatingHours.setText("Hours: " + (store.getOpeningTime() != null ? store.getOpeningTime() : "07:00 AM")
                + " - " + (store.getClosingTime() != null ? store.getClosingTime() : "11:00 PM"));

        // Status Badges
        if (store.isOpen()) {
            holder.tvOpenBadge.setText("OPEN");
            holder.tvOpenBadge.setTextColor(holder.itemView.getContext().getColor(R.color.accent_green));
        } else {
            holder.tvOpenBadge.setText("CLOSED");
            holder.tvOpenBadge.setTextColor(holder.itemView.getContext().getColor(R.color.accent_red));
        }

        if (store.isActive()) {
            holder.tvActiveBadge.setText("ACTIVE");
            holder.tvActiveBadge.setTextColor(holder.itemView.getContext().getColor(R.color.primary_green));
            holder.btnToggleActive.setText("Deactivate");
        } else {
            holder.tvActiveBadge.setText("INACTIVE");
            holder.tvActiveBadge.setTextColor(holder.itemView.getContext().getColor(R.color.accent_red));
            holder.btnToggleActive.setText("Activate");
        }

        holder.btnToggleOpen.setText(store.isOpen() ? "Close Store" : "Open Store");

        holder.btnToggleOpen.setOnClickListener(v -> listener.onToggleOpen(store));
        holder.btnToggleActive.setOnClickListener(v -> listener.onToggleActive(store));
        holder.btnEditStore.setOnClickListener(v -> listener.onEditStore(store));
    }

    @Override
    public int getItemCount() {
        return stores.size();
    }

    static class StoreViewHolder extends RecyclerView.ViewHolder {
        TextView tvStoreName, tvOpenBadge, tvActiveBadge, tvStoreAddress, tvServiceAreas, tvOperatingHours;
        AppCompatButton btnToggleOpen, btnToggleActive, btnEditStore;

        public StoreViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStoreName = itemView.findViewById(R.id.tvStoreName);
            tvOpenBadge = itemView.findViewById(R.id.tvOpenBadge);
            tvActiveBadge = itemView.findViewById(R.id.tvActiveBadge);
            tvStoreAddress = itemView.findViewById(R.id.tvStoreAddress);
            tvServiceAreas = itemView.findViewById(R.id.tvServiceAreas);
            tvOperatingHours = itemView.findViewById(R.id.tvOperatingHours);
            btnToggleOpen = itemView.findViewById(R.id.btnToggleOpen);
            btnToggleActive = itemView.findViewById(R.id.btnToggleActive);
            btnEditStore = itemView.findViewById(R.id.btnEditStore);
        }
    }
}
