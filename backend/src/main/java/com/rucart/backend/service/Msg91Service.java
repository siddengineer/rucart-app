package com.rucart.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class Msg91Service {

    private static final Logger log = LoggerFactory.getLogger(Msg91Service.class);

    @Value("${msg91.auth.key:}")
    private String authKey;

    @Value("${msg91.widget.id:366b646c4b74353831343330}")
    private String widgetId;

    @Value("${msg91.sender.id:RUCART}")
    private String senderId;

    @Value("${msg91.template.id:}")
    private String templateId;

    @Value("${msg91.verify.access.token.url:https://control.msg91.com/api/v5/widget/verifyAccessToken}")
    private String verifyAccessTokenUrl;

    @Value("${msg91.otp.url:https://control.msg91.com/api/v5/otp}")
    private String otpUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    // In-memory OTP storage for direct OTP requests with timestamp expiration (5 mins)
    private final Map<String, OtpEntry> otpCache = new ConcurrentHashMap<>();

    private static class OtpEntry {
        final String otp;
        final long expiresAt;

        OtpEntry(String otp, long expiresAt) {
            this.otp = otp;
            this.expiresAt = expiresAt;
        }
    }

    public String getWidgetId() {
        return widgetId;
    }

    public boolean isConfigured() {
        return authKey != null && !authKey.isBlank();
    }

    /**
     * Verifies the access-token returned by the MSG91 OTP Widget on frontend.
     * Uses MSG91 endpoint: POST https://control.msg91.com/api/v5/widget/verifyAccessToken
     */
    public boolean verifyAccessToken(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return false;
        }

        // Sandbox / Local fallback if AuthKey is not yet set
        if (!isConfigured()) {
            log.warn("[MSG91 SANDBOX] MSG91_AUTH_KEY is not set. Allowing access token in sandbox mode.");
            return true;
        }

        try {
            Map<String, String> requestBody = Map.of(
                    "authkey", authKey.trim(),
                    "access-token", accessToken.trim()
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(verifyAccessTokenUrl))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(12))
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                JsonNode root = objectMapper.readTree(response.body());
                String type = root.path("type").asText("");
                String message = root.path("message").asText("");

                log.info("MSG91 verifyAccessToken response: code={}, type={}, msg={}", response.statusCode(), type, message);

                if ("success".equalsIgnoreCase(type) || response.body().contains("success")) {
                    return true;
                }
            }

            log.warn("MSG91 verifyAccessToken rejected: HTTP {} - {}", response.statusCode(), response.body());
            return false;
        } catch (Exception e) {
            log.error("Failed to call MSG91 verifyAccessToken API", e);
            return false;
        }
    }

    /**
     * Sends OTP SMS to the specified mobile number (e.g. for delivery verification code).
     */
    public boolean sendOtpSms(String mobile, String otp, String purpose) {
        if (mobile == null || mobile.isBlank() || otp == null || otp.isBlank()) {
            return false;
        }

        String formattedMobile = formatMobileNumber(mobile);

        // Store in local cache (valid for 5 minutes)
        otpCache.put(cleanDigitsOnly(mobile), new OtpEntry(otp, System.currentTimeMillis() + 300_000));

        if (!isConfigured()) {
            log.info("================================================================================");
            log.info("🔔 [MSG91 OTP NOTIFICATION] To: {} | Code: {} | Purpose: {}", formattedMobile, otp, purpose);
            log.info("ℹ️ To deliver live SMS, configure MSG91_AUTH_KEY in application.properties or environment");
            log.info("================================================================================");
            return true;
        }

        try {
            StringBuilder urlBuilder = new StringBuilder(otpUrl);
            urlBuilder.append("?mobile=").append(formattedMobile);
            urlBuilder.append("&otp=").append(URLEncoder.encode(otp, StandardCharsets.UTF_8));
            urlBuilder.append("&authkey=").append(URLEncoder.encode(authKey.trim(), StandardCharsets.UTF_8));

            if (templateId != null && !templateId.isBlank()) {
                urlBuilder.append("&template_id=").append(URLEncoder.encode(templateId.trim(), StandardCharsets.UTF_8));
            }
            if (senderId != null && !senderId.isBlank()) {
                urlBuilder.append("&sender=").append(URLEncoder.encode(senderId.trim(), StandardCharsets.UTF_8));
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlBuilder.toString()))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("MSG91 OTP SMS successfully dispatched to {}: {}", formattedMobile, response.body());
                return true;
            } else {
                log.warn("MSG91 OTP SMS error HTTP {}: {}", response.statusCode(), response.body());
                return false;
            }
        } catch (Exception e) {
            log.error("Error dispatching MSG91 OTP SMS to " + formattedMobile, e);
            return false;
        }
    }

    /**
     * Validates a locally generated or direct OTP.
     */
    public boolean verifyDirectOtp(String mobile, String enteredOtp) {
        if (mobile == null || enteredOtp == null) {
            return false;
        }
        String cleanPhone = cleanDigitsOnly(mobile);
        OtpEntry entry = otpCache.get(cleanPhone);

        if (entry == null) {
            return false;
        }

        if (System.currentTimeMillis() > entry.expiresAt) {
            otpCache.remove(cleanPhone);
            return false;
        }

        boolean valid = entry.otp.equals(enteredOtp.trim());
        if (valid) {
            otpCache.remove(cleanPhone);
        }
        return valid;
    }

    private String formatMobileNumber(String mobile) {
        String digits = cleanDigitsOnly(mobile);
        if (digits.length() == 10) {
            return "91" + digits; // Default India prefix for MSG91
        }
        return digits;
    }

    private String cleanDigitsOnly(String phone) {
        return phone.replaceAll("[^0-9]", "");
    }
}
