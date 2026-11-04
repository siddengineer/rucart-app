package com.rucart.app.ui.auth;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.rucart.app.R;
import com.rucart.app.api.ApiClient;
import com.rucart.app.model.ApiResponse;
import com.rucart.app.model.AuthResponse;
import com.rucart.app.ui.customer.CustomerMainActivity;
import com.rucart.app.util.SessionManager;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Msg91OtpActivity extends AppCompatActivity {

    private static final String TAG = "Msg91OtpActivity";

    public static final String EXTRA_PHONE = "extra_phone";
    public static final String EXTRA_NAME = "extra_name";
    public static final String EXTRA_ROLE = "extra_role";

    public static final String WIDGET_ID = "366b646c4b74353831343330";

    private String userPhone;
    private String userName;
    private String userRole;

    private ImageView btnMsg91Back;
    private WebView wvMsg91Widget;
    private FrameLayout flMsg91Loading;
    private TextView tvMsg91Status;

    // Direct OTP Fallback UI
    private LinearLayout llDirectOtpFallback;
    private TextView tvDirectOtpSubtitle;
    private EditText etDirectOtpInput;
    private Button btnVerifyDirectOtp;
    private TextView btnResendDirectOtp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msg91_otp);

        userPhone = getIntent().getStringExtra(EXTRA_PHONE);
        userName = getIntent().getStringExtra(EXTRA_NAME);
        userRole = getIntent().getStringExtra(EXTRA_ROLE);
        if (userRole == null || userRole.isEmpty()) {
            userRole = "CUSTOMER";
        }

        initViews();
        setupListeners();
        setupWebView();
    }

    private void initViews() {
        btnMsg91Back = findViewById(R.id.btnMsg91Back);
        wvMsg91Widget = findViewById(R.id.wvMsg91Widget);
        flMsg91Loading = findViewById(R.id.flMsg91Loading);
        tvMsg91Status = findViewById(R.id.tvMsg91Status);

        llDirectOtpFallback = findViewById(R.id.llDirectOtpFallback);
        tvDirectOtpSubtitle = findViewById(R.id.tvDirectOtpSubtitle);
        etDirectOtpInput = findViewById(R.id.etDirectOtpInput);
        btnVerifyDirectOtp = findViewById(R.id.btnVerifyDirectOtp);
        btnResendDirectOtp = findViewById(R.id.btnResendDirectOtp);
    }

    private void setupListeners() {
        btnMsg91Back.setOnClickListener(v -> finish());

        btnVerifyDirectOtp.setOnClickListener(v -> verifyDirectOtpCode());

        btnResendDirectOtp.setOnClickListener(v -> resendDirectOtp());
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings settings = wvMsg91Widget.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);

        wvMsg91Widget.setWebViewClient(new WebViewClient());
        wvMsg91Widget.setWebChromeClient(new WebChromeClient());
        wvMsg91Widget.addJavascriptInterface(new Msg91Bridge(), "AndroidBridge");

        loadMsg91HtmlWidget();
    }

    private void loadMsg91HtmlWidget() {
        String phoneParam = (userPhone != null && !userPhone.isEmpty()) ? userPhone : "";

        String html = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "  <style>\n" +
                "    body { font-family: -apple-system, Roboto, sans-serif; margin: 0; padding: 16px; background-color: #F8F9FA; display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 80vh; }\n" +
                "    .fallback-btn { margin-top: 24px; padding: 10px 18px; background: #0B6E3F; color: white; border: none; border-radius: 8px; font-weight: bold; font-size: 14px; cursor: pointer; }\n" +
                "  </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "<script type=\"text/javascript\">\n" +
                "var configuration = {\n" +
                "  widgetId: \"" + WIDGET_ID + "\",\n" +
                "  tokenAuth: \"\",\n" +
                "  identifier: \"" + phoneParam + "\",\n" +
                "  exposeMethods: true,\n" +
                "  success: (data) => {\n" +
                "      console.log('MSG91 success response', data);\n" +
                "      if (window.AndroidBridge && window.AndroidBridge.onSuccess) {\n" +
                "          window.AndroidBridge.onSuccess(typeof data === 'string' ? data : JSON.stringify(data));\n" +
                "      }\n" +
                "  },\n" +
                "  failure: (error) => {\n" +
                "      console.log('MSG91 failure reason', error);\n" +
                "      if (window.AndroidBridge && window.AndroidBridge.onFailure) {\n" +
                "          window.AndroidBridge.onFailure(typeof error === 'string' ? error : JSON.stringify(error));\n" +
                "      }\n" +
                "  }\n" +
                "};\n" +
                "</script>\n" +
                "<script type=\"text/javascript\">\n" +
                "(function loadOtpScript(urls) {\n" +
                "    let i = 0;\n" +
                "    function attempt() {\n" +
                "        const s = document.createElement('script');\n" +
                "        s.src = urls[i];\n" +
                "        s.async = true;\n" +
                "        s.onload = () => {\n" +
                "            if (typeof window.initSendOTP === 'function') {\n" +
                "                window.initSendOTP(configuration);\n" +
                "            }\n" +
                "        };\n" +
                "        s.onerror = () => {\n" +
                "            i++;\n" +
                "            if (i < urls.length) {\n" +
                "                attempt();\n" +
                "            } else if (window.AndroidBridge && window.AndroidBridge.onScriptLoadFailed) {\n" +
                "                window.AndroidBridge.onScriptLoadFailed();\n" +
                "            }\n" +
                "        };\n" +
                "        document.head.appendChild(s);\n" +
                "    }\n" +
                "    attempt();\n" +
                "})([\n" +
                "    'https://verify.msg91.com/otp-provider.js',\n" +
                "    'https://verify.phone91.com/otp-provider.js'\n" +
                "]);\n" +
                "</script>\n" +
                "<button class=\"fallback-btn\" onclick=\"if(window.AndroidBridge) window.AndroidBridge.switchToDirectOtp();\">Switch to SMS Verification Code</button>\n" +
                "</body>\n" +
                "</html>";

        wvMsg91Widget.loadDataWithBaseURL("https://verify.msg91.com", html, "text/html", "UTF-8", null);
    }

    public class Msg91Bridge {
        @JavascriptInterface
        public void onSuccess(String data) {
            runOnUiThread(() -> handleMsg91WidgetSuccess(data));
        }

        @JavascriptInterface
        public void onFailure(String error) {
            runOnUiThread(() -> {
                Log.w(TAG, "MSG91 failure from widget: " + error);
                Toast.makeText(Msg91OtpActivity.this, "OTP verification failed. Please try again.", Toast.LENGTH_SHORT).show();
            });
        }

        @JavascriptInterface
        public void onScriptLoadFailed() {
            runOnUiThread(() -> {
                Log.w(TAG, "Failed to load remote MSG91 scripts. Falling back to direct SMS OTP.");
                activateDirectOtpFallback();
            });
        }

        @JavascriptInterface
        public void switchToDirectOtp() {
            runOnUiThread(() -> activateDirectOtpFallback());
        }
    }

    private void handleMsg91WidgetSuccess(String data) {
        showLoading("Verifying MSG91 access token...");

        String accessToken = data;
        try {
            if (data != null && data.trim().startsWith("{")) {
                JSONObject obj = new JSONObject(data);
                if (obj.has("token")) {
                    accessToken = obj.getString("token");
                } else if (obj.has("message")) {
                    accessToken = obj.getString("message");
                }
            }
        } catch (Exception ignored) {}

        Map<String, Object> body = new HashMap<>();
        body.put("accessToken", accessToken);
        body.put("phone", userPhone);
        body.put("name", userName);
        body.put("role", userRole);

        ApiClient.getApiService().verifyMsg91Token(body).enqueue(new Callback<ApiResponse<AuthResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AuthResponse>> call, Response<ApiResponse<AuthResponse>> response) {
                hideLoading();
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    onAuthenticationSuccess(response.body().getData());
                } else {
                    String err = response.body() != null ? response.body().getMessage() : "Verification failed";
                    Toast.makeText(Msg91OtpActivity.this, err, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AuthResponse>> call, Throwable t) {
                hideLoading();
                Toast.makeText(Msg91OtpActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void activateDirectOtpFallback() {
        wvMsg91Widget.setVisibility(View.GONE);
        llDirectOtpFallback.setVisibility(View.VISIBLE);

        if (userPhone != null && !userPhone.isEmpty()) {
            tvDirectOtpSubtitle.setText("Enter the 6-digit OTP code sent to " + userPhone);
            resendDirectOtp();
        } else {
            tvDirectOtpSubtitle.setText("Please enter the verification code received on your mobile");
        }
    }

    private void resendDirectOtp() {
        if (userPhone == null || userPhone.trim().isEmpty()) {
            Toast.makeText(this, "Phone number is not provided", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading("Sending OTP via MSG91 SMS...");
        Map<String, Object> body = new HashMap<>();
        body.put("phone", userPhone);

        ApiClient.getApiService().sendOtp(body).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
                hideLoading();
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(Msg91OtpActivity.this, "OTP sent successfully to " + userPhone, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Msg91OtpActivity.this, "Failed to send OTP", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                hideLoading();
                Toast.makeText(Msg91OtpActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void verifyDirectOtpCode() {
        String otp = etDirectOtpInput.getText().toString().trim();
        if (otp.length() < 4) {
            etDirectOtpInput.setError("Please enter the full OTP code");
            etDirectOtpInput.requestFocus();
            return;
        }

        showLoading("Verifying OTP code...");
        Map<String, Object> body = new HashMap<>();
        body.put("phone", userPhone);
        body.put("otp", otp);
        body.put("name", userName);
        body.put("role", userRole);

        ApiClient.getApiService().verifyOtp(body).enqueue(new Callback<ApiResponse<AuthResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AuthResponse>> call, Response<ApiResponse<AuthResponse>> response) {
                hideLoading();
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    onAuthenticationSuccess(response.body().getData());
                } else {
                    String err = response.body() != null ? response.body().getMessage() : "Invalid OTP code";
                    Toast.makeText(Msg91OtpActivity.this, err, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AuthResponse>> call, Throwable t) {
                hideLoading();
                Toast.makeText(Msg91OtpActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onAuthenticationSuccess(AuthResponse auth) {
        if (auth != null) {
            SessionManager.getInstance().saveUserSession(
                    auth.getUserId(),
                    auth.getName(),
                    auth.getPhone(),
                    auth.getRole() != null ? auth.getRole().name() : "CUSTOMER",
                    auth.getToken()
            );
        }

        Toast.makeText(this, "Phone verified successfully! Welcome to RUCART.", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(Msg91OtpActivity.this, CustomerMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showLoading(String message) {
        tvMsg91Status.setText(message);
        flMsg91Loading.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        flMsg91Loading.setVisibility(View.GONE);
    }
}
