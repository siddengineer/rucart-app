package com.rucart.backend.controller;

import com.rucart.backend.dto.ApiResponse;
import com.rucart.backend.dto.CustomerAddressRequest;
import com.rucart.backend.model.CustomerAddress;
import com.rucart.backend.security.CustomUserDetails;
import com.rucart.backend.service.CustomerAddressService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customer/addresses")
public class CustomerAddressController {

    private final CustomerAddressService addressService;

    public CustomerAddressController(CustomerAddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerAddress>>> getAddresses(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<CustomerAddress> addresses = addressService.getUserAddresses(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.ok(addresses));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerAddress>> addAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CustomerAddressRequest request) {
        CustomerAddress address = addressService.addAddress(userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Address added successfully", address));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerAddress>> updateAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody CustomerAddressRequest request) {
        CustomerAddress address = addressService.updateAddress(userDetails.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.ok("Address updated successfully", address));
    }

    @PatchMapping("/{id}/default")
    public ResponseEntity<ApiResponse<CustomerAddress>> setDefaultAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        CustomerAddress address = addressService.setDefaultAddress(userDetails.getId(), id);
        return ResponseEntity.ok(ApiResponse.ok("Default address updated", address));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        addressService.deleteAddress(userDetails.getId(), id);
        return ResponseEntity.ok(ApiResponse.ok("Address deleted successfully", null));
    }
}
