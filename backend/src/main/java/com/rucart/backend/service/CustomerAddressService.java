package com.rucart.backend.service;

import com.rucart.backend.dto.CustomerAddressRequest;
import com.rucart.backend.model.CustomerAddress;
import com.rucart.backend.model.User;
import com.rucart.backend.repository.CustomerAddressRepository;
import com.rucart.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerAddressService {

    private final CustomerAddressRepository addressRepository;
    private final UserRepository userRepository;

    public CustomerAddressService(CustomerAddressRepository addressRepository, UserRepository userRepository) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    public List<CustomerAddress> getUserAddresses(Long userId) {
        return addressRepository.findByUserId(userId);
    }

    public CustomerAddress getDefaultAddress(Long userId) {
        return addressRepository.findByUserIdAndIsDefaultTrue(userId)
                .orElseGet(() -> {
                    List<CustomerAddress> addresses = addressRepository.findByUserId(userId);
                    return addresses.isEmpty() ? null : addresses.get(0);
                });
    }

    @Transactional
    public CustomerAddress addAddress(Long userId, CustomerAddressRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // If this address is set as default, reset others
        if (request.isDefault()) {
            resetDefaultAddresses(userId);
        }

        CustomerAddress address = new CustomerAddress();
        address.setUser(user);
        address.setTitle(request.getTitle() != null ? request.getTitle() : "Home");
        address.setRecipientName(request.getRecipientName() != null ? request.getRecipientName() : user.getName());
        address.setPhoneNumber(request.getPhoneNumber() != null ? request.getPhoneNumber() : user.getPhone());
        address.setAddressLine1(request.getAddressLine1());
        address.setAddressLine2(request.getAddressLine2());
        address.setLandmark(request.getLandmark());
        address.setCity(request.getCity() != null ? request.getCity() : "Pune");
        address.setState(request.getState() != null ? request.getState() : "Maharashtra");
        address.setPostalCode(request.getPostalCode() != null ? request.getPostalCode() : "411005");
        address.setLatitude(request.getLatitude());
        address.setLongitude(request.getLongitude());
        address.setDefault(request.isDefault());

        CustomerAddress saved = addressRepository.save(address);

        // Update user's summary address
        user.setAddress(saved.getFormattedAddress());
        userRepository.save(user);

        return saved;
    }

    @Transactional
    public CustomerAddress updateAddress(Long userId, Long addressId, CustomerAddressRequest request) {
        CustomerAddress address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new RuntimeException("Address not found or unauthorized: " + addressId));

        if (request.isDefault() && !address.isDefault()) {
            resetDefaultAddresses(userId);
        }

        if (request.getTitle() != null) address.setTitle(request.getTitle());
        if (request.getRecipientName() != null) address.setRecipientName(request.getRecipientName());
        if (request.getPhoneNumber() != null) address.setPhoneNumber(request.getPhoneNumber());
        if (request.getAddressLine1() != null) address.setAddressLine1(request.getAddressLine1());
        if (request.getAddressLine2() != null) address.setAddressLine2(request.getAddressLine2());
        if (request.getLandmark() != null) address.setLandmark(request.getLandmark());
        if (request.getCity() != null) address.setCity(request.getCity());
        if (request.getState() != null) address.setState(request.getState());
        if (request.getPostalCode() != null) address.setPostalCode(request.getPostalCode());
        if (request.getLatitude() != null) address.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) address.setLongitude(request.getLongitude());
        address.setDefault(request.isDefault());

        CustomerAddress saved = addressRepository.save(address);

        if (saved.isDefault()) {
            User user = address.getUser();
            user.setAddress(saved.getFormattedAddress());
            userRepository.save(user);
        }

        return saved;
    }

    @Transactional
    public CustomerAddress setDefaultAddress(Long userId, Long addressId) {
        resetDefaultAddresses(userId);
        CustomerAddress address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new RuntimeException("Address not found: " + addressId));
        address.setDefault(true);
        CustomerAddress saved = addressRepository.save(address);

        User user = address.getUser();
        user.setAddress(saved.getFormattedAddress());
        userRepository.save(user);

        return saved;
    }

    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        CustomerAddress address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new RuntimeException("Address not found: " + addressId));
        addressRepository.delete(address);
    }

    private void resetDefaultAddresses(Long userId) {
        List<CustomerAddress> addresses = addressRepository.findByUserId(userId);
        for (CustomerAddress addr : addresses) {
            if (addr.isDefault()) {
                addr.setDefault(false);
                addressRepository.save(addr);
            }
        }
    }
}
