package com.rucart.app.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rucart.app.util.Constants;
import com.rucart.app.util.SessionManager;
import android.os.Build;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

public class ApiClient {

    private static Retrofit retrofit = null;
    private static ApiService apiService = null;

    public static boolean isEmulator() {
        return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.PRODUCT.contains("sdk_google")
                || Build.PRODUCT.contains("google_sdk")
                || Build.PRODUCT.contains("sdk")
                || Build.PRODUCT.contains("sdk_x86")
                || Build.PRODUCT.contains("vbox86p")
                || Build.PRODUCT.contains("emulator")
                || Build.PRODUCT.contains("simulator");
    }

    public static String getBaseUrl() {
        if (SessionManager.getInstance() != null) {
            String custom = SessionManager.getInstance().getCustomBaseUrl();
            if (custom != null && !custom.trim().isEmpty()) {
                String trimmed = custom.trim();
                return trimmed.endsWith("/") ? trimmed : trimmed + "/";
            }
        }
        return isEmulator() ? Constants.EMULATOR_BASE_URL : Constants.DEVICE_BASE_URL;
    }

    public static String getBaseHost() {
        HttpUrl parsed = HttpUrl.parse(getBaseUrl());
        return parsed != null ? parsed.host() + ":" + parsed.port() : getBaseUrl();
    }

    public static synchronized void resetApiClient() {
        apiService = null;
        retrofit = null;
    }

    public static synchronized ApiService getApiService() {
        if (apiService == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        Request.Builder builder = original.newBuilder();

                        String currentBaseUrl = getBaseUrl();
                        HttpUrl newBase = HttpUrl.parse(currentBaseUrl);
                        if (newBase != null) {
                            HttpUrl oldUrl = original.url();
                            HttpUrl updatedUrl = oldUrl.newBuilder()
                                    .scheme(newBase.scheme())
                                    .host(newBase.host())
                                    .port(newBase.port())
                                    .build();
                            builder.url(updatedUrl);
                        }

                        if (SessionManager.getInstance() != null) {
                            String token = SessionManager.getInstance().getToken();
                            if (token != null && !token.isBlank()) {
                                builder.header("Authorization", "Bearer " + token);
                            }
                        }
                        return chain.proceed(builder.build());
                    })
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .build();

            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(getBaseUrl())
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();

            apiService = retrofit.create(ApiService.class);
        }
        return apiService;
    }
}
