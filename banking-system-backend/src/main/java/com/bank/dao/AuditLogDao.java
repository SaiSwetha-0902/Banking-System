package com.bank.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bank.dao.entity.AuditLogEntity;

public interface AuditLogDao extends JpaRepository<AuditLogEntity,Integer> {
    
}
