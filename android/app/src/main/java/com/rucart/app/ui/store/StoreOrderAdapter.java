package com.rucart.app.ui.store;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.rucart.app.R;
import com.rucart.app.model.Order;
import com.rucart.app.model.OrderItem;

import java.util.ArrayList;
import java.util.List;

public class StoreOrderAdapter extends RecyclerView.Adapter<StoreOrderAdapter.StoreOrderViewHolder> {

    public interface OnStoreOrderActionListener {
        void onAccept(Order order);
        void onReject(Order order);
        void onAdvanceStatus(Order order, String nextStatus);
        void onOrderClick(Order order);
    }

    private final List<Order> orders = new ArrayList<>();
    private final OnStoreOrderActionListener listener;

    public StoreOrderAdapter(OnStoreOrderActionListener listener) {
        this.listener = listener;
    }

    public void setOrders(List<Order> newOrders) {
        orders.clear();
        if (newOrders != null) {
            orders.addAll(newOrders);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StoreOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_store_order, parent, false);
        return new StoreOrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoreOrderViewHolder holder, int position) {
        Order order = orders.get(position);
        Context context = holder.itemView.getContext();

        holder.tvOrderNumber.setText("Order #" + (order.getOrderNumber() != null ? order.getOrderNumber() : order.getId()));
        String status = order.getStatus() != null ? order.getStatus().toUpperCase() : "PLACED";
        holder.tvStatusBadge.setText(status.replace('_', ' '));

        // Customer snapshot
        String customerText = "👤 " + (order.getCustomerName() != null ? order.getCustomerName() : "Customer");
        if (order.getCustomerPhone() != null && !order.getCustomerPhone().isEmpty()) {
            customerText += " • " + order.getCustomerPhone();
        }
        holder.tvCustomer.setText(customerText);

        holder.tvAddress.setText("📍 Deliver to: " + (order.getDeliveryAddress() != null ? order.getDeliveryAddress() : "Pune"));

        // Items snapshot summary
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            StringBuilder sb = new StringBuilder("📦 ");
            for (int i = 0; i < order.getItems().size(); i++) {
                OrderItem item = order.getItems().get(i);
                if (i > 0) sb.append(", ");
                sb.append(item.getQuantity()).append("x ").append(item.getProductName());
                if (item.getUnit() != null && !item.getUnit().isEmpty()) {
                    sb.append(" (").append(item.getUnit()).append(")");
                }
            }
            holder.tvItems.setText(sb.toString());
            holder.tvItems.setVisibility(View.VISIBLE);
        } else {
            holder.tvItems.setText("📦 Standard grocery basket items");
            holder.tvItems.setVisibility(View.VISIBLE);
        }

        String total = order.getGrandTotal() != null ? order.getGrandTotal().toPlainString()
                : (order.getTotalAmount() != null ? order.getTotalAmount().toPlainString() : "0");
        holder.tvTotal.setText("Total: ₹" + total);

        holder.tvPayment.setText(order.getPaymentMethod() != null ? order.getPaymentMethod().replace('_', ' ') : "COD");

        // Action Buttons logic
        if ("PLACED".equals(status) || "PAYMENT_CONFIRMED".equals(status)) {
            holder.layoutActions.setVisibility(View.VISIBLE);
            holder.btnReject.setVisibility(View.VISIBLE);
            holder.btnActionPrimary.setText("✅ Accept Order");
            holder.btnActionPrimary.setOnClickListener(v -> {
                if (listener != null) listener.onAccept(order);
            });
            holder.btnReject.setOnClickListener(v -> {
                if (listener != null) listener.onReject(order);
            });
            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_orange);
            holder.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.accent_orange));
        } else if ("ACCEPTED".equals(status)) {
            holder.layoutActions.setVisibility(View.VISIBLE);
            holder.btnReject.setVisibility(View.GONE);
            holder.btnActionPrimary.setText("👨‍🍳 Start Preparing");
            holder.btnActionPrimary.setOnClickListener(v -> {
                if (listener != null) listener.onAdvanceStatus(order, "PREPARING");
            });
            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_blue);
            holder.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.info_blue));
        } else if ("PREPARING".equals(status)) {
            holder.layoutActions.setVisibility(View.VISIBLE);
            holder.btnReject.setVisibility(View.GONE);
            holder.btnActionPrimary.setText("📦 Mark Ready for Pickup");
            holder.btnActionPrimary.setOnClickListener(v -> {
                if (listener != null) listener.onAdvanceStatus(order, "READY_FOR_PICKUP");
            });
            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_blue);
            holder.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.info_blue));
        } else if ("READY_FOR_PICKUP".equals(status)) {
            holder.layoutActions.setVisibility(View.GONE);
            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_green);
            holder.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.primary_green_dark));
        } else if ("CANCELLED".equals(status)) {
            holder.layoutActions.setVisibility(View.GONE);
            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_red);
            holder.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.danger_red));
        } else {
            // OUT_FOR_DELIVERY, DELIVERED, etc.
            holder.layoutActions.setVisibility(View.GONE);
            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_green);
            holder.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.primary_green_dark));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onOrderClick(order);
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class StoreOrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderNumber, tvStatusBadge, tvCustomer, tvAddress, tvItems, tvTotal, tvPayment;
        LinearLayout layoutActions;
        Button btnReject, btnActionPrimary;

        public StoreOrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderNumber = itemView.findViewById(R.id.tvStoreOrderNumber);
            tvStatusBadge = itemView.findViewById(R.id.tvStoreOrderStatusBadge);
            tvCustomer = itemView.findViewById(R.id.tvStoreOrderCustomer);
            tvAddress = itemView.findViewById(R.id.tvStoreOrderAddress);
            tvItems = itemView.findViewById(R.id.tvStoreOrderItems);
            tvTotal = itemView.findViewById(R.id.tvStoreOrderTotal);
            tvPayment = itemView.findViewById(R.id.tvStoreOrderPayment);
            layoutActions = itemView.findViewById(R.id.layoutStoreActions);
            btnReject = itemView.findViewById(R.id.btnStoreReject);
            btnActionPrimary = itemView.findViewById(R.id.btnStoreActionPrimary);
        }
    }
}
