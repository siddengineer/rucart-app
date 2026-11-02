package com.rucart.app.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.rucart.app.R;
import com.rucart.app.model.StorePerformance;

import java.util.ArrayList;
import java.util.List;

public class StorePerformanceAdapter extends RecyclerView.Adapter<StorePerformanceAdapter.PerfViewHolder> {

    private List<StorePerformance> items = new ArrayList<>();

    public void setItems(List<StorePerformance> list) {
        this.items = (list != null) ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PerfViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_store_performance, parent, false);
        return new PerfViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PerfViewHolder holder, int position) {
        StorePerformance sp = items.get(position);
        holder.tvPerfStoreName.setText(sp.getStoreName());
        holder.tvPerfStoreCity.setText(sp.getCity() + (sp.isOpen() ? " • OPEN" : " • CLOSED"));
        holder.tvPerfOrders.setText("Orders: " + sp.getTotalOrders() + " (Delivered: " + sp.getDeliveredOrders() + ")");
        holder.tvPerfSales.setText("Sales: ₹" + sp.getTotalSales());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class PerfViewHolder extends RecyclerView.ViewHolder {
        TextView tvPerfStoreName, tvPerfStoreCity, tvPerfOrders, tvPerfSales;

        public PerfViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPerfStoreName = itemView.findViewById(R.id.tvPerfStoreName);
            tvPerfStoreCity = itemView.findViewById(R.id.tvPerfStoreCity);
            tvPerfOrders = itemView.findViewById(R.id.tvPerfOrders);
            tvPerfSales = itemView.findViewById(R.id.tvPerfSales);
        }
    }
}
