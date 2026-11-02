package com.rucart.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rucart.app.model.CartItem;
import com.rucart.app.model.Product;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartManager {

    private static CartManager instance;
    private final SharedPreferences prefs;
    private final Map<Long, CartItem> items = new HashMap<>();
    private final List<CartListener> listeners = new ArrayList<>();
    private final Gson gson = new Gson();

    public interface CartListener {
        void onCartUpdated(int totalCount, BigDecimal grandTotal);
    }

    private CartManager(Context context) {
        this.prefs = context != null ? context.getApplicationContext().getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE) : null;
        restoreCartForCurrentUser();
    }

    public static synchronized void init(Context context) {
        if (instance == null) {
            instance = new CartManager(context.getApplicationContext());
        }
    }

    public static synchronized CartManager getInstance() {
        return instance;
    }

    public void addListener(CartListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(CartListener listener) {
        listeners.remove(listener);
    }

    public int getQuantity(Long productId) {
        if (productId == null || !items.containsKey(productId)) return 0;
        return items.get(productId).getQuantity();
    }

    public boolean addProduct(Product product) {
        if (product == null || product.getId() == null) return false;
        int maxStock = product.getStock() != null ? product.getStock() : 99;
        return addProduct(product, maxStock);
    }

    public boolean addProduct(Product product, int maxStock) {
        if (product == null || product.getId() == null) return false;

        if (items.containsKey(product.getId())) {
            CartItem item = items.get(product.getId());
            if (item.getQuantity() >= maxStock) {
                return false; // Reached stock limit
            }
            item.setQuantity(item.getQuantity() + 1);
        } else {
            if (maxStock <= 0) return false;
            items.put(product.getId(), new CartItem(product, 1));
        }
        persistCart();
        notifyListeners();
        return true;
    }

    public void decrementProduct(Product product) {
        if (product == null || product.getId() == null) return;

        if (items.containsKey(product.getId())) {
            CartItem item = items.get(product.getId());
            if (item.getQuantity() > 1) {
                item.setQuantity(item.getQuantity() - 1);
            } else {
                items.remove(product.getId());
            }
            persistCart();
            notifyListeners();
        }
    }

    public void removeProduct(Long productId) {
        if (productId != null && items.containsKey(productId)) {
            items.remove(productId);
            persistCart();
            notifyListeners();
        }
    }

    public List<CartItem> getCartItems() {
        return new ArrayList<>(items.values());
    }

    public int getItemCount() {
        int total = 0;
        for (CartItem item : items.values()) {
            total += item.getQuantity();
        }
        return total;
    }

    public BigDecimal getCartTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : items.values()) {
            if (item.getProduct() != null && item.getProduct().getPrice() != null) {
                total = total.add(item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            }
        }
        return total;
    }

    public void clearCart() {
        items.clear();
        persistCart();
        notifyListeners();
    }

    public void clearMemoryCartOnLogout() {
        items.clear();
        notifyListeners();
    }

    public void restoreCartForCurrentUser() {
        if (prefs == null || SessionManager.getInstance() == null || !SessionManager.getInstance().isLoggedIn()) {
            items.clear();
            notifyListeners();
            return;
        }
        Long userId = SessionManager.getInstance().getUserId();
        String json = prefs.getString("key_saved_cart_" + userId, null);
        if (json != null && !json.isEmpty()) {
            try {
                Type type = new TypeToken<List<CartItem>>() {}.getType();
                List<CartItem> saved = gson.fromJson(json, type);
                items.clear();
                if (saved != null) {
                    for (CartItem ci : saved) {
                        if (ci.getProduct() != null && ci.getProduct().getId() != null) {
                            items.put(ci.getProduct().getId(), ci);
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }
        notifyListeners();
    }

    private void persistCart() {
        if (prefs == null || SessionManager.getInstance() == null || !SessionManager.getInstance().isLoggedIn()) {
            return;
        }
        Long userId = SessionManager.getInstance().getUserId();
        String json = gson.toJson(new ArrayList<>(items.values()));
        prefs.edit().putString("key_saved_cart_" + userId, json).apply();
    }

    private void notifyListeners() {
        int count = getItemCount();
        BigDecimal total = getCartTotal();
        for (CartListener listener : listeners) {
            listener.onCartUpdated(count, total);
        }
    }
}
