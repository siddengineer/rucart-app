package com.rucart.app.ui.customer;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.rucart.app.R;
import com.rucart.app.api.ApiClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReceiptViewActivity extends AppCompatActivity {

    public static final String EXTRA_ORDER_ID = "extra_order_id";
    public static final String EXTRA_PAYMENT_ID = "extra_payment_id";

    private Long orderId;
    private Long paymentId;
    private File savedPdfFile;

    private ImageView btnBack;
    private TextView tvReceiptStatusTitle, tvReceiptStatusSubtitle;
    private TextView tvReceiptFileName, tvReceiptFileSize, tvReceiptLocalPath;
    private LinearLayout llPdfDownloading, llPdfActions;
    private TextView tvPdfDownloadProgress;
    private Button btnOpenPdf, btnSharePdf, btnDoneReceipt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt_view);

        orderId = getIntent().hasExtra(EXTRA_ORDER_ID) ? getIntent().getLongExtra(EXTRA_ORDER_ID, -1L) : null;
        if (orderId != null && orderId == -1L) orderId = null;

        paymentId = getIntent().hasExtra(EXTRA_PAYMENT_ID) ? getIntent().getLongExtra(EXTRA_PAYMENT_ID, -1L) : null;
        if (paymentId != null && paymentId == -1L) paymentId = null;

        if (orderId == null && paymentId == null) {
            Toast.makeText(this, "Order or payment reference required", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupListeners();
        downloadReceiptPdf();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBackReceipt);
        tvReceiptStatusTitle = findViewById(R.id.tvReceiptStatusTitle);
        tvReceiptStatusSubtitle = findViewById(R.id.tvReceiptStatusSubtitle);
        tvReceiptFileName = findViewById(R.id.tvReceiptFileName);
        tvReceiptFileSize = findViewById(R.id.tvReceiptFileSize);
        tvReceiptLocalPath = findViewById(R.id.tvReceiptLocalPath);
        llPdfDownloading = findViewById(R.id.llPdfDownloading);
        llPdfActions = findViewById(R.id.llPdfActions);
        tvPdfDownloadProgress = findViewById(R.id.tvPdfDownloadProgress);
        btnOpenPdf = findViewById(R.id.btnOpenPdf);
        btnSharePdf = findViewById(R.id.btnSharePdf);
        btnDoneReceipt = findViewById(R.id.btnDoneReceipt);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnDoneReceipt.setOnClickListener(v -> finish());

        btnOpenPdf.setOnClickListener(v -> openPdfDocument());
        btnSharePdf.setOnClickListener(v -> sharePdfDocument());
    }

    private void downloadReceiptPdf() {
        llPdfDownloading.setVisibility(View.VISIBLE);
        llPdfActions.setVisibility(View.GONE);

        Call<ResponseBody> call;
        String fileName;

        if (orderId != null) {
            call = ApiClient.getApiService().downloadOrderReceiptPdf(orderId);
            fileName = "Rucart_Invoice_Order_" + orderId + ".pdf";
        } else {
            call = ApiClient.getApiService().downloadPaymentReceiptPdf(paymentId);
            fileName = "Rucart_Invoice_Payment_" + paymentId + ".pdf";
        }

        tvReceiptFileName.setText(fileName);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    savePdfToStorage(response.body(), fileName);
                } else {
                    llPdfDownloading.setVisibility(View.GONE);
                    Toast.makeText(ReceiptViewActivity.this, "Failed to download receipt: HTTP " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                llPdfDownloading.setVisibility(View.GONE);
                Toast.makeText(ReceiptViewActivity.this, "Network error downloading receipt: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void savePdfToStorage(ResponseBody body, String fileName) {
        new Thread(() -> {
            try {
                File dir = getExternalCacheDir() != null ? getExternalCacheDir() : getCacheDir();
                File pdfFile = new File(dir, fileName);

                try (InputStream inputStream = body.byteStream();
                     OutputStream outputStream = new FileOutputStream(pdfFile)) {

                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    outputStream.flush();
                }

                savedPdfFile = pdfFile;
                long fileSizeBytes = pdfFile.length();
                double sizeKb = fileSizeBytes / 1024.0;

                runOnUiThread(() -> {
                    llPdfDownloading.setVisibility(View.GONE);
                    llPdfActions.setVisibility(View.VISIBLE);
                    tvReceiptFileSize.setText(String.format(java.util.Locale.getDefault(), "%.1f KB", sizeKb));
                    tvReceiptLocalPath.setText(pdfFile.getAbsolutePath());
                    Toast.makeText(this, "Receipt downloaded successfully", Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    llPdfDownloading.setVisibility(View.GONE);
                    Toast.makeText(this, "Error saving receipt PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void openPdfDocument() {
        if (savedPdfFile == null || !savedPdfFile.exists()) {
            Toast.makeText(this, "PDF file not found", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Uri pdfUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", savedPdfFile);
            Intent viewIntent = new Intent(Intent.ACTION_VIEW);
            viewIntent.setDataAndType(pdfUri, "application/pdf");
            viewIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(viewIntent, "Open PDF Invoice"));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No PDF viewer app found on device", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Could not open PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void sharePdfDocument() {
        if (savedPdfFile == null || !savedPdfFile.exists()) {
            Toast.makeText(this, "PDF file not found", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Uri pdfUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", savedPdfFile);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, pdfUri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "RUCART Grocery Invoice");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Here is your official RUCART tax invoice & delivery receipt.");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Share Receipt"));
        } catch (Exception e) {
            Toast.makeText(this, "Could not share PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
