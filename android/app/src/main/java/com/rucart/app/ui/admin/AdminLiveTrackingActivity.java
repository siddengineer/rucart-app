package com.rucart.app.ui.admin;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.ValueEventListener;
import com.rucart.app.R;
import com.rucart.app.util.FirebaseTrackingManager;

import java.util.HashMap;
import java.util.Map;

public class AdminLiveTrackingActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private final Map<Long, Marker> riderMarkers = new HashMap<>();

    private TextView tvActiveRidersCount;
    private MaterialCardView cardRiderDetail;
    private TextView tvSelectedRiderName;
    private TextView tvSelectedRiderStatus;
    private TextView tvSelectedRiderOrder;

    private ValueEventListener allRidersListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_live_tracking);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        tvActiveRidersCount = findViewById(R.id.tvActiveRidersCount);
        cardRiderDetail = findViewById(R.id.cardRiderDetail);
        tvSelectedRiderName = findViewById(R.id.tvSelectedRiderName);
        tvSelectedRiderStatus = findViewById(R.id.tvSelectedRiderStatus);
        tvSelectedRiderOrder = findViewById(R.id.tvSelectedRiderOrder);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.adminMapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;
        this.googleMap.getUiSettings().setZoomControlsEnabled(true);
        this.googleMap.getUiSettings().setCompassEnabled(true);

        LatLng center = new LatLng(18.5204, 73.8567); // Pune
        this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 12f));

        this.googleMap.setOnMarkerClickListener(marker -> {
            cardRiderDetail.setVisibility(View.VISIBLE);
            tvSelectedRiderName.setText(marker.getTitle());
            tvSelectedRiderStatus.setText(marker.getSnippet());
            return false;
        });

        startListeningToAllRiders();
    }

    private void startListeningToAllRiders() {
        allRidersListener = FirebaseTrackingManager.getInstance().listenToAllActiveRiders(new FirebaseTrackingManager.AllRidersListener() {
            @Override
            public void onRiderUpdated(Long riderId, String riderName, double latitude, double longitude, String status, Long orderId) {
                runOnUiThread(() -> {
                    LatLng pos = new LatLng(latitude, longitude);
                    Marker marker = riderMarkers.get(riderId);

                    float hue = BitmapDescriptorFactory.HUE_AZURE;
                    if ("ACTIVE".equalsIgnoreCase(status) || "OUT_FOR_DELIVERY".equalsIgnoreCase(status)) {
                        hue = BitmapDescriptorFactory.HUE_GREEN;
                    } else if ("SLEEP".equalsIgnoreCase(status)) {
                        hue = BitmapDescriptorFactory.HUE_YELLOW;
                    } else if ("OFFLINE".equalsIgnoreCase(status)) {
                        hue = BitmapDescriptorFactory.HUE_RED;
                    }

                    String snippet = "Status: " + status + (orderId != null && orderId > 0 ? " | Order #" + orderId : " | Idle");

                    if (marker == null) {
                        if (googleMap != null) {
                            marker = googleMap.addMarker(new MarkerOptions()
                                    .position(pos)
                                    .title(riderName)
                                    .snippet(snippet)
                                    .icon(BitmapDescriptorFactory.defaultMarker(hue)));
                            riderMarkers.put(riderId, marker);
                        }
                    } else {
                        marker.setPosition(pos);
                        marker.setSnippet(snippet);
                    }

                    tvActiveRidersCount.setText("Live Riders: " + riderMarkers.size());
                });
            }

            @Override
            public void onError(String message) {
                // Ignore or log
            }
        });
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
        if (allRidersListener != null) {
            FirebaseTrackingManager.getInstance().stopListeningToAllRiders(allRidersListener);
        }
    }
}
