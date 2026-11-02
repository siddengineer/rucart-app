package com.rucart.backend.service;

import com.rucart.backend.dto.StoreOrderStatsDto;
import com.rucart.backend.dto.StoreStatusUpdateRequest;
import com.rucart.backend.model.OrderStatus;
import com.rucart.backend.model.Store;
import com.rucart.backend.repository.OrderRepository;
import com.rucart.backend.repository.StoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StoreService {

    private final StoreRepository storeRepository;
    private final OrderRepository orderRepository;

    public StoreService(StoreRepository storeRepository, OrderRepository orderRepository) {
        this.storeRepository = storeRepository;
        this.orderRepository = orderRepository;
    }

    public List<Store> getAllStores() {
        return storeRepository.findAll();
    }

    public List<Store> getOpenStores() {
        return storeRepository.findByApprovedTrueAndActiveTrueAndOpenTrue();
    }

    public List<Store> getStoresByServiceArea(String area) {
        if (area == null || area.trim().isEmpty()) {
            return getOpenStores();
        }
        return storeRepository.findByApprovedTrueAndActiveTrue().stream()
                .filter(store -> store.servesArea(area))
                .collect(Collectors.toList());
    }

    public Store getStoreById(Long id) {
        return storeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Store not found with id: " + id));
    }

    @Transactional
    public Store updateStoreStatus(Long storeId, StoreStatusUpdateRequest request) {
        Store store = getStoreById(storeId);
        if (request.getOpen() != null) {
            store.setOpen(request.getOpen());
        }
        if (request.getActive() != null) {
            store.setActive(request.getActive());
        }
        return storeRepository.save(store);
    }

    @Transactional
    public Store toggleStoreOpenStatus(Long storeId, boolean open) {
        Store store = getStoreById(storeId);
        store.setOpen(open);
        return storeRepository.save(store);
    }

    public StoreOrderStatsDto getStoreOrderStats(Long storeId) {
        Store store = getStoreById(storeId);

        long newOrders = orderRepository.countByStoreIdAndStatus(storeId, OrderStatus.PLACED);
        long preparingOrders = orderRepository.countByStoreIdAndStatus(storeId, OrderStatus.ACCEPTED)
                + orderRepository.countByStoreIdAndStatus(storeId, OrderStatus.PREPARING);
        long readyOrders = orderRepository.countByStoreIdAndStatus(storeId, OrderStatus.READY_FOR_PICKUP)
                + orderRepository.countByStoreIdAndStatus(storeId, OrderStatus.PACKED);
        long completedOrders = orderRepository.countByStoreIdAndStatus(storeId, OrderStatus.DELIVERED);
        long totalOrders = orderRepository.countByStoreId(storeId);

        BigDecimal totalSales = orderRepository.sumStoreDeliveredOrdersTotal(storeId);
        if (totalSales == null) {
            totalSales = BigDecimal.ZERO;
        }

        return new StoreOrderStatsDto(
                store.getId(),
                store.getName(),
                store.isOpen(),
                store.isActive(),
                newOrders,
                preparingOrders,
                readyOrders,
                completedOrders,
                totalOrders,
                totalSales
        );
    }

    public Store saveStore(Store store) {
        return storeRepository.save(store);
    }
}
