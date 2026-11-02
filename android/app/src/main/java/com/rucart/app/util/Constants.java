package com.rucart.app.util;

public final class Constants {
    private Constants() {}

    // Android Emulator host loopback address
    public static final String EMULATOR_BASE_URL = "http://10.0.2.2:8080/api/v1/";
    // Physical device loopback address (requires: adb reverse tcp:8080 tcp:8080)
    public static final String DEVICE_BASE_URL = "http://127.0.0.1:8080/api/v1/";
    public static final String BASE_URL = EMULATOR_BASE_URL;

    public static final String PREF_NAME = "rucart_preferences";
    public static final String KEY_USER_ID = "key_user_id";
    public static final String KEY_USER_NAME = "key_user_name";
    public static final String KEY_USER_PHONE = "key_user_phone";
    public static final String KEY_USER_ROLE = "key_user_role";
    public static final String KEY_TOKEN = "key_token";
    public static final String KEY_LOGGED_IN = "key_logged_in";

    public static final String DEFAULT_CITY = "Pune";
    public static final String DEFAULT_AREA = "Shivajinagar";
}
