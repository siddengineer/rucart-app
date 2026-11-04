package com.rucart.app.ui.customer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.ValueEventListener;
import com.rucart.app.R;
import com.rucart.app.util.FirebaseTrackingManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LiveTrackingActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String EXTRA_ORDER_ID = "extra_order_id";
    public static final String EXTRA_ORDER_NUMBER = "extra_order_number";
    public static final String EXTRA_RIDER_NAME = "extra_rider_name";
    public static final String EXTRA_RIDER_PHONE = "extra_rider_phone";
    public static final String EXTRA_DELIVERY_ADDRESS = "extra_delivery_address";
    public static final String EXTRA_DEST_LAT = "extra_dest_lat";
    public static final String EXTRA_DEST_LNG = "extra_dest_lng";

    private GoogleMap googleMap;
    private Marker riderMarker;
    private Marker destinationMarker;

    private Long orderId;
    private String orderNumber;
    private String riderName;
    private String riderPhone;
    private String deliveryAddress;
    private double destLat = 18.5204;
    private double destLng = 73.8567;

    private TextView tvOrderNumber;
    private TextView tvDeliveryStatus;
    private TextView tvRiderName;
    private TextView tvLastUpdated;
    private TextView tvCustomerAddress;
    private MaterialButton btnCallRider;

    private ValueEventListener trackingListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_tracking);

        orderId = getIntent().getLongExtra(EXTRA_ORDER_ID, 0L);
        orderNumber = getIntent().getStringExtra(EXTRA_ORDER_NUMBER);
        riderName = getIntent().getStringExtra(EXTRA_RIDER_NAME);
        riderPhone = getIntent().getStringExtra(EXTRA_RIDER_PHONE);
        deliveryAddress = getIntent().getStringExtra(EXTRA_DELIVERY_ADDRESS);
        destLat = getIntent().getDoubleExtra(EXTRA_DEST_LAT, 18.5204);
        destLng = getIntent().getDoubleExtra(EXTRA_DEST_LNG, 73.8567);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        tvOrderNumber = findViewById(R.id.tvOrderNumber);
        tvDeliveryStatus = findViewById(R.id.tvDeliveryStatus);
        tvRiderName = findViewById(R.id.tvRiderName);
        tvLastUpdated = findViewById(R.id.tvLastUpdated);
        tvCustomerAddress = findViewById(R.id.tvCustomerAddress);
        btnCallRider = findViewById(R.id.btnCallRider);

        if (orderNumber != null && !orderNumber.isEmpty()) {
            tvOrderNumber.setText("Order #" + orderNumber);
        } else {
            tvOrderNumber.setText("Order #" + orderId);
        }

        if (riderName != null && !riderName.isEmpty()) {
            tvRiderName.setText("Rider: " + riderName);
        } else {
            tvRiderName.setText("Rider: Assigned Partner");
        }

        if (deliveryAddress != null && !deliveryAddress.isEmpty()) {
            tvCustomerAddress.setText("Delivering to: " + deliveryAddress);
        }

        if (riderPhone != null && !riderPhone.isEmpty()) {
            btnCallRider.setOnClickListener(v -> {
                Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + riderPhone));
                startActivity(callIntent);
            });
        } else {
            btnCallRider.setEnabled(false);
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;
        this.googleMap.getUiSettings().setZoomControlsEnabled(true);
        this.googleMap.getUiSettings().setCompassEnabled(true);

        LatLng destLatLng = new LatLng(destLat, destLng);
        destinationMarker = googleMap.addMarker(new MarkerOptions()
                .position(destLatLng)
                .title("Delivery Destination")
                .snippet(deliveryAddress != null ? deliveryAddress : "Your Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destLatLng, 14f));

        startListeningToLiveTracking();
    }

    private void startListeningToLiveTracking() {
        if (orderId == null || orderId <= 0) {
            tvLastUpdated.setText("No active delivery tracking found.");
            return;
        }

        trackingListener = FirebaseTrackingManager.getInstance().listenToOrderDelivery(orderId, new FirebaseTrackingManager.LocationUpdateListener() {
            @Override
            public void onLocationReceived(double latitude, double longitude, String status, long timestamp) {
                runOnUiThread(() -> updateRiderOnMap(latitude, longitude, status, timestamp));
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    tvLastUpdated.setText(message != null ? message : "Rider location not available");
                });
            }
        });
    }

    private void updateRiderOnMap(double latitude, double longitude, String status, long timestamp) {
        LatLng riderPos = new LatLng(latitude, longitude);

        if (riderMarker == null) {
            if (googleMap != null) {
                riderMarker = googleMap.addMarker(new MarkerOptions()
                        .position(riderPos)
                        .title(riderName != null ? riderName : "Delivery Rider")
                        .snippet("Out for delivery")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            }
        } else {
            riderMarker.setPosition(riderPos);
        }

        String timeStr = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(new Date(timestamp));
        tvLastUpdated.setText("Live location updated at " + timeStr);

        if ("COMPLETED".equalsIgnoreCase(status) || "DELIVERED".equalsIgnoreCase(status)) {
            tvDeliveryStatus.setText("Order has been delivered!");
        } else {
            tvDeliveryStatus.setText("Delivery partner is on the way!");
        }

        // Adjust camera to show both destination and rider
        if (googleMap != null && destinationMarker != null) {
            try {
                LatLngBounds bounds = new LatLngBounds.Builder()
                        .include(destinationMarker.getPosition())
                        .include(riderPos)
                        .build();
                googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 120));
            } catch (Exception e) {
                googleMap.animateCamera(CameraUpdateFactory.newLatLng(riderPos));
            }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (trackingListener != null && orderId != null) {
            FirebaseTrackingManager.getInstance().stopListeningToOrder(orderId, trackingListener);
        }
    }
}
