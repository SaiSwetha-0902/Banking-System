package com.bank.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.bank.dao.AccountDao;
import com.bank.dao.TransactionDao;
import com.bank.dao.entity.AccountEntity;
import com.bank.dao.entity.TransactionEntity;
import com.bank.dao.entity.UserEntity;
import com.bank.pojos.TransactionPojo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.persistence.OptimisticLockException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

@Service


public class TransactionService {

    @Autowired
    private TransactionDao transactionDao;

    @Autowired
    private AccountDao accountDao;

    // --------------------------
    // Deposit money
    // --------------------------
    public TransactionPojo deposit(String toAccountNumber, double amount) {
        TransactionEntity transaction = new TransactionEntity();
        transaction.setToAccountNumber(toAccountNumber);
        transaction.setType(TransactionEntity.Type.DEPOSIT);
        transaction.setStatus(TransactionEntity.Status.PENDING);
        transaction.setTimestamp(LocalDateTime.now());

        try {
            
            
            
            AccountEntity toAccount = accountDao.findById(toAccountNumber)
                    .orElseThrow(() -> new RuntimeException("Account not found"));
            UserEntity user = toAccount.getUser();
            if (user.getStatus() != UserEntity.Status.ACTIVE) {
                throw new RuntimeException("User is inactive. Transaction blocked.");
            }
            if (toAccount.getStatus() == AccountEntity.Status.FROZEN) {
                throw new RuntimeException("Cannot deposit to a frozen account");
            }
            if (amount <= 0)
                throw new RuntimeException("Deposit amount must be positive");

            // Update account balance
            toAccount.setBalance(toAccount.getBalance() + amount);

            // Mark transaction as SUCCESS
            transaction.setAmount(amount);
            transaction.setStatus(TransactionEntity.Status.SUCCESS);
            transaction.setDescription("Deposited "+ amount);
            // Save transaction first
            TransactionEntity savedTransaction = transactionDao.save(transaction);
            accountDao.saveAndFlush(toAccount); // Update account

            return mapToPojo(savedTransaction);

        }catch (OptimisticLockException | ObjectOptimisticLockingFailureException e) {
        transaction.setAmount(amount);
        transaction.setStatus(TransactionEntity.Status.FAILED);
        transaction.setDescription("Concurrent update detected. Please retry.");
        TransactionEntity failed = transactionDao.save(transaction);
        return mapToPojo(failed);

    } catch (Exception e) {
            transaction.setAmount(amount);
            transaction.setStatus(TransactionEntity.Status.FAILED);
          
            TransactionEntity failed = transactionDao.save(transaction);
            return mapToPojo(failed);
        }
    }

