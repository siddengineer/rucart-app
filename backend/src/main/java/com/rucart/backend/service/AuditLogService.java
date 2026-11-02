package com.rucart.backend.service;

import com.rucart.backend.model.AuditLog;
import com.rucart.backend.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public AuditLog logAction(String actor, String actorRole, String action, String targetType, String targetId, String details, String result) {
        String actorName = (actor != null && !actor.isBlank()) ? actor : "ADMIN_SYSTEM";
        AuditLog auditLog = new AuditLog(actorName, actorRole != null ? actorRole : "ADMIN", action, targetType, targetId, details, result != null ? result : "SUCCESS");
        log.info("AUDIT: [{}] by [{}] on [{} #{}] - Result: {}", action, actorName, targetType, targetId, result);
        return auditLogRepository.save(auditLog);
    }

    public List<AuditLog> getAllAuditLogs() {
        return auditLogRepository.findTop50ByOrderByTimestampDesc();
    }

    public List<AuditLog> getAuditLogsByTarget(String targetType) {
        return auditLogRepository.findByTargetTypeOrderByTimestampDesc(targetType);
    }

    public List<AuditLog> getAuditLogsByAction(String action) {
        return auditLogRepository.findByActionOrderByTimestampDesc(action);
    }

    public List<AuditLog> getAuditLogsByActor(String actor) {
        return auditLogRepository.findByActorOrderByTimestampDesc(actor);
    }
}
