package com.rucart.backend.service;

import com.rucart.backend.dto.DeliveryDashboardDto;
import com.rucart.backend.dto.DeliveryPartnerDto;
import com.rucart.backend.model.*;
import com.rucart.backend.repository.DeliveryPartnerRepository;
import com.rucart.backend.repository.OrderRepository;
import com.rucart.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DeliveryPartnerService {

    private final DeliveryPartnerRepository deliveryPartnerRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public DeliveryPartnerService(DeliveryPartnerRepository deliveryPartnerRepository,
                                  UserRepository userRepository,
                                  OrderRepository orderRepository) {
        this.deliveryPartnerRepository = deliveryPartnerRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }

    public List<DeliveryPartner> getAvailablePartners() {
        return deliveryPartnerRepository.findByAvailableTrueAndApprovedTrue();
    }

    public DeliveryPartner getByUserId(Long userId) {
        return deliveryPartnerRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Delivery partner record not found for user: " + userId));
    }

    @Transactional
    public DeliveryPartner registerOrUpdatePartner(Long userId, DeliveryPartnerDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        if (user.getRole() != Role.DELIVERY_PARTNER) {
            user.setRole(Role.DELIVERY_PARTNER);
            userRepository.save(user);
        }

        Optional<DeliveryPartner> existingOpt = deliveryPartnerRepository.findByUserId(userId);
        DeliveryPartner partner = existingOpt.orElseGet(() -> {
            DeliveryPartner dp = new DeliveryPartner();
            dp.setUser(user);
            return dp;
        });

        if (dto.getVehicleType() != null) partner.setVehicleType(dto.getVehicleType());
        if (dto.getVehicleNumber() != null) partner.setVehicleNumber(dto.getVehicleNumber());
        if (dto.getLicenseNumber() != null) partner.setLicenseNumber(dto.getLicenseNumber());
        if (dto.getCurrentLatitude() != null) partner.setCurrentLatitude(dto.getCurrentLatitude());
        if (dto.getCurrentLongitude() != null) partner.setCurrentLongitude(dto.getCurrentLongitude());
        if (dto.getAvailable() != null) partner.setAvailable(dto.getAvailable());

        return deliveryPartnerRepository.save(partner);
    }

    @Transactional
    public DeliveryPartner setAvailability(Long userId, boolean available) {
        DeliveryPartner partner = getByUserId(userId);
        partner.setAvailable(available);
        return deliveryPartnerRepository.save(partner);
    }

    @Transactional
    public DeliveryPartner setPartnerStatus(Long userId, DeliveryPartnerStatus status) {
        DeliveryPartner partner = getByUserId(userId);
        partner.setStatus(status);
        return deliveryPartnerRepository.save(partner);
    }

    public DeliveryDashboardDto getDeliveryDashboard(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        DeliveryPartner partner = deliveryPartnerRepository.findByUserId(userId)
                .orElseGet(() -> {
                    DeliveryPartner dp = new DeliveryPartner();
                    dp.setUser(user);
                    return deliveryPartnerRepository.save(dp);
                });

        int activeAssigned = (int) orderRepository.countByDeliveryPartnerIdAndStatusIn(
                userId,
                List.of(OrderStatus.ASSIGNED, OrderStatus.PICKED_UP, OrderStatus.OUT_FOR_DELIVERY)
        );

        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        List<Order> partnerOrders = orderRepository.findByDeliveryPartnerIdOrderByCreatedAtDesc(userId);

        int completedToday = (int) partnerOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED
                        && o.getDeliveredAt() != null
                        && o.getDeliveredAt().isAfter(startOfDay))
                .count();

        long pendingPickups = orderRepository.countByStatus(OrderStatus.READY_FOR_PICKUP);
        long deliveryIssues = orderRepository.countByDeliveryPartnerIdAndStatus(userId, OrderStatus.DELIVERY_FAILED);

        BigDecimal earningsToday = partnerOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED
                        && o.getDeliveredAt() != null
                        && o.getDeliveredAt().isAfter(startOfDay))
                .map(o -> o.getDeliveryFee() != null ? o.getDeliveryFee() : new BigDecimal("35.00"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalDeliveries = partner.getTotalDeliveries() != null ? partner.getTotalDeliveries() : 0;
        BigDecimal earningsTotal = partner.getEarningsTotal() != null ? partner.getEarningsTotal() : BigDecimal.ZERO;

        return new DeliveryDashboardDto(
                user.getName(),
                partner.getVehicleType(),
                partner.getVehicleNumber(),
                partner.getRating(),
                partner.getStatus(),
                activeAssigned,
                completedToday,
                pendingPickups,
                deliveryIssues,
                earningsToday,
                totalDeliveries,
                earningsTotal
        );
    }
}