    // --------------------------
    // Withdraw money
    @Transactional
    public TransactionPojo withdraw(String fromAccountNumber, double amount) {
        TransactionEntity transaction = new TransactionEntity();
        transaction.setFromAccountNumber(fromAccountNumber);
        transaction.setType(TransactionEntity.Type.WITHDRAWAL);
        transaction.setStatus(TransactionEntity.Status.PENDING);
        transaction.setTimestamp(LocalDateTime.now());

        try {
            // Fetch account
            AccountEntity fromAccount = accountDao.findById(fromAccountNumber)
                    .orElseThrow(() -> new RuntimeException("Account not found"));

            // Business validations
            UserEntity user = fromAccount.getUser();
            if (user.getStatus() != UserEntity.Status.ACTIVE) {
                throw new RuntimeException("User is inactive. Transaction blocked.");
            }
            if (fromAccount.getStatus() == AccountEntity.Status.FROZEN) {
                throw new RuntimeException("Cannot withdraw from a frozen account");
            }
            if (amount <= 0) {
                throw new RuntimeException("Withdrawal amount must be positive");
            }

            double newBalance = fromAccount.getBalance() - amount;
            if (newBalance < fromAccount.getAccountType().getMinBalance()) {
                throw new RuntimeException("Insufficient funds: minimum balance required");
            }

            // Simulate concurrent delay (for testing)
            Thread.sleep(500);

            // Update balance
            fromAccount.setBalance(newBalance);

            // Save account → version check happens automatically here
            accountDao.saveAndFlush(fromAccount);

            // Save successful transaction
            transaction.setAmount(amount);
            transaction.setStatus(TransactionEntity.Status.SUCCESS);
            transaction.setDescription("Withdrawal successful");
            TransactionEntity savedTransaction = transactionDao.save(transaction);

            return mapToPojo(savedTransaction);

        } catch (OptimisticLockException | ObjectOptimisticLockingFailureException e) {
            // One of the concurrent threads lost the version race
            System.out.println("⚠️ Concurrent modification detected on account " + fromAccountNumber);

            transaction.setAmount(amount);
            transaction.setStatus(TransactionEntity.Status.FAILED);
            transaction.setDescription("Concurrent update detected. Please retry.");

            TransactionEntity failed = transactionDao.save(transaction);
            return mapToPojo(failed);

        } catch (Exception e) {
            // Any other error
            transaction.setAmount(amount);
            transaction.setStatus(TransactionEntity.Status.FAILED);
            transaction.setDescription(e.getMessage());

            TransactionEntity failed = transactionDao.save(transaction);
            return mapToPojo(failed);
        }
    }

    // --------------------------
    // Transfer money
    // --------------------------
    @Transactional
public TransactionPojo transfer(String fromAccountNumber, String toAccountNumber, double amount) {
    System.out.println("🚀 STARTING TRANSFER: " + fromAccountNumber + " -> " + toAccountNumber + " Amount: " + amount);
    
    TransactionEntity transaction = new TransactionEntity();
    transaction.setFromAccountNumber(fromAccountNumber);
    transaction.setToAccountNumber(toAccountNumber);
    transaction.setType(TransactionEntity.Type.TRANSFER);
    transaction.setStatus(TransactionEntity.Status.PENDING);
    transaction.setTimestamp(LocalDateTime.now());
    transaction.setAmount(amount);

    try {
        System.out.println("📥 Fetching from account: " + fromAccountNumber);
        AccountEntity fromAccount = accountDao.findById(fromAccountNumber)
                .orElseThrow(() -> new RuntimeException("From account not found"));
        System.out.println("✅ From account found - Balance: " + fromAccount.getBalance() + ", Version: " + fromAccount.getVersion());

        System.out.println("📥 Fetching to account: " + toAccountNumber);
        AccountEntity toAccount = accountDao.findById(toAccountNumber)
                .orElseThrow(() -> new RuntimeException("To account not found"));
        System.out.println("✅ To account found - Balance: " + toAccount.getBalance() + ", Version: " + toAccount.getVersion());

        // Validate sender user
        UserEntity senderUser = fromAccount.getUser();
        System.out.println("👤 Sender user status: " + senderUser.getStatus());
        if (senderUser.getStatus() != UserEntity.Status.ACTIVE) {
            throw new RuntimeException("Sender user is inactive. Transaction blocked.");
        }

        // Validate receiver user
        UserEntity receiverUser = toAccount.getUser();
        System.out.println("👤 Receiver user status: " + receiverUser.getStatus());
        if (receiverUser.getStatus() != UserEntity.Status.ACTIVE) {
            throw new RuntimeException("Receiver user is inactive. Transaction blocked.");
        }

        System.out.println("🔍 Validating account statuses...");
        if (fromAccount.getStatus() == AccountEntity.Status.FROZEN) {
            throw new RuntimeException("Cannot transfer from a frozen account");
        }
        if (toAccount.getStatus() == AccountEntity.Status.FROZEN) {
            throw new RuntimeException("Cannot transfer to a frozen account");
        }
        if (amount <= 0) {
            throw new RuntimeException("Transfer amount must be positive");
        }

        System.out.println("💰 Checking balance...");
        double newBalance = fromAccount.getBalance() - amount;
        System.out.println("From account current balance: " + fromAccount.getBalance() + ", After transfer: " + newBalance);
        System.out.println("Minimum balance required: " + fromAccount.getAccountType().getMinBalance());
        
        if (newBalance < fromAccount.getAccountType().getMinBalance()) {
            throw new RuntimeException("Insufficient funds in source account");
        }

        System.out.println("🔄 Performing transfer calculations...");
        // Perform transfer
        fromAccount.setBalance(newBalance);
        toAccount.setBalance(toAccount.getBalance() + amount);
        
        System.out.println("💾 Saving accounts...");
        System.out.println("Before save - From version: " + fromAccount.getVersion() + ", To version: " + toAccount.getVersion());
        
        // Save accounts - this should trigger UPDATE statements
        accountDao.save(fromAccount);
        accountDao.save(toAccount);
        
        System.out.println("✅ Accounts saved successfully");

        // Mark transaction as SUCCESS
        transaction.setStatus(TransactionEntity.Status.SUCCESS);
        transaction.setDescription("Transfer completed successfully");
        
        System.out.println("💾 Saving transaction record...");
        TransactionEntity savedTransaction = transactionDao.save(transaction);
        System.out.println("🎉 TRANSFER SUCCESSFUL - Transaction ID: " + savedTransaction.getTransactionId());

        return mapToPojo(savedTransaction);

    } catch (OptimisticLockException | ObjectOptimisticLockingFailureException e) {
        System.out.println("❌ OPTIMISTIC LOCK EXCEPTION: " + e.getMessage());
        transaction.setStatus(TransactionEntity.Status.FAILED);
        transaction.setDescription("Concurrent update detected. Please retry.");
        TransactionEntity failed = transactionDao.save(transaction);
        return mapToPojo(failed);

    } catch (Exception e) {
        System.out.println("❌ GENERAL EXCEPTION: " + e.getMessage());
        e.printStackTrace();
        transaction.setStatus(TransactionEntity.Status.FAILED);
        transaction.setDescription(e.getMessage());
        TransactionEntity failed = transactionDao.save(transaction);
        return mapToPojo(failed);
    }
}

