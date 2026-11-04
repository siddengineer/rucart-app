package com.rucart.app.ui.admin;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.rucart.app.R;
import com.rucart.app.api.ApiClient;
import com.rucart.app.model.ApiResponse;
import com.rucart.app.model.InvoiceDto;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminInvoicesActivity extends AppCompatActivity {

    private SwipeRefreshLayout swipeRefreshInvoices;
    private RecyclerView rvInvoices;
    private EditText etSearchInvoices;
    private ChipGroup chipGroupInvoiceFilter;

    private InvoiceAdapter adapter;
    private List<InvoiceDto> invoiceList = new ArrayList<>();

    private String currentMethodFilter = null;
    private String currentStatusFilter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_invoices);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        swipeRefreshInvoices = findViewById(R.id.swipeRefreshInvoices);
        rvInvoices = findViewById(R.id.rvInvoices);
        etSearchInvoices = findViewById(R.id.etSearchInvoices);
        chipGroupInvoiceFilter = findViewById(R.id.chipGroupInvoiceFilter);

        adapter = new InvoiceAdapter();
        rvInvoices.setLayoutManager(new LinearLayoutManager(this));
        rvInvoices.setAdapter(adapter);

        setupListeners();
        loadInvoices(null, null, null);
    }

    private void setupListeners() {
        swipeRefreshInvoices.setOnRefreshListener(() -> loadInvoices(etSearchInvoices.getText().toString().trim(), currentMethodFilter, currentStatusFilter));

        etSearchInvoices.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadInvoices(s.toString().trim(), currentMethodFilter, currentStatusFilter);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        chipGroupInvoiceFilter.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipCOD) {
                currentMethodFilter = "COD";
                currentStatusFilter = null;
            } else if (checkedId == R.id.chipQR) {
                currentMethodFilter = "QR";
                currentStatusFilter = null;
            } else if (checkedId == R.id.chipPendingVerif) {
                currentMethodFilter = null;
                currentStatusFilter = "PENDING_VERIFICATION";
            } else if (checkedId == R.id.chipPaid) {
                currentMethodFilter = null;
                currentStatusFilter = "COMPLETED";
            } else {
                currentMethodFilter = null;
                currentStatusFilter = null;
            }
            loadInvoices(etSearchInvoices.getText().toString().trim(), currentMethodFilter, currentStatusFilter);
        });
    }

    private void loadInvoices(String search, String pMethod, String pStatus) {
        swipeRefreshInvoices.setRefreshing(true);
        ApiClient.getApiService().getAllInvoices(search, pMethod, pStatus).enqueue(new Callback<ApiResponse<List<InvoiceDto>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<InvoiceDto>>> call, Response<ApiResponse<List<InvoiceDto>>> response) {
                swipeRefreshInvoices.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    invoiceList = response.body().getData();
                    adapter.setInvoices(invoiceList);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<InvoiceDto>>> call, Throwable t) {
                swipeRefreshInvoices.setRefreshing(false);
                Toast.makeText(AdminInvoicesActivity.this, "Failed to load invoices", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showInvoiceDetailsDialog(InvoiceDto invoice) {
        StringBuilder sb = new StringBuilder();
        sb.append("Invoice: ").append(invoice.getInvoiceNumber()).append("\n");
        sb.append("Order ID: #").append(invoice.getOrderNumber()).append("\n");
        sb.append("Store: ").append(invoice.getStoreName()).append("\n");
        sb.append("Customer: ").append(invoice.getCustomerName()).append(" (").append(invoice.getCustomerPhone()).append(")\n");
        sb.append("Address: ").append(invoice.getDeliveryAddress()).append("\n\n");

        sb.append("--- Items Ordered ---\n");
        if (invoice.getItems() != null && !invoice.getItems().isEmpty()) {
            for (InvoiceDto.InvoiceItemDto item : invoice.getItems()) {
                sb.append("• ").append(item.getProductName())
                        .append(" x").append(item.getQuantity())
                        .append(" @ ₹").append(String.format("%.2f", item.getUnitPrice()))
                        .append(" = ₹").append(String.format("%.2f", item.getTotalPrice()))
                        .append("\n");
            }
        } else {
            sb.append("Standard grocery cart items\n");
        }

        sb.append("\nSubtotal: ₹").append(String.format("%.2f", invoice.getSubtotal())).append("\n");
        sb.append("Delivery Charges: ₹").append(String.format("%.2f", invoice.getDeliveryFee())).append("\n");
        sb.append("Final Grand Total: ₹").append(String.format("%.2f", invoice.getGrandTotal())).append("\n\n");

        sb.append("Payment Method: ").append(invoice.getPaymentMethodDisplay()).append("\n");
        sb.append("Payment Status: ").append(invoice.getPaymentStatusDisplay()).append("\n");
        sb.append("Order Date: ").append(invoice.getOrderDate() != null ? invoice.getOrderDate() : "Recently").append("\n");

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Invoice Details")
                .setMessage(sb.toString())
                .setPositiveButton("Download PDF", (dialog, which) -> downloadInvoicePdf(invoice.getOrderId()))
                .setNegativeButton("Close", null);

        if (!"COMPLETED".equalsIgnoreCase(invoice.getPaymentStatus()) && !"Paid".equalsIgnoreCase(invoice.getPaymentStatusDisplay())) {
            builder.setNeutralButton("Confirm Payment", (dialog, which) -> confirmPayment(invoice.getOrderId()));
        }

        builder.show();
    }

    private void confirmPayment(Long orderId) {
        ApiClient.getApiService().confirmInvoicePayment(orderId).enqueue(new Callback<ApiResponse<InvoiceDto>>() {
            @Override
            public void onResponse(Call<ApiResponse<InvoiceDto>> call, Response<ApiResponse<InvoiceDto>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdminInvoicesActivity.this, "Payment confirmed and marked as Paid!", Toast.LENGTH_SHORT).show();
                    loadInvoices(etSearchInvoices.getText().toString().trim(), currentMethodFilter, currentStatusFilter);
                } else {
                    Toast.makeText(AdminInvoicesActivity.this, "Could not confirm payment", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<InvoiceDto>> call, Throwable t) {
                Toast.makeText(AdminInvoicesActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void downloadInvoicePdf(Long orderId) {
        String pdfUrl = ApiClient.getBaseUrl() + "admin/invoices/" + orderId + "/pdf";
        try {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(pdfUrl));
            startActivity(browserIntent);
        } catch (Exception e) {
            Toast.makeText(this, "Opening PDF: " + pdfUrl, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class InvoiceAdapter extends RecyclerView.Adapter<InvoiceAdapter.ViewHolder> {
        private List<InvoiceDto> items = new ArrayList<>();

        void setInvoices(List<InvoiceDto> list) {
            this.items = list != null ? list : new ArrayList<>();
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_invoice, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            InvoiceDto inv = items.get(position);
            holder.tvInvoiceNum.setText(inv.getInvoiceNumber() != null ? inv.getInvoiceNumber() : "INV-" + inv.getOrderNumber());
            holder.tvInvoiceCustomer.setText("Customer: " + (inv.getCustomerName() != null ? inv.getCustomerName() : "Customer") + " • " + (inv.getCustomerPhone() != null ? inv.getCustomerPhone() : ""));
            holder.tvInvoicePaymentMethod.setText("Method: " + inv.getPaymentMethodDisplay());
            holder.tvInvoiceTotal.setText("Total: ₹ " + String.format("%.2f", inv.getGrandTotal()));
            holder.tvInvoiceDate.setText("Date: " + (inv.getOrderDate() != null ? inv.getOrderDate() : "Recently"));

            String pDisplay = inv.getPaymentStatusDisplay();
            holder.tvInvoiceStatusBadge.setText(pDisplay != null ? pDisplay.toUpperCase() : "PENDING");

            if ("Paid".equalsIgnoreCase(pDisplay) || "COMPLETED".equalsIgnoreCase(inv.getPaymentStatus())) {
                holder.tvInvoiceStatusBadge.setBackgroundColor(getResources().getColor(R.color.primary_green));
                holder.btnInvoiceConfirmPay.setVisibility(View.GONE);
            } else if ("Pending Verification".equalsIgnoreCase(pDisplay)) {
                holder.tvInvoiceStatusBadge.setBackgroundColor(getResources().getColor(R.color.accent_orange));
                holder.btnInvoiceConfirmPay.setVisibility(View.VISIBLE);
            } else {
                holder.tvInvoiceStatusBadge.setBackgroundColor(getResources().getColor(R.color.status_pending));
                holder.btnInvoiceConfirmPay.setVisibility(View.VISIBLE);
            }

            holder.btnInvoiceDetails.setOnClickListener(v -> showInvoiceDetailsDialog(inv));
            holder.btnInvoiceConfirmPay.setOnClickListener(v -> confirmPayment(inv.getOrderId()));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvInvoiceNum, tvInvoiceStatusBadge, tvInvoiceCustomer, tvInvoicePaymentMethod, tvInvoiceDate, tvInvoiceTotal;
            MaterialButton btnInvoiceConfirmPay, btnInvoiceDetails;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvInvoiceNum = itemView.findViewById(R.id.tvInvoiceNum);
                tvInvoiceStatusBadge = itemView.findViewById(R.id.tvInvoiceStatusBadge);
                tvInvoiceCustomer = itemView.findViewById(R.id.tvInvoiceCustomer);
                tvInvoicePaymentMethod = itemView.findViewById(R.id.tvInvoicePaymentMethod);
                tvInvoiceDate = itemView.findViewById(R.id.tvInvoiceDate);
                tvInvoiceTotal = itemView.findViewById(R.id.tvInvoiceTotal);
                btnInvoiceConfirmPay = itemView.findViewById(R.id.btnInvoiceConfirmPay);
                btnInvoiceDetails = itemView.findViewById(R.id.btnInvoiceDetails);
            }
        }
    }
}
