package com.rucart.app.ui.customer.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.rucart.app.R;
import com.rucart.app.model.CustomerAddress;

import java.util.ArrayList;
import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {

    private List<CustomerAddress> addresses = new ArrayList<>();
    private Long selectedAddressId = null;
    private final AddressActionsListener listener;

    public interface AddressActionsListener {
        void onAddressSelected(CustomerAddress address);
        void onAddressEdit(CustomerAddress address);
        void onAddressDelete(CustomerAddress address);
    }

    public AddressAdapter(AddressActionsListener listener) {
        this.listener = listener;
    }

    public void setAddresses(List<CustomerAddress> addresses, Long selectedId) {
        this.addresses = addresses != null ? addresses : new ArrayList<>();
        this.selectedAddressId = selectedId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_address, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        CustomerAddress address = addresses.get(position);

        holder.tvAddressTitle.setText(address.getTitle() != null ? address.getTitle().toUpperCase() : "HOME");
        holder.tvRecipient.setText(address.getRecipientName() != null ? address.getRecipientName() : "Customer");
        holder.tvFullAddress.setText(address.getFormattedAddress());
        holder.tvAddressPhone.setText("Phone: " + (address.getPhoneNumber() != null ? address.getPhoneNumber() : ""));

        boolean isSelected = selectedAddressId != null && selectedAddressId.equals(address.getId());
        holder.rbSelectAddress.setChecked(isSelected);

        View.OnClickListener clickListener = v -> {
            selectedAddressId = address.getId();
            notifyDataSetChanged();
            if (listener != null) {
                listener.onAddressSelected(address);
            }
        };

        holder.itemView.setOnClickListener(clickListener);
        holder.btnDeliverHere.setOnClickListener(clickListener);

        holder.btnEditAddress.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddressEdit(address);
            }
        });

        holder.btnDeleteAddress.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddressDelete(address);
            }
        });
    }

    @Override
    public int getItemCount() {
        return addresses.size();
    }

    static class AddressViewHolder extends RecyclerView.ViewHolder {
        TextView tvAddressTitle, tvRecipient, tvFullAddress, tvAddressPhone;
        RadioButton rbSelectAddress;
        Button btnDeliverHere, btnEditAddress, btnDeleteAddress;

        AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAddressTitle = itemView.findViewById(R.id.tvAddressTitle);
            tvRecipient = itemView.findViewById(R.id.tvRecipient);
            tvFullAddress = itemView.findViewById(R.id.tvFullAddress);
            tvAddressPhone = itemView.findViewById(R.id.tvAddressPhone);
            rbSelectAddress = itemView.findViewById(R.id.rbSelectAddress);
            btnDeliverHere = itemView.findViewById(R.id.btnDeliverHere);
            btnEditAddress = itemView.findViewById(R.id.btnEditAddress);
            btnDeleteAddress = itemView.findViewById(R.id.btnDeleteAddress);
        }
    }
}