    // --------------------------
    // Fetch all transactions of an account
    // --------------------------
    public List<TransactionPojo> getTransactionsOfAccount(String accountNumber) {
        return transactionDao.findByFromAccountNumberOrToAccountNumber(accountNumber, accountNumber)
                .stream()
                .map(this::mapToPojo)
                .collect(Collectors.toList());
    }

    // --------------------------
    // Map entity to POJO
    // --------------------------
    private TransactionPojo mapToPojo(TransactionEntity entity) {
        return new TransactionPojo(
                entity.getFromAccountNumber(),
                entity.getToAccountNumber(),
                entity.getAmount(),
                TransactionPojo.Type.valueOf(entity.getType().name()),
                TransactionPojo.Status.valueOf(entity.getStatus().name()),
                entity.getDescription(),
                entity.getTimestamp()
        );
    }
    public List<TransactionPojo> getSuspiciousTransactions() {
    List<TransactionEntity> suspiciousTxns = transactionDao.findByStatus(TransactionEntity.Status.PENDING);
    System.out.println("hiiiiiiiiiiiiiiiiiiiiiiiiii"+suspiciousTxns.size());
    return suspiciousTxns.stream().map(txn -> new TransactionPojo(
            txn.getTransactionId(),
            txn.getFromAccountNumber(),
            txn.getToAccountNumber(),
            txn.getAmount(),
            TransactionPojo.Type.valueOf(txn.getType().name()),
            TransactionPojo.Status.valueOf(txn.getStatus().name()),
            txn.getDescription(),
            txn.getTimestamp(),
            txn.getIsSuspicious(),
            txn.getSuspiciousReason()
    )).toList();
}

}
