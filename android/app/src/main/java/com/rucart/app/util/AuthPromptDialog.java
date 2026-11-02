package com.rucart.app.util;

import android.content.Context;
import android.content.Intent;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.rucart.app.ui.auth.LoginActivity;
import com.rucart.app.ui.auth.RegisterActivity;

public class AuthPromptDialog {

    public static void showLoginRequired(Context context, String actionDescription) {
        if (context == null) return;

        String message = "Please log in or sign up to " +
                (actionDescription != null ? actionDescription : "continue") +
                " and explore fresh groceries with fast 10-15 minute delivery.";

        new MaterialAlertDialogBuilder(context)
                .setTitle("Sign in to RUCART")
                .setMessage(message)
                .setPositiveButton("Log In", (dialog, which) -> {
                    Intent intent = new Intent(context, LoginActivity.class);
                    intent.putExtra(LoginActivity.EXTRA_RETURN_TO_CALLER, true);
                    context.startActivity(intent);
                })
                .setNeutralButton("Sign Up", (dialog, which) -> {
                    Intent intent = new Intent(context, RegisterActivity.class);
                    context.startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
