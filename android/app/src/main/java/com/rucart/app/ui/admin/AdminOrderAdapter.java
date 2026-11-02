package com.rucart.app.ui.admin;

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

public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.OrderViewHolder> {

    public interface OrderActionListener {
        void onViewOrder(Order order);
        void onOverrideStatus(Order order);
    }

    private List<Order> orders = new ArrayList<>();
    private final OrderActionListener listener;

    public AdminOrderAdapter(OrderActionListener listener) {
        this.listener = listener;
    }

    public void setOrders(List<Order> list) {
        this.orders = (list != null) ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order o = orders.get(position);
        holder.tvAdminOrderNumber.setText("#" + o.getOrderNumber());
        holder.tvAdminOrderCustomer.setText("Customer: " + (o.getCustomerName() != null ? o.getCustomerName() : "Guest")
                + (o.getCustomerPhone() != null ? " (" + o.getCustomerPhone() + ")" : ""));
        holder.tvAdminOrderStore.setText("Store: " + (o.getStoreName() != null ? o.getStoreName() : (o.getStore() != null ? o.getStore().getName() : "Direct")));
        holder.tvAdminOrderAddress.setText("Delivery: " + (o.getDeliveryAddress() != null ? o.getDeliveryAddress() : "Address not specified"));
        holder.tvAdminOrderTotal.setText("₹" + o.getGrandTotal());
        holder.tvAdminOrderPaymentMethod.setText(o.getPaymentMethod() != null ? o.getPaymentMethod().replace('_', ' ') : "COD");

        String status = o.getStatus() != null ? o.getStatus(): "PLACED";
        holder.tvAdminOrderStatusBadge.setText(status.replace('_', ' '));

        int badgeColor;
        switch (status) {
            case "DELIVERED":
                badgeColor = holder.itemView.getContext().getColor(R.color.accent_green);
                break;
            case "CANCELLED":
            case "REFUNDED":
                badgeColor = holder.itemView.getContext().getColor(R.color.accent_red);
                break;
            case "OUT_FOR_DELIVERY":
            case "READY_FOR_PICKUP":
                badgeColor = holder.itemView.getContext().getColor(R.color.accent_orange);
                break;
            default:
                badgeColor = holder.itemView.getContext().getColor(R.color.primary_green);
                break;
        }
        holder.tvAdminOrderStatusBadge.setTextColor(badgeColor);

        holder.btnViewOrderDetails.setOnClickListener(v -> listener.onViewOrder(o));
        holder.btnAdminOverrideStatus.setOnClickListener(v -> listener.onOverrideStatus(o));
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvAdminOrderNumber, tvAdminOrderStatusBadge, tvAdminOrderCustomer, tvAdminOrderStore;
        TextView tvAdminOrderAddress, tvAdminOrderTotal, tvAdminOrderPaymentMethod;
        AppCompatButton btnViewOrderDetails, btnAdminOverrideStatus;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAdminOrderNumber = itemView.findViewById(R.id.tvAdminOrderNumber);
            tvAdminOrderStatusBadge = itemView.findViewById(R.id.tvAdminOrderStatusBadge);
            tvAdminOrderCustomer = itemView.findViewById(R.id.tvAdminOrderCustomer);
            tvAdminOrderStore = itemView.findViewById(R.id.tvAdminOrderStore);
            tvAdminOrderAddress = itemView.findViewById(R.id.tvAdminOrderAddress);
            tvAdminOrderTotal = itemView.findViewById(R.id.tvAdminOrderTotal);
            tvAdminOrderPaymentMethod = itemView.findViewById(R.id.tvAdminOrderPaymentMethod);
            btnViewOrderDetails = itemView.findViewById(R.id.btnViewOrderDetails);
            btnAdminOverrideStatus = itemView.findViewById(R.id.btnAdminOverrideStatus);
        }
    }
}
