package com.bank.service;



import com.bank.dao.AuditLogDao;
import com.bank.dao.entity.AuditLogEntity;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogDao auditLogDao;

    public void logAction(String action, String performedBy, String details) {
        AuditLogEntity log = new AuditLogEntity();
        log.setAction(action);
        log.setPerformedBy(performedBy);
        log.setDetails(details);
        auditLogDao.save(log);
    }
    public List<AuditLogEntity> getAllLogs() {
        return auditLogDao.findAll();
    }
}
