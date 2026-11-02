package com.rucart.app.ui.customer.adapter;

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

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<Order> orders = new ArrayList<>();
    private final OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    public OrderAdapter(OnOrderClickListener listener) {
        this.listener = listener;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders != null ? orders : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.tvOrderNumber.setText("Order #" + (order.getOrderNumber() != null ? order.getOrderNumber() : order.getId()));
        holder.tvStatusBadge.setText(order.getStatus() != null ? order.getStatus() : "PLACED");
        holder.tvStore.setText("Store: " + (order.getStore() != null ? order.getStore().getName() : "RUCART Super Store"));
        holder.tvTotal.setText("Total: ₹" + (order.getGrandTotal() != null ? order.getGrandTotal().toPlainString() : "0"));
        holder.tvAddress.setText("Delivery to: " + (order.getDeliveryAddress() != null ? order.getDeliveryAddress() : "Shivajinagar, Pune"));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOrderClick(order);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderNumber, tvStatusBadge, tvStore, tvTotal, tvAddress;

        OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderNumber = itemView.findViewById(R.id.tvOrderNumber);
            tvStatusBadge = itemView.findViewById(R.id.tvOrderStatusBadge);
            tvStore = itemView.findViewById(R.id.tvOrderStore);
            tvTotal = itemView.findViewById(R.id.tvOrderTotal);
            tvAddress = itemView.findViewById(R.id.tvOrderAddress);
        }
    }
}
