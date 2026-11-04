package com.rucart.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rucart.backend.dto.Msg91VerifyRequest;
import com.rucart.backend.dto.SendOtpRequest;
import com.rucart.backend.dto.VerifyOtpRequest;
import com.rucart.backend.repository.UserRepository;
import com.rucart.backend.service.Msg91Service;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class Msg91AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Msg91Service msg91Service;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("MSG91 configuration endpoint returns widgetId 366b646c4b74353831343330")
    public void testGetMsg91Config() throws Exception {
        mockMvc.perform(get("/api/v1/auth/msg91/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.widgetId").value("366b646c4b74353831343330"));
    }

    @Test
    @DisplayName("MSG91 token verification logs in or registers customer and returns JWT Bearer token")
    public void testMsg91TokenVerification() throws Exception {
        String testPhone = "9822001122";
        Msg91VerifyRequest req = new Msg91VerifyRequest("mock_msg91_jwt_access_token", testPhone, "OTP Customer", "CUSTOMER");

        mockMvc.perform(post("/api/v1/auth/msg91/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.user.phone").value(testPhone));

        assertTrue(userRepository.findByPhone(testPhone).isPresent());
    }

    @Test
    @DisplayName("Direct OTP send and verify flow completes successfully")
    public void testDirectOtpFlow() throws Exception {
        String phone = "9822334455";

        // 1. Send OTP
        mockMvc.perform(post("/api/v1/auth/otp/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SendOtpRequest(phone))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 2. We can simulate sending OTP with fixed code in test
        msg91Service.sendOtpSms(phone, "789123", "Test OTP");

        // 3. Verify OTP
        VerifyOtpRequest verifyReq = new VerifyOtpRequest(phone, "789123", "Direct OTP User", "CUSTOMER");
        mockMvc.perform(post("/api/v1/auth/otp/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.user.phone").value(phone));
    }
}
