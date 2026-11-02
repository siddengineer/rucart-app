package com.rucart.app.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.rucart.app.R;
import com.rucart.app.model.AuditLog;

import java.util.ArrayList;
import java.util.List;

public class AuditLogAdapter extends RecyclerView.Adapter<AuditLogAdapter.AuditViewHolder> {

    private List<AuditLog> logs = new ArrayList<>();

    public void setLogs(List<AuditLog> list) {
        this.logs = (list != null) ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AuditViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_audit_log, parent, false);
        return new AuditViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AuditViewHolder holder, int position) {
        AuditLog log = logs.get(position);
        holder.tvAuditAction.setText(log.getAction());
        holder.tvAuditTarget.setText("Target: " + log.getTargetType() + " #" + (log.getTargetId() != null ? log.getTargetId() : ""));
        holder.tvAuditActor.setText("By: " + log.getActor() + " (" + log.getActorRole() + ")");
        holder.tvAuditDetails.setText(log.getDetails() != null ? log.getDetails() : "Administrative operation");
        holder.tvAuditTimestamp.setText(log.getTimestamp() != null ? log.getTimestamp().replace('T', ' ') : "");

        holder.tvAuditResultBadge.setText(log.getResult() != null ? log.getResult() : "SUCCESS");
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    static class AuditViewHolder extends RecyclerView.ViewHolder {
        TextView tvAuditAction, tvAuditResultBadge, tvAuditTarget, tvAuditActor, tvAuditDetails, tvAuditTimestamp;

        public AuditViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAuditAction = itemView.findViewById(R.id.tvAuditAction);
            tvAuditResultBadge = itemView.findViewById(R.id.tvAuditResultBadge);
            tvAuditTarget = itemView.findViewById(R.id.tvAuditTarget);
            tvAuditActor = itemView.findViewById(R.id.tvAuditActor);
            tvAuditDetails = itemView.findViewById(R.id.tvAuditDetails);
            tvAuditTimestamp = itemView.findViewById(R.id.tvAuditTimestamp);
        }
    }
}
