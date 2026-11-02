package com.rucart.app.ui.delivery;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.rucart.app.R;
import com.rucart.app.model.Order;

import java.util.ArrayList;
import java.util.List;

public class DeliveryHistoryAdapter extends RecyclerView.Adapter<DeliveryHistoryAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Order order);
    }

    private List<Order> orders = new ArrayList<>();
    private final OnItemClickListener listener;

    public DeliveryHistoryAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders != null ? orders : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_delivery_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.bind(order, listener);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvHistoryOrderNumber;
        TextView tvHistoryStatusBadge;
        TextView tvHistoryDate;
        TextView tvHistoryStoreAndCustomer;
        TextView tvHistoryNotes;
        TextView tvHistoryEarnings;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHistoryOrderNumber = itemView.findViewById(R.id.tvHistoryOrderNumber);
            tvHistoryStatusBadge = itemView.findViewById(R.id.tvHistoryStatusBadge);
            tvHistoryDate = itemView.findViewById(R.id.tvHistoryDate);
            tvHistoryStoreAndCustomer = itemView.findViewById(R.id.tvHistoryStoreAndCustomer);
            tvHistoryNotes = itemView.findViewById(R.id.tvHistoryNotes);
            tvHistoryEarnings = itemView.findViewById(R.id.tvHistoryEarnings);
        }

        void bind(Order order, OnItemClickListener listener) {
            tvHistoryOrderNumber.setText("#" + order.getOrderNumber());

            String status = order.getStatus() != null ? order.getStatus() : "DELIVERED";
            tvHistoryStatusBadge.setText(status.replace('_', ' '));

            if ("DELIVERED".equals(status)) {
                tvHistoryStatusBadge.setBackgroundResource(R.drawable.bg_badge_green);
                tvHistoryEarnings.setText("Earned: ₹ 35.00");
                tvHistoryEarnings.setTextColor(itemView.getContext().getResources().getColor(R.color.colorPrimary));

                String note = order.getDeliveryNotes() != null && !order.getDeliveryNotes().isEmpty()
                        ? "Note: " + order.getDeliveryNotes() : "Handed over directly to customer";
                tvHistoryNotes.setText(note);
                tvHistoryNotes.setVisibility(View.VISIBLE);
            } else {
                tvHistoryStatusBadge.setBackgroundResource(R.drawable.bg_badge_orange);
                tvHistoryEarnings.setText("Delivery Unsuccessful");
                tvHistoryEarnings.setTextColor(itemView.getContext().getResources().getColor(R.color.colorStatusCancelled));

                String reason = order.getDeliveryIssueReason() != null ? order.getDeliveryIssueReason() : "Failed Attempt";
                String details = order.getDeliveryIssueDetails() != null ? " (" + order.getDeliveryIssueDetails() + ")" : "";
                tvHistoryNotes.setText("Issue: " + reason.replace('_', ' ') + details);
                tvHistoryNotes.setVisibility(View.VISIBLE);
            }

            // Timestamp
            String timestamp = order.getDeliveredAt() != null ? order.getDeliveredAt()
                    : (order.getDeliveryIssueAt() != null ? order.getDeliveryIssueAt()
                    : (order.getCreatedAt() != null ? order.getCreatedAt() : "Recently"));
            tvHistoryDate.setText(timestamp);

            // Store and Customer
            String store = order.getStoreName();
            String area = order.getDeliveryAddress() != null ? order.getDeliveryAddress() : "Destination";
            tvHistoryStoreAndCustomer.setText(store + " → " + area);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(order);
                }
            });
        }
    }
}
