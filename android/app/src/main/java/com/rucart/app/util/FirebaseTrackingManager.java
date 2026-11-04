package com.rucart.app.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class FirebaseTrackingManager {

    private static final String TAG = "FirebaseTracking";
    private static FirebaseTrackingManager instance;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private DatabaseReference deliveriesRef;
    private DatabaseReference ridersRef;

    private Long activeOrderId;
    private Long activeRiderId;
    private String activeRiderName;

    public interface LocationUpdateListener {
        void onLocationReceived(double latitude, double longitude, String status, long timestamp);
        void onError(String message);
    }

    public interface AllRidersListener {
        void onRiderUpdated(Long riderId, String riderName, double latitude, double longitude, String status, Long orderId);
        void onError(String message);
    }

    private FirebaseTrackingManager() {
        try {
            FirebaseDatabase db = FirebaseDatabase.getInstance();
            deliveriesRef = db.getReference("deliveries");
            ridersRef = db.getReference("riders");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase Database reference: " + e.getMessage());
        }
    }

    public static synchronized FirebaseTrackingManager getInstance() {
        if (instance == null) {
            instance = new FirebaseTrackingManager();
        }
        return instance;
    }

    @SuppressLint("MissingPermission")
    public void startRiderTracking(Context context, Long orderId, Long riderId, String riderName) {
        stopRiderTracking(); // Stop any existing session first

        this.activeOrderId = orderId;
        this.activeRiderId = riderId;
        this.activeRiderName = riderName != null ? riderName : "Delivery Partner";

        if (context == null) return;

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context.getApplicationContext());

        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateIntervalMillis(3000)
                .setMinUpdateDistanceMeters(3)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location lastLoc = locationResult.getLastLocation();
                if (lastLoc != null) {
                    publishLocation(lastLoc.getLatitude(), lastLoc.getLongitude());
                }
            }
        };

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
            Log.d(TAG, "Rider location tracking started for order: " + orderId);
        } catch (SecurityException se) {
            Log.e(TAG, "Location permission not granted: " + se.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Error starting location updates: " + e.getMessage());
        }
    }

    public void publishLocation(double latitude, double longitude) {
        long now = System.currentTimeMillis();

        if (deliveriesRef != null && activeOrderId != null) {
            Map<String, Object> deliveryData = new HashMap<>();
            deliveryData.put("orderId", activeOrderId);
            deliveryData.put("riderId", activeRiderId);
            deliveryData.put("riderName", activeRiderName);
            deliveryData.put("latitude", latitude);
            deliveryData.put("longitude", longitude);
            deliveryData.put("status", "OUT_FOR_DELIVERY");
            deliveryData.put("timestamp", now);

            deliveriesRef.child(String.valueOf(activeOrderId)).setValue(deliveryData);
        }

        if (ridersRef != null && activeRiderId != null) {
            Map<String, Object> riderData = new HashMap<>();
            riderData.put("riderId", activeRiderId);
            riderData.put("riderName", activeRiderName);
            riderData.put("latitude", latitude);
            riderData.put("longitude", longitude);
            riderData.put("status", "ACTIVE");
            riderData.put("activeOrderId", activeOrderId != null ? activeOrderId : 0L);
            riderData.put("timestamp", now);

            ridersRef.child(String.valueOf(activeRiderId)).setValue(riderData);
        }
    }

    public void stopRiderTracking() {
        if (fusedLocationClient != null && locationCallback != null) {
            try {
                fusedLocationClient.removeLocationUpdates(locationCallback);
            } catch (Exception e) {
                Log.e(TAG, "Error removing location updates: " + e.getMessage());
            }
            locationCallback = null;
        }

        if (deliveriesRef != null && activeOrderId != null) {
            deliveriesRef.child(String.valueOf(activeOrderId)).child("status").setValue("COMPLETED");
        }

        if (ridersRef != null && activeRiderId != null) {
            ridersRef.child(String.valueOf(activeRiderId)).child("status").setValue("OFFLINE");
        }

        activeOrderId = null;
        activeRiderId = null;
        Log.d(TAG, "Rider location tracking stopped");
    }

    public ValueEventListener listenToOrderDelivery(Long orderId, LocationUpdateListener listener) {
        if (deliveriesRef == null || orderId == null) {
            if (listener != null) listener.onError("Firebase tracking not initialized");
            return null;
        }

        ValueEventListener valListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    if (listener != null) listener.onError("Rider has not started sharing live location yet.");
                    return;
                }

                Double lat = snapshot.child("latitude").getValue(Double.class);
                Double lng = snapshot.child("longitude").getValue(Double.class);
                String status = snapshot.child("status").getValue(String.class);
                Long ts = snapshot.child("timestamp").getValue(Long.class);

                if (lat != null && lng != null && listener != null) {
                    listener.onLocationReceived(lat, lng, status != null ? status : "OUT_FOR_DELIVERY", ts != null ? ts : System.currentTimeMillis());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (listener != null) listener.onError(error.getMessage());
            }
        };

        deliveriesRef.child(String.valueOf(orderId)).addValueEventListener(valListener);
        return valListener;
    }

    public void stopListeningToOrder(Long orderId, ValueEventListener listener) {
        if (deliveriesRef != null && orderId != null && listener != null) {
            deliveriesRef.child(String.valueOf(orderId)).removeEventListener(listener);
        }
    }

    public ValueEventListener listenToAllActiveRiders(AllRidersListener listener) {
        if (ridersRef == null) {
            if (listener != null) listener.onError("Firebase tracking not initialized");
            return null;
        }

        ValueEventListener valListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    try {
                        Long riderId = child.child("riderId").getValue(Long.class);
                        String name = child.child("riderName").getValue(String.class);
                        Double lat = child.child("latitude").getValue(Double.class);
                        Double lng = child.child("longitude").getValue(Double.class);
                        String status = child.child("status").getValue(String.class);
                        Long orderId = child.child("activeOrderId").getValue(Long.class);

                        if (riderId != null && lat != null && lng != null && listener != null) {
                            listener.onRiderUpdated(riderId, name != null ? name : "Rider #" + riderId,
                                    lat, lng, status != null ? status : "ACTIVE", orderId);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing rider live data: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (listener != null) listener.onError(error.getMessage());
            }
        };

        ridersRef.addValueEventListener(valListener);
        return valListener;
    }

    public void stopListeningToAllRiders(ValueEventListener listener) {
        if (ridersRef != null && listener != null) {
            ridersRef.removeEventListener(listener);
        }
    }
}
