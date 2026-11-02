package com.rucart.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.rucart.app.model.CustomerAddress;

public class SessionManager {

    private static SessionManager instance;
    private final SharedPreferences prefs;

    private static final String KEY_SELECTED_ADDR = "key_selected_address";
    private static final String KEY_SELECTED_ADDR_ID = "key_selected_address_id";
    private static final String KEY_SELECTED_ADDR_JSON = "key_selected_address_json";
    private static final String KEY_SELECTED_AREA = "key_selected_service_area";
    private static final String KEY_SELECTED_STORE_ID = "key_selected_store_id";
    private static final String KEY_SELECTED_STORE_NAME = "key_selected_store_name";

    private SessionManager(Context context) {
        this.prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized void init(Context context) {
        if (instance == null) {
            instance = new SessionManager(context.getApplicationContext());
        }
    }

    public static synchronized SessionManager getInstance() {
        return instance;
    }

    public void saveUserSession(Long userId, String name, String phone, String role, String token) {
        prefs.edit()
                .putLong(Constants.KEY_USER_ID, userId != null ? userId : 1L)
                .putString(Constants.KEY_USER_NAME, name)
                .putString(Constants.KEY_USER_PHONE, phone)
                .putString(Constants.KEY_USER_ROLE, role)
                .putString(Constants.KEY_TOKEN, token)
                .putBoolean(Constants.KEY_LOGGED_IN, true)
                .apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(Constants.KEY_LOGGED_IN, false);
    }

    public Long getUserId() {
        if (!isLoggedIn()) return null;
        long id = prefs.getLong(Constants.KEY_USER_ID, -1L);
        return id > 0 ? id : null;
    }

    public String getUserName() {
        if (!isLoggedIn()) return "Guest User";
        return prefs.getString(Constants.KEY_USER_NAME, "Customer");
    }

    public String getUserPhone() {
        if (!isLoggedIn()) return "";
        return prefs.getString(Constants.KEY_USER_PHONE, "");
    }

    public String getUserRole() {
        if (!isLoggedIn()) return "GUEST";
        return prefs.getString(Constants.KEY_USER_ROLE, "GUEST");
    }

    public String getToken() {
        return prefs.getString(Constants.KEY_TOKEN, null);
    }

    public String getSelectedAddress() {
        return prefs.getString(KEY_SELECTED_ADDR, "Flat 402, Green Avenue, Shivajinagar, Pune");
    }

    public CustomerAddress getSelectedCustomerAddress() {
        String json = prefs.getString(KEY_SELECTED_ADDR_JSON, null);
        if (json != null) {
            try {
                return new Gson().fromJson(json, CustomerAddress.class);
            } catch (Exception ignored) {}
        }
        String addrStr = getSelectedAddress();
        Long addrId = getSelectedAddressId();
        if (addrStr != null) {
            CustomerAddress fallback = new CustomerAddress();
            fallback.setId(addrId != null ? addrId : 1L);
            fallback.setAddressLine1(addrStr);
            fallback.setCity("Pune");
            return fallback;
        }
        return null;
    }

    public void setSelectedAddress(String address, Long addressId) {
        prefs.edit()
                .putString(KEY_SELECTED_ADDR, address)
                .putLong(KEY_SELECTED_ADDR_ID, addressId != null ? addressId : -1L)
                .apply();
    }

    public void setSelectedAddress(CustomerAddress address) {
        if (address != null) {
            String formatted = address.getFormattedAddress();
            if (formatted == null || formatted.trim().isEmpty()) {
                formatted = address.getAddressLine1();
            }
            setSelectedAddress(formatted, address.getId());
            if (address.getAddressLine2() != null && !address.getAddressLine2().trim().isEmpty()) {
                setSelectedServiceArea(address.getAddressLine2());
            }
            prefs.edit().putString(KEY_SELECTED_ADDR_JSON, new Gson().toJson(address)).apply();
        }
    }

    public Long getSelectedAddressId() {
        long id = prefs.getLong(KEY_SELECTED_ADDR_ID, -1L);
        return id > 0 ? id : null;
    }

    public String getSelectedServiceArea() {
        return prefs.getString(KEY_SELECTED_AREA, "Shivajinagar, Pune");
    }

    public void setSelectedServiceArea(String area) {
        prefs.edit().putString(KEY_SELECTED_AREA, area).apply();
    }

    public Long getSelectedStoreId() {
        long id = prefs.getLong(KEY_SELECTED_STORE_ID, 1L);
        return id > 0 ? id : 1L;
    }

    public void setSelectedStore(Long storeId, String storeName) {
        prefs.edit()
                .putLong(KEY_SELECTED_STORE_ID, storeId != null ? storeId : 1L)
                .putString(KEY_SELECTED_STORE_NAME, storeName)
                .apply();
    }

    public String getSelectedStoreName() {
        return prefs.getString(KEY_SELECTED_STORE_NAME, "RUCART Express Store");
    }

    public String getCustomBaseUrl() {
        return prefs.getString("key_custom_base_url", null);
    }

    public void setCustomBaseUrl(String url) {
        prefs.edit().putString("key_custom_base_url", url).apply();
    }

    public void clear() {
        prefs.edit().clear().apply();
        if (CartManager.getInstance() != null) {
            CartManager.getInstance().clearMemoryCartOnLogout();
        }
    }
    public void clearSession() {
        clear();
    }
}
