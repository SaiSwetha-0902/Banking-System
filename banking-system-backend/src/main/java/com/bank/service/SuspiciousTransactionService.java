package com.bank.service;

import com.bank.dao.AccountDao;
import com.bank.dao.TransactionDao;
import com.bank.dao.entity.TransactionEntity;
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
    private TransactionDao transactionDao;

    @Autowired
    private AccountService accountService; // for checking accounts and marking suspicious

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private AccountDao accountDao;

    private static final double HIGH_VALUE_THRESHOLD = 100000.0; // ₹1,00,000
    private static final int FREQUENT_TXN_LIMIT = 5;             // transactions per hour

    @Scheduled(fixedRate = 60_000) // every 1 minute
    public void monitorSuspiciousTransactions() {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        List<TransactionEntity> recentTxns = transactionDao.findByTimestampAfter(oneHourAgo);

        for (TransactionEntity txn : recentTxns) {
            // ✅ Skip already flagged transactions
            if (Boolean.TRUE.equals(txn.getIsSuspicious())) continue;

            // ✅ Skip incomplete transactions (missing required account numbers)
            if ((txn.getType() == TransactionEntity.Type.DEPOSIT && txn.getToAccountNumber() == null) ||
                (txn.getType() == TransactionEntity.Type.WITHDRAWAL && txn.getFromAccountNumber() == null) ||
                (txn.getType() == TransactionEntity.Type.TRANSFER &&
                 (txn.getFromAccountNumber() == null || txn.getToAccountNumber() == null))) {
                System.out.println("Skipping transaction " + txn.getTransactionId() + " due to missing account details");
                continue;
            }

            // ✅ Load accounts safely
            AccountEntity fromAccount = null;
            AccountEntity toAccount = null;

            if (txn.getFromAccountNumber() != null) {
                fromAccount = accountDao.findByAccountNumber(txn.getFromAccountNumber()).orElse(null);
                if (fromAccount == null) continue;
                if (fromAccount.getStatus() == AccountEntity.Status.FROZEN) continue;
            }

            if (txn.getToAccountNumber() != null) {
                toAccount = accountDao.findByAccountNumber(txn.getToAccountNumber()).orElse(null);
                if (toAccount == null) continue;
            }

            boolean isSuspicious = false;
            String reason = "";

            // 1️⃣ High-value transaction
            if (txn.getAmount() >= HIGH_VALUE_THRESHOLD) {
                isSuspicious = true;
                reason = "High-value transaction detected";
            }

            // 2️⃣ Frequent transactions from the same account (outgoing)
            if (txn.getFromAccountNumber() != null) {
                long recentCount = transactionDao.countByFromAccountNumberAndTimestampAfter(
                        txn.getFromAccountNumber(), oneHourAgo);
                if (recentCount >= FREQUENT_TXN_LIMIT) {
                    isSuspicious = true;
                    reason = "Frequent transactions in short period";
                }
            }

            // 3️⃣ Multiple failed transactions
            if (txn.getStatus() == TransactionEntity.Status.FAILED &&
                txn.getFromAccountNumber() != null) {
                long failedCount = transactionDao.countByFromAccountNumberAndStatusAndTimestampAfter(
                        txn.getFromAccountNumber(), TransactionEntity.Status.FAILED, oneHourAgo);
                if (failedCount >= FREQUENT_TXN_LIMIT) {
                    isSuspicious = true;
                    reason = "Multiple failed transactions";
                }
            }

            // 4️⃣ Unusual destination (for transfers)
            if (txn.getType() == TransactionEntity.Type.TRANSFER) {
                boolean isNewDestination = accountService.isNewDestination(
                        txn.getFromAccountNumber(), txn.getToAccountNumber());
                if (isNewDestination) {
                    isSuspicious = true;
                    reason = "Transfer to new destination account";
                }
            }

            // 5️⃣ Balance anomaly
            if (fromAccount != null) {
                Double averageBalance = accountService.getAverageBalance(txn.getFromAccountNumber());
                if (averageBalance != null &&
                    Math.abs(fromAccount.getBalance() - averageBalance) > averageBalance * 2) {
                    isSuspicious = true;
                    reason = "Balance anomaly detected";
                }
            }

            System.out.println("Transaction " + txn.getTransactionId() + " checked — not suspicious yet.");

            // ✅ If suspicious → flag + freeze account + audit log
            if (isSuspicious) {
                System.out.println("⚠️ Suspicious transaction detected: " + txn.getTransactionId());

                txn.setDescription("Flagged as suspicious by monitoring service");
                txn.setStatus(TransactionEntity.Status.PENDING);
                txn.setIsSuspicious(true);
                txn.setSuspiciousReason(reason);
                transactionDao.save(txn);

                // ✅ Determine which account to freeze
                AccountEntity targetAccount = null;
                switch (txn.getType()) {
                    case DEPOSIT:
                        targetAccount = toAccount; // freeze receiver
                        break;
                    case WITHDRAWAL:
                    case TRANSFER:
                        targetAccount = fromAccount; // freeze sender
                        break;
                }

                // ✅ Freeze and log
                if (targetAccount != null) {
                    targetAccount.setStatus(AccountEntity.Status.FROZEN);
                    accountDao.save(targetAccount);

                    adminService.freezeAccount(targetAccount.getAccountNumber());

                    Integer userId = targetAccount.getUser() != null ? targetAccount.getUser().getUserId() : null;
                    auditLogService.logAction(
                            userId,
                            "ACCOUNT_FROZEN / TRANSACTION_FLAGGED",
                            "ADMIN",
                            String.format("Transaction ID: %d, Account: %s, Reason: %s",
                                    txn.getTransactionId(),
                                    targetAccount.getAccountNumber(),
                                    reason)
                    );
                }
            }
        }
    }
}
