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

@Service
@Transactional(isolation = Isolation.SERIALIZABLE)
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

        } catch (Exception e) {
            transaction.setAmount(amount);
            transaction.setStatus(TransactionEntity.Status.FAILED);
          
            TransactionEntity failed = transactionDao.save(transaction);
            return mapToPojo(failed);
        }
    }

    // --------------------------
    // Withdraw money
    // --------------------------
    public TransactionPojo withdraw(String fromAccountNumber, double amount) {
        TransactionEntity transaction = new TransactionEntity();
        transaction.setFromAccountNumber(fromAccountNumber);
        transaction.setType(TransactionEntity.Type.WITHDRAWAL);
        transaction.setStatus(TransactionEntity.Status.PENDING);
        transaction.setTimestamp(LocalDateTime.now());

        try {
            

            AccountEntity fromAccount = accountDao.findById(fromAccountNumber)
                    .orElseThrow(() -> new RuntimeException("Account not found"));
            UserEntity user = fromAccount.getUser();
            if (user.getStatus() != UserEntity.Status.ACTIVE) 
            {
                throw new RuntimeException("User is inactive. Transaction blocked.");
            }
            if (fromAccount.getStatus() == AccountEntity.Status.FROZEN) 
            {
                    throw new RuntimeException("Cannot withdraw from a frozen account");
            }
            if (amount <= 0)
                throw new RuntimeException("Withdrawal amount must be positive");

            // Check minimum balance
            double newBalance = fromAccount.getBalance() - amount;
            if (newBalance < fromAccount.getAccountType().getMinBalance()) {
                throw new RuntimeException("Insufficient funds: minimum balance required");
            }

            fromAccount.setBalance(newBalance);

            // Mark transaction as SUCCESS
            transaction.setAmount(amount);
            transaction.setStatus(TransactionEntity.Status.SUCCESS);

            // Save transaction first
            TransactionEntity savedTransaction = transactionDao.save(transaction);
            accountDao.save(fromAccount); // Update account

            return mapToPojo(savedTransaction);

        } catch (Exception e) {
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
    public TransactionPojo transfer(String fromAccountNumber, String toAccountNumber, double amount) {
        TransactionEntity transaction = new TransactionEntity();
        transaction.setFromAccountNumber(fromAccountNumber);
        transaction.setToAccountNumber(toAccountNumber);
        transaction.setType(TransactionEntity.Type.TRANSFER);
        transaction.setStatus(TransactionEntity.Status.PENDING);
        transaction.setTimestamp(LocalDateTime.now());

        try {
            

            AccountEntity fromAccount = accountDao.findById(fromAccountNumber)
                    .orElseThrow(() -> new RuntimeException("From account not found"));
             UserEntity user = fromAccount.getUser();
            if (user.getStatus() != UserEntity.Status.ACTIVE) 
            {
                throw new RuntimeException("User is inactive. Transaction blocked.");
            }
            AccountEntity toAccount = accountDao.findById(toAccountNumber)
                    .orElseThrow(() -> new RuntimeException("To account not found"));
            UserEntity user2 = fromAccount.getUser();
            if (user2.getStatus() != UserEntity.Status.ACTIVE) 
            {
                throw new RuntimeException("User is inactive. Transaction blocked.");
            }
            if (fromAccount.getStatus() == AccountEntity.Status.FROZEN) {
                throw new RuntimeException("Cannot transfer from a frozen account");
            }
            if (toAccount.getStatus() == AccountEntity.Status.FROZEN) {
                throw new RuntimeException("Cannot transfer to a frozen account");
            }
            if (amount <= 0)
                throw new RuntimeException("Transfer amount must be positive");
            double newBalance = fromAccount.getBalance() - amount;
            if (newBalance < fromAccount.getAccountType().getMinBalance()) {
                throw new RuntimeException("Insufficient funds in source account");
            }

            // Deduct from sender
            fromAccount.setBalance(newBalance);
       
            // Add to receiver
            toAccount.setBalance(toAccount.getBalance() + amount);

            // Mark transaction as SUCCESS
            transaction.setAmount(amount);
            transaction.setStatus(TransactionEntity.Status.SUCCESS);

            // Save transaction first
            TransactionEntity savedTransaction = transactionDao.save(transaction);
            accountDao.save(fromAccount);
            accountDao.save(toAccount);

            return mapToPojo(savedTransaction);

        } catch (Exception e) {
            transaction.setAmount(amount);
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
}
