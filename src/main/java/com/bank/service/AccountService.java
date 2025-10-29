package com.bank.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bank.dao.AccountDao;
import com.bank.dao.TransactionDao;
import com.bank.dao.UserDao;
import com.bank.dao.entity.AccountEntity;
import com.bank.dao.entity.TransactionEntity;
import com.bank.dao.entity.UserEntity;
import com.bank.pojos.AccountPojo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AccountService {

    @Autowired
   TransactionDao transactionDao;

    @Autowired
    AccountDao accountDao;

    @Autowired
   UserDao userDao;

    @Autowired
    TransactionService transactionService;
    public AccountPojo createAccount(Integer userId, AccountPojo.AccountType type, double initialDeposit) {
    // Fetch user
    UserEntity user = userDao.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

    // Check minimum balance only for SAVINGS
    if (type == AccountPojo.AccountType.SAVINGS && initialDeposit < type.getMinBalance()) {
        throw new RuntimeException(
                "Initial deposit must be at least minimum balance for " + type.name()
        );
    }

    // Create account entity
    AccountEntity account = new AccountEntity();
    account.setUser(user);
    account.setAccountType(AccountEntity.AccountType.valueOf(type.name()));
    account.setBalance(initialDeposit); // Set initial balance
    account.setStatus(AccountEntity.Status.ACTIVE);

    // Generate account number
    String accNumber = "ACCT-" +
            LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) +
            "-" + String.format("%05d", (int) (Math.random() * 100000));
    account.setAccountNumber(accNumber);

    // Save account
    AccountEntity saved = accountDao.save(account);

    // Create initial deposit transaction only if amount > 0
    if (initialDeposit > 0) {
        transactionService.deposit(saved.getAccountNumber(), initialDeposit);
    }

    return mapToPojo(saved);
}

    public AccountPojo getAccountByNumber(String accountNumber) {
        AccountEntity account = accountDao.findById(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        return mapToPojo(account);
    }

  
    public List<AccountPojo> getAccountsByUser(Integer userId) {
        UserEntity user = userDao.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return accountDao.findByUser(user)
                .stream()
                .map(this::mapToPojo)
                .collect(Collectors.toList());
    }

    public AccountPojo updateAccountStatus(String accountNumber, AccountPojo.Status status) {
        AccountEntity account = accountDao.findById(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        account.setStatus(AccountEntity.Status.valueOf(status.name()));
        AccountEntity saved = accountDao.save(account);
        return mapToPojo(saved);
    }


    private AccountPojo mapToPojo(AccountEntity account) {
        return new AccountPojo(
                account.getAccountNumber(),
                account.getUser().getUserId(),
                account.getAccountType() == null ? null : AccountPojo.AccountType.valueOf(account.getAccountType().name()),
                account.getBalance(),
                AccountPojo.Status.valueOf(account.getStatus().name())
        );
    }
   
     public boolean isNewDestination(String fromAccountNumber, String toAccountNumber) {
        // Check if there was any previous transfer from fromAccount to toAccount
        return transactionDao.findByFromAccountNumberAndToAccountNumber(fromAccountNumber, toAccountNumber).isEmpty();
    }

    public Double getAverageBalance(String accountNumber) 
    {
    List<TransactionEntity> txns = transactionDao
            .findByFromAccountNumberOrToAccountNumber(accountNumber, accountNumber);

    double balance = 0.0;
    double totalBalance = 0.0;
    int count = 0;

    // Assume initial balance is zero or fetch from account
    AccountEntity account = accountDao.findById(accountNumber)
            .orElseThrow(() -> new RuntimeException("Account not found"));

    balance = account.getBalance(); // starting point

    for (TransactionEntity txn : txns) {
        if (txn.getType() == TransactionEntity.Type.DEPOSIT || 
            (txn.getType() == TransactionEntity.Type.TRANSFER && txn.getToAccountNumber().equals(accountNumber))) {
            balance += txn.getAmount();
        } else if (txn.getType() == TransactionEntity.Type.WITHDRAWAL || 
                  (txn.getType() == TransactionEntity.Type.TRANSFER && txn.getFromAccountNumber().equals(accountNumber))) {
            balance -= txn.getAmount();
        }
        totalBalance += balance;
        count++;
    }

    return count > 0 ? totalBalance / count : account.getBalance();
}

}

