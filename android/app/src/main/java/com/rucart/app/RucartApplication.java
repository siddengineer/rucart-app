package com.rucart.app;

import android.app.Application;
import com.rucart.app.util.CartManager;
import com.rucart.app.util.SessionManager;

public class RucartApplication extends Application {

    private static RucartApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        SessionManager.init(this);
        CartManager.init(this);
    }

    public static RucartApplication getInstance() {
        return instance;
    }
}
