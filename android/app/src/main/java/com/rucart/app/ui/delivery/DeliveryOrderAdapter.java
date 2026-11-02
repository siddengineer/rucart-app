package com.rucart.app.ui.delivery;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;
import com.rucart.app.R;
import com.rucart.app.model.Order;

import java.util.ArrayList;
import java.util.List;

public class DeliveryOrderAdapter extends RecyclerView.Adapter<DeliveryOrderAdapter.ViewHolder> {

    public interface OnOrderActionListener {
        void onAction(Order order, boolean isClaimAction);
    }

    private List<Order> orders = new ArrayList<>();
    private boolean isAvailableTab = true;
    private final OnOrderActionListener listener;

    public DeliveryOrderAdapter(OnOrderActionListener listener) {
        this.listener = listener;
    }

    public void setOrders(List<Order> orders, boolean isAvailableTab) {
        this.orders = orders != null ? orders : new ArrayList<>();
        this.isAvailableTab = isAvailableTab;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_delivery_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.bind(order, isAvailableTab, listener);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderNumber;
        TextView tvOrderStatusBadge;
        TextView tvStoreName;
        TextView tvStoreAddress;
        TextView tvCustomerName;
        TextView tvCustomerAddress;
        TextView tvOrderAmount;
        TextView tvOrderItemsCount;
        TextView tvPaymentBadge;
        AppCompatButton btnOrderAction;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderNumber = itemView.findViewById(R.id.tvOrderNumber);
            tvOrderStatusBadge = itemView.findViewById(R.id.tvOrderStatusBadge);
            tvStoreName = itemView.findViewById(R.id.tvStoreName);
            tvStoreAddress = itemView.findViewById(R.id.tvStoreAddress);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvCustomerAddress = itemView.findViewById(R.id.tvCustomerAddress);
            tvOrderAmount = itemView.findViewById(R.id.tvOrderAmount);
            tvOrderItemsCount = itemView.findViewById(R.id.tvOrderItemsCount);
            tvPaymentBadge = itemView.findViewById(R.id.tvPaymentBadge);
            btnOrderAction = itemView.findViewById(R.id.btnOrderAction);
        }

        void bind(Order order, boolean isAvailableTab, OnOrderActionListener listener) {
            tvOrderNumber.setText("#" + order.getOrderNumber());

            // Status Badge
            String status = order.getStatus() != null ? order.getStatus() : "READY_FOR_PICKUP";
            tvOrderStatusBadge.setText(status.replace('_', ' '));
            if ("READY_FOR_PICKUP".equals(status)) {
                tvOrderStatusBadge.setBackgroundResource(R.drawable.bg_badge_orange);
            } else if ("ASSIGNED".equals(status)) {
                tvOrderStatusBadge.setBackgroundResource(R.drawable.bg_badge_blue);
            } else if ("PICKED_UP".equals(status)) {
                tvOrderStatusBadge.setBackgroundResource(R.drawable.bg_badge_orange);
            } else if ("OUT_FOR_DELIVERY".equals(status)) {
                tvOrderStatusBadge.setBackgroundResource(R.drawable.bg_badge_green);
            } else {
                tvOrderStatusBadge.setBackgroundResource(R.drawable.bg_badge_blue);
            }

            // Store Info
            tvStoreName.setText(order.getStoreName());
            if (order.getStore() != null && order.getStore().getAddress() != null) {
                tvStoreAddress.setText(order.getStore().getAddress());
            } else {
                tvStoreAddress.setText("Shivajinagar Central Hub");
            }

            // Customer Info
            tvCustomerName.setText(order.getCustomerName() != null ? order.getCustomerName() : "Customer");
            tvCustomerAddress.setText(order.getDeliveryAddress() != null ? order.getDeliveryAddress() : "Address on file");

            // Amount & items
            tvOrderAmount.setText("₹ " + (order.getGrandTotal() != null ? String.format("%.2f", order.getGrandTotal()) : "0.00"));
            int count = order.getItems() != null ? order.getItems().size() : 1;
            tvOrderItemsCount.setText("• " + count + (count > 1 ? " items" : " item"));

            // Payment Badge
            String method = order.getPaymentMethod() != null ? order.getPaymentMethod() : "CASH_ON_DELIVERY";
            if (method.contains("CASH")) {
                tvPaymentBadge.setText("COD");
                tvPaymentBadge.setBackgroundResource(R.drawable.bg_badge_orange);
            } else {
                tvPaymentBadge.setText("ONLINE");
                tvPaymentBadge.setBackgroundResource(R.drawable.bg_badge_green);
            }

            // Button Action
            if (isAvailableTab) {
                btnOrderAction.setText("Claim Order");
                btnOrderAction.setBackgroundResource(R.drawable.bg_button_primary);
                btnOrderAction.setTextColor(itemView.getContext().getResources().getColor(R.color.colorWhite));
            } else {
                btnOrderAction.setText("Workflow →");
                btnOrderAction.setBackgroundResource(R.drawable.bg_button_secondary);
                btnOrderAction.setTextColor(itemView.getContext().getResources().getColor(R.color.colorPrimaryDark));
            }

            btnOrderAction.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAction(order, isAvailableTab);
                }
            });

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAction(order, false);
                }
            });
        }
    }
}
