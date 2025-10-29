package com.bank.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bank.dao.entity.TransactionEntity;

public interface TransactionDao extends JpaRepository<TransactionEntity,Integer>{
   List<TransactionEntity> findByFromAccountNumberOrToAccountNumber(String accountNumber, String accountNumber1);
      // 1️⃣ Get all transactions after a certain timestamp
    List<TransactionEntity> findByTimestampAfter(LocalDateTime since);

    // 2️⃣ Count failed transactions for a fromAccountNumber after a timestamp
    long countByFromAccountNumberAndStatusAndTimestampAfter(
            String fromAccountNumber, TransactionEntity.Status status, LocalDateTime since);

    // 3️⃣ Count all transactions for a fromAccountNumber after a timestamp
    long countByFromAccountNumberAndTimestampAfter(
            String fromAccountNumber, LocalDateTime since);

    // Optional: fetch all transactions for a specific fromAccountNumber
    List<TransactionEntity> findByFromAccountNumber(String fromAccountNumber);

    // Optional: fetch all transactions for a specific toAccountNumber
    List<TransactionEntity> findByToAccountNumber(String toAccountNumber);

     List<TransactionEntity> findByFromAccountNumberAndToAccountNumber(String fromAccountNumber, String toAccountNumber);
     List<TransactionEntity> findByStatus(TransactionEntity.Status status);

} 
