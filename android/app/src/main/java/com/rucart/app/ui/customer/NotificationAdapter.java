package com.rucart.app.ui.customer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rucart.app.R;
import com.rucart.app.api.ApiClient;
import com.rucart.app.model.ApiResponse;
import com.rucart.app.model.Notification;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private final Context context;
    private List<Notification> notificationList = new ArrayList<>();
    private final OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    public NotificationAdapter(Context context, OnNotificationClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notificationList = notifications != null ? notifications : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notificationList.get(position);

        holder.tvTitle.setText(notification.getTitle() != null ? notification.getTitle() : "Notification");
        holder.tvMessage.setText(notification.getMessage() != null ? notification.getMessage() : "");
        holder.tvTime.setText(notification.getCreatedAt() != null ? notification.getCreatedAt().replace("T", " ") : "Recently");

        boolean isRead = notification.isRead();
        if (isRead) {
            holder.tvUnreadBadge.setVisibility(View.GONE);
            holder.llContainer.setBackgroundColor(Color.WHITE);
            holder.tvTitle.setTextColor(Color.parseColor("#424242"));
        } else {
            holder.tvUnreadBadge.setVisibility(View.VISIBLE);
            holder.llContainer.setBackgroundColor(Color.parseColor("#F1F8E9"));
            holder.tvTitle.setTextColor(Color.parseColor("#1B5E20"));
        }

        String refId = notification.getReferenceId();
        if (refId != null && !refId.isBlank()) {
            holder.tvRef.setVisibility(View.VISIBLE);
            if (refId.startsWith("RUC")) {
                holder.tvRef.setText("View Order " + refId + " →");
            } else {
                holder.tvRef.setText("View Ref: " + refId + " →");
            }
        } else {
            holder.tvRef.setVisibility(View.GONE);
        }

        // Icon based on type
        String type = notification.getType() != null ? notification.getType() : "SYSTEM";
        if ("DELIVERY_ALERT".equals(type)) {
            holder.ivIcon.setImageResource(R.drawable.ic_delivery);
        } else if ("STORE_ALERT".equals(type)) {
            holder.ivIcon.setImageResource(R.drawable.ic_store);
        } else if ("ORDER_UPDATE".equals(type)) {
            holder.ivIcon.setImageResource(R.drawable.ic_orders);
        } else {
            holder.ivIcon.setImageResource(R.drawable.ic_notifications);
        }

        holder.itemView.setOnClickListener(v -> {
            if (!notification.isRead()) {
                notification.setRead(true);
                notifyItemChanged(position);
                // Mark as read in backend
                ApiClient.getApiService().markNotificationAsRead(notification.getId())
                        .enqueue(new Callback<ApiResponse<Notification>>() {
                            @Override
                            public void onResponse(Call<ApiResponse<Notification>> call, Response<ApiResponse<Notification>> response) {
                            }

                            @Override
                            public void onFailure(Call<ApiResponse<Notification>> call, Throwable t) {
                            }
                        });
            }

            if (listener != null) {
                listener.onNotificationClick(notification);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTitle, tvMessage, tvTime, tvUnreadBadge, tvRef;
        LinearLayout llContainer;

        NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivNotificationIcon);
            tvTitle = itemView.findViewById(R.id.tvNotificationTitle);
            tvMessage = itemView.findViewById(R.id.tvNotificationMessage);
            tvTime = itemView.findViewById(R.id.tvNotificationTime);
            tvUnreadBadge = itemView.findViewById(R.id.tvUnreadBadge);
            tvRef = itemView.findViewById(R.id.tvNotificationRef);
            llContainer = itemView.findViewById(R.id.llNotificationContainer);
        }
    }
}
