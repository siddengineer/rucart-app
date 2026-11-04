package com.rucart.backend.service;

import com.rucart.backend.dto.RiderCommissionDto;
import com.rucart.backend.model.CommissionType;
import com.rucart.backend.model.RiderCommissionConfig;
import com.rucart.backend.model.RiderCommissionHistory;
import com.rucart.backend.repository.RiderCommissionConfigRepository;
import com.rucart.backend.repository.RiderCommissionHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RiderCommissionService {

    private final RiderCommissionConfigRepository configRepository;
    private final RiderCommissionHistoryRepository historyRepository;

    public RiderCommissionService(RiderCommissionConfigRepository configRepository,
                                  RiderCommissionHistoryRepository historyRepository) {
        this.configRepository = configRepository;
        this.historyRepository = historyRepository;
    }

    public RiderCommissionDto getCurrentCommission() {
        RiderCommissionConfig config = configRepository.findFirstByActiveTrueOrderByUpdatedAtDesc()
                .orElseGet(() -> new RiderCommissionConfig(CommissionType.FIXED, new BigDecimal("35.00"), "DEFAULT"));
        return new RiderCommissionDto(config.getCommissionType(), config.getCommissionRate(), config.getUpdatedBy(), config.getUpdatedAt());
    }

    @Transactional
    public RiderCommissionDto updateCommission(CommissionType type, BigDecimal rate, String notes, String updatedBy) {
        if (rate == null || rate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Commission rate must be zero or positive");
        }

        RiderCommissionConfig config = configRepository.findFirstByActiveTrueOrderByUpdatedAtDesc()
                .orElseGet(() -> new RiderCommissionConfig(CommissionType.FIXED, new BigDecimal("35.00"), updatedBy));

        config.setCommissionType(type != null ? type : CommissionType.FIXED);
        config.setCommissionRate(rate);
        config.setUpdatedBy(updatedBy != null ? updatedBy : "ADMIN");
        config.setUpdatedAt(LocalDateTime.now());
        RiderCommissionConfig saved = configRepository.save(config);

        // Record history
        RiderCommissionHistory history = new RiderCommissionHistory(
                saved.getCommissionType(),
                saved.getCommissionRate(),
                saved.getUpdatedBy(),
                notes != null ? notes : "Updated by administrator"
        );
        historyRepository.save(history);

        return new RiderCommissionDto(saved.getCommissionType(), saved.getCommissionRate(), saved.getUpdatedBy(), saved.getUpdatedAt());
    }

    public List<RiderCommissionHistory> getHistory() {
        return historyRepository.findAllByOrderByChangedAtDesc();
    }
}
