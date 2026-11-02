package com.rucart.app.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.rucart.app.R;
import com.rucart.app.ui.auth.LoginActivity;
import com.rucart.app.ui.auth.RoleSelectionActivity;
import com.rucart.app.util.AuthPromptDialog;
import com.rucart.app.util.SessionManager;

public class CustomerAccountFragment extends Fragment {

    private TextView tvAccountName, tvAccountPhone, tvLogout;
    private Button btnManageAddresses, btnPaymentHistory, btnAccountNotifications, btnSwitchRole;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customer_account, container, false);

        tvAccountName = view.findViewById(R.id.tvAccountName);
        tvAccountPhone = view.findViewById(R.id.tvAccountPhone);
        btnManageAddresses = view.findViewById(R.id.btnManageAddresses);
        btnPaymentHistory = view.findViewById(R.id.btnPaymentHistory);
        btnAccountNotifications = view.findViewById(R.id.btnAccountNotifications);
        btnSwitchRole = view.findViewById(R.id.btnSwitchRole);
        tvLogout = view.findViewById(R.id.tvLogout);

        setupListeners();
        updateUI();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    private void updateUI() {
        if (getContext() == null) return;
        SessionManager session = SessionManager.getInstance();

        if (session.isLoggedIn()) {
            tvAccountName.setText(session.getUserName());
            String phone = session.getUserPhone();
            tvAccountPhone.setText(phone != null && !phone.isEmpty() ? "+91 " + phone : "RUCART Member");
            tvLogout.setText("Log Out");
            tvLogout.setTextColor(getContext().getColor(R.color.danger_red));
        } else {
            tvAccountName.setText("Welcome to RUCART");
            tvAccountPhone.setText("Sign in to manage addresses, orders & account");
            tvLogout.setText("Log In / Sign Up");
            tvLogout.setTextColor(getContext().getColor(R.color.primary_green));
        }
    }

    private void setupListeners() {
        btnManageAddresses.setOnClickListener(v -> {
            if (!SessionManager.getInstance().isLoggedIn()) {
                AuthPromptDialog.showLoginRequired(getContext(), "manage saved delivery addresses");
                return;
            }
            startActivity(new Intent(getContext(), AddressManagementActivity.class));
        });

        btnPaymentHistory.setOnClickListener(v -> {
            if (!SessionManager.getInstance().isLoggedIn()) {
                AuthPromptDialog.showLoginRequired(getContext(), "view your payment & invoice history");
                return;
            }
            startActivity(new Intent(getContext(), TransactionHistoryActivity.class));
        });

        btnAccountNotifications.setOnClickListener(v -> {
            if (!SessionManager.getInstance().isLoggedIn()) {
                AuthPromptDialog.showLoginRequired(getContext(), "view order and account notifications");
                return;
            }
            startActivity(new Intent(getContext(), NotificationsActivity.class));
        });

        btnSwitchRole.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), RoleSelectionActivity.class));
            if (getActivity() != null) {
                getActivity().finish();
            }
        });

        tvLogout.setOnClickListener(v -> {
            SessionManager session = SessionManager.getInstance();
            if (session.isLoggedIn()) {
                session.clear();
                Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getContext(), RoleSelectionActivity.class));
                if (getActivity() != null) {
                    getActivity().finish();
                }
            } else {
                Intent intent = new Intent(getContext(), LoginActivity.class);
                intent.putExtra(LoginActivity.EXTRA_RETURN_TO_CALLER, true);
                startActivity(intent);
            }
        });
    }
}
