package com.rucart.app.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;
import com.rucart.app.R;
import com.rucart.app.model.User;

import java.util.ArrayList;
import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.UserViewHolder> {

    public interface UserActionListener {
        void onToggleStatus(User user);
        void onViewDetails(User user);
    }

    private List<User> users = new ArrayList<>();
    private final UserActionListener listener;

    public AdminUserAdapter(UserActionListener listener) {
        this.listener = listener;
    }

    public void setUsers(List<User> list) {
        this.users = (list != null) ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User u = users.get(position);
        holder.tvAdminUserName.setText(u.getName());
        holder.tvAdminUserContact.setText("+91 " + u.getPhone() + (u.getEmail() != null ? " • " + u.getEmail() : ""));
        holder.tvAdminUserAddress.setText("Address: " + (u.getAddress() != null ? u.getAddress() : "Not provided"));

        String role = (u.getRole() != null) ? u.getRole().name() : "CUSTOMER";
        holder.tvAdminUserRoleBadge.setText(role);

        if (u.isActive()) {
            holder.tvAdminUserStatusBadge.setText("ACTIVE");
            holder.tvAdminUserStatusBadge.setTextColor(holder.itemView.getContext().getColor(R.color.accent_green));
            holder.btnToggleUserStatus.setText("Suspend Account");
            holder.btnToggleUserStatus.setTextColor(holder.itemView.getContext().getColor(R.color.accent_red));
        } else {
            holder.tvAdminUserStatusBadge.setText("SUSPENDED");
            holder.tvAdminUserStatusBadge.setTextColor(holder.itemView.getContext().getColor(R.color.accent_red));
            holder.btnToggleUserStatus.setText("Reactivate User");
            holder.btnToggleUserStatus.setTextColor(holder.itemView.getContext().getColor(R.color.accent_green));
        }

        holder.btnToggleUserStatus.setOnClickListener(v -> listener.onToggleStatus(u));
        holder.btnViewUserDetails.setOnClickListener(v -> listener.onViewDetails(u));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvAdminUserName, tvAdminUserContact, tvAdminUserRoleBadge, tvAdminUserStatusBadge, tvAdminUserAddress;
        AppCompatButton btnToggleUserStatus, btnViewUserDetails;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAdminUserName = itemView.findViewById(R.id.tvAdminUserName);
            tvAdminUserContact = itemView.findViewById(R.id.tvAdminUserContact);
            tvAdminUserRoleBadge = itemView.findViewById(R.id.tvAdminUserRoleBadge);
            tvAdminUserStatusBadge = itemView.findViewById(R.id.tvAdminUserStatusBadge);
            tvAdminUserAddress = itemView.findViewById(R.id.tvAdminUserAddress);
            btnToggleUserStatus = itemView.findViewById(R.id.btnToggleUserStatus);
            btnViewUserDetails = itemView.findViewById(R.id.btnViewUserDetails);
        }
    }
}
