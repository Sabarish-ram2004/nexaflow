package com.nexaflow.service;

import com.nexaflow.model.AuditLog;
import com.nexaflow.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    public void log(String performedBy, String action, String details) {
        auditLogRepository.save(new AuditLog(performedBy, action, details));
    }
}
