package com.bank.service;

import com.bank.dao.TransactionDao;
import com.bank.dao.entity.TransactionEntity;
import com.bank.pojos.AccountPojo;
import com.bank.dao.entity.AccountEntity;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SuspiciousTransactionService {

    @Autowired
    TransactionDao transactionDao;
    @Autowired
    AccountService accountService; // for checking accounts and marking suspicious

    @Autowired
    private AuditLogService auditLogService;


    private static final double HIGH_VALUE_THRESHOLD = 100000.0; // ₹1,00,000
    private static final int FREQUENT_TXN_LIMIT = 5;               // transactions per hour

    // Run every 5 minutes
    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void monitorSuspiciousTransactions() {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);

        // 1️⃣ Get all recent transactions in the last 1 hour
        List<TransactionEntity> recentTxns = transactionDao.findByTimestampAfter(oneHourAgo);

        for (TransactionEntity txn : recentTxns) {
            boolean isSuspicious = false;

            // 2️⃣ High-value transaction
            if (txn.getAmount() >= HIGH_VALUE_THRESHOLD) {
                isSuspicious = true;
            }

            // 3️⃣ Frequent transactions in a short period
            long recentCount = transactionDao.countByFromAccountNumberAndTimestampAfter(txn.getFromAccountNumber(), oneHourAgo);
            if (recentCount >= FREQUENT_TXN_LIMIT) {
                isSuspicious = true;
            }

            // 4️⃣ Repeated failed transactions
            if (txn.getStatus() == TransactionEntity.Status.FAILED) {
                long failedCount = transactionDao.countByFromAccountNumberAndStatusAndTimestampAfter(
                        txn.getFromAccountNumber(), TransactionEntity.Status.FAILED, oneHourAgo
                );
                if (failedCount >= FREQUENT_TXN_LIMIT) {
                    isSuspicious = true;
                }
            }

            // 5️⃣ Unusual destinations (transfer to a new account)
            if (txn.getType() == TransactionEntity.Type.TRANSFER) {
                boolean isNewDestination = accountService.isNewDestination(txn.getFromAccountNumber(), txn.getToAccountNumber());
                if (isNewDestination) {
                    isSuspicious = true;
                }
            }

            // 6️⃣ Balance anomaly
            Double averageBalance = accountService.getAverageBalance(txn.getFromAccountNumber());
            AccountPojo fromAccount = accountService.getAccountByNumber(txn.getFromAccountNumber());
             if (Math.abs(fromAccount.getBalance() - averageBalance) > averageBalance * 2) {
                 isSuspicious = true;
             }

            // 7️⃣ Flag transaction/account if suspicious
           if (isSuspicious) {
                txn.setDescription("Flagged as suspicious by monitoring service");
                txn.setStatus(TransactionEntity.Status.PENDING);

                // Freeze the account
                fromAccount.setStatus(AccountPojo.Status.valueOf(AccountEntity.Status.FROZEN.name()));

                // Save the transaction
                transactionDao.save(txn);

                // -------------------------
                // Log in Audit
                // -------------------------
                String details = String.format(
                    "Transaction ID: %d, From Account: %s, Reason: %s",
                    txn.getTransactionId(),
                    txn.getFromAccountNumber(),
                    txn.getDescription()
                );
                auditLogService.logAction("ACCOUNT_FROZEN / TRANSACTION_FLAGGED", "ADMIN", details);
        }

        }
    }
}
