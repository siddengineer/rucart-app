package com.rucart.app.ui.customer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rucart.app.R;
import com.rucart.app.model.Payment;

import java.util.ArrayList;
import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private final Context context;
    private List<Payment> paymentList = new ArrayList<>();

    public TransactionAdapter(Context context) {
        this.context = context;
    }

    public void setPayments(List<Payment> payments) {
        this.paymentList = payments != null ? payments : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Payment payment = paymentList.get(position);

        String orderNum = payment.getOrder() != null && payment.getOrder().getOrderNumber() != null ?
                payment.getOrder().getOrderNumber() : "Order #" + payment.getId();
        holder.tvTxnOrderNumber.setText(orderNum);

        String date = payment.getPaidAt() != null ? payment.getPaidAt() : (payment.getCreatedAt() != null ? payment.getCreatedAt() : "Recently");
        holder.tvTxnDate.setText(date);

        String ref = payment.getGatewayPaymentId() != null ? payment.getGatewayPaymentId() :
                (payment.getTransactionId() != null ? payment.getTransactionId() : "N/A");
        holder.tvTxnReference.setText("Ref: " + ref);

        holder.tvTxnMethod.setText("Method: " + (payment.getPaymentMethod() != null ? payment.getPaymentMethod() : "ONLINE"));
        holder.tvTxnAmount.setText("₹" + (payment.getAmount() != null ? payment.getAmount().toPlainString() : "0.00"));

        String status = payment.getPaymentStatus() != null ? payment.getPaymentStatus() : "PENDING";
        holder.tvTxnStatusBadge.setText(status);

        switch (status) {
            case "COMPLETED":
                holder.tvTxnStatusBadge.setBackgroundResource(R.drawable.bg_badge_green);
                holder.tvTxnStatusBadge.setTextColor(Color.parseColor("#1B5E20"));
                holder.btnTxnRefundDetails.setVisibility(View.GONE);
                holder.btnTxnDownloadPdf.setVisibility(View.VISIBLE);
                break;
            case "REFUNDED":
            case "PARTIALLY_REFUNDED":
                holder.tvTxnStatusBadge.setBackgroundResource(R.drawable.bg_badge_purple);
                holder.tvTxnStatusBadge.setTextColor(Color.parseColor("#7B1FA2"));
                holder.btnTxnRefundDetails.setVisibility(View.VISIBLE);
                holder.btnTxnDownloadPdf.setVisibility(View.VISIBLE);
                break;
            case "FAILED":
                holder.tvTxnStatusBadge.setBackgroundResource(R.drawable.bg_badge_red);
                holder.tvTxnStatusBadge.setTextColor(Color.parseColor("#B71C1C"));
                holder.btnTxnRefundDetails.setVisibility(View.GONE);
                holder.btnTxnDownloadPdf.setVisibility(View.GONE);
                break;
            case "CANCELLED":
                holder.tvTxnStatusBadge.setBackgroundResource(R.drawable.bg_badge_orange);
                holder.tvTxnStatusBadge.setTextColor(Color.parseColor("#E65100"));
                holder.btnTxnRefundDetails.setVisibility(View.GONE);
                holder.btnTxnDownloadPdf.setVisibility(View.GONE);
                break;
            default:
                holder.tvTxnStatusBadge.setBackgroundResource(R.drawable.bg_badge_blue);
                holder.tvTxnStatusBadge.setTextColor(Color.parseColor("#0D47A1"));
                holder.btnTxnRefundDetails.setVisibility(View.GONE);
                holder.btnTxnDownloadPdf.setVisibility(View.VISIBLE);
                break;
        }

        holder.btnTxnDownloadPdf.setOnClickListener(v -> {
            Intent intent = new Intent(context, ReceiptViewActivity.class);
            intent.putExtra(ReceiptViewActivity.EXTRA_PAYMENT_ID, payment.getId());
            if (payment.getOrder() != null) {
                intent.putExtra(ReceiptViewActivity.EXTRA_ORDER_ID, payment.getOrder().getId());
            }
            context.startActivity(intent);
        });

        holder.btnTxnRefundDetails.setOnClickListener(v -> {
            Intent intent = new Intent(context, RefundStatusActivity.class);
            intent.putExtra(RefundStatusActivity.EXTRA_PAYMENT, payment);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return paymentList.size();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvTxnOrderNumber, tvTxnDate, tvTxnStatusBadge, tvTxnReference, tvTxnMethod, tvTxnAmount;
        Button btnTxnDownloadPdf, btnTxnRefundDetails;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTxnOrderNumber = itemView.findViewById(R.id.tvTxnOrderNumber);
            tvTxnDate = itemView.findViewById(R.id.tvTxnDate);
            tvTxnStatusBadge = itemView.findViewById(R.id.tvTxnStatusBadge);
            tvTxnReference = itemView.findViewById(R.id.tvTxnReference);
            tvTxnMethod = itemView.findViewById(R.id.tvTxnMethod);
            tvTxnAmount = itemView.findViewById(R.id.tvTxnAmount);
            btnTxnDownloadPdf = itemView.findViewById(R.id.btnTxnDownloadPdf);
            btnTxnRefundDetails = itemView.findViewById(R.id.btnTxnRefundDetails);
        }
    }
}
