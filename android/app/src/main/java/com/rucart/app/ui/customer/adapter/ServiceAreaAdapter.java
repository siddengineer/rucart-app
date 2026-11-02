package com.rucart.app.ui.customer.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.rucart.app.R;

import java.util.ArrayList;
import java.util.List;

public class ServiceAreaAdapter extends RecyclerView.Adapter<ServiceAreaAdapter.AreaViewHolder> {

    public interface OnAreaClickListener {
        void onAreaClick(String area);
    }

    private final List<String> areas = new ArrayList<>();
    private final OnAreaClickListener listener;
    private String selectedArea = "";

    public ServiceAreaAdapter(OnAreaClickListener listener) {
        this.listener = listener;
    }

    public void setAreas(List<String> newAreas, String currentSelected) {
        areas.clear();
        if (newAreas != null) {
            areas.addAll(newAreas);
        }
        this.selectedArea = currentSelected != null ? currentSelected : "";
        notifyDataSetChanged();
    }

    public void setSelectedArea(String area) {
        this.selectedArea = area;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AreaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_service_area_chip, parent, false);
        return new AreaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AreaViewHolder holder, int position) {
        String area = areas.get(position);
        holder.tvChip.setText(area);

        boolean isSelected = selectedArea.equalsIgnoreCase(area) || selectedArea.contains(area);
        if (isSelected) {
            holder.tvChip.setBackgroundResource(R.drawable.bg_button_primary);
            holder.tvChip.setTextColor(holder.itemView.getContext().getColor(R.color.white));
        } else {
            holder.tvChip.setBackgroundResource(R.drawable.bg_badge_green);
            holder.tvChip.setTextColor(holder.itemView.getContext().getColor(R.color.primary_green_dark));
        }

        holder.itemView.setOnClickListener(v -> {
            setSelectedArea(area);
            if (listener != null) {
                listener.onAreaClick(area);
            }
        });
    }

    @Override
    public int getItemCount() {
        return areas.size();
    }

    static class AreaViewHolder extends RecyclerView.ViewHolder {
        TextView tvChip;

        public AreaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvChip = itemView.findViewById(R.id.tvAreaChip);
        }
    }
}
