package com.bank.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bank.dao.AccountDao;
import com.bank.dao.AccountRequestDao;
import com.bank.dao.AuditLogDao;
import com.bank.dao.TransactionDao;
import com.bank.dao.UserDao;
import com.bank.dao.entity.AccountEntity;
import com.bank.dao.entity.AccountRequestEntity;
import com.bank.dao.entity.AuditLogEntity;
import com.bank.dao.entity.TransactionEntity;
import com.bank.dao.entity.UserEntity;
import com.bank.pojos.AccountPojo;
import com.bank.pojos.AccountRequestPojo;

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
   AccountRequestDao accountRequestDao;

   @Autowired
   AuditLogDao auditLogDao;

    @Autowired
    TransactionService transactionService;
    public AccountPojo createAccount(AccountRequestEntity  pojo) {
    // ✅ 1. Fetch user
    UserEntity user = userDao.findById(pojo.getUserId())
            .orElseThrow(() -> new RuntimeException("User not found"));

    // ✅ 2. Check if user already has this account type
    boolean exists = accountDao.existsByUserAndAccountType(
            user, AccountEntity.AccountType.valueOf(pojo.getAccountType().name())
    );
    if (exists) {
        throw new RuntimeException("User already has a " + pojo.getAccountType() + " account.");
    }

  AccountEntity.AccountType type = AccountEntity.AccountType.valueOf(pojo.getAccountType().name());

// ✅ 3. Check minimum balance (only for Savings)
if (type == AccountEntity.AccountType.SAVINGS &&
        pojo.getInitialDeposit() < type.getMinBalance()) {
    throw new RuntimeException("Initial deposit must be at least " + type.getMinBalance() +
            " for " + type.name() + " account.");
}


    // ✅ 4. Create account entity
    AccountEntity account = new AccountEntity();
    account.setUser(user);
    account.setAccountType(AccountEntity.AccountType.valueOf(pojo.getAccountType().name()));
    account.setBalance(pojo.getInitialDeposit());
    account.setStatus(AccountEntity.Status.ACTIVE);
    account.setBranchName(pojo.getBranchName());
    account.setIfscCode(pojo.getIfscCode());
    account.setNomineeName(pojo.getNomineeName());
    account.setNomineeRelation(pojo.getNomineeRelation());
    account.setDebitCardRequired(pojo.isDebitCardRequired());
    account.setNetBankingEnabled(pojo.isNetBankingEnabled());
    // ✅ 5. Generate account number dynamically
    String accNumber = "ACCT-" +
            LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) +
            "-" + String.format("%05d", (int) (Math.random() * 100000));
    account.setAccountNumber(accNumber);

    // ✅ 6. Save to DB
    AccountEntity saved = accountDao.save(account);

    // ✅ 7. Create initial deposit transaction (if deposit > 0)
    if (pojo.getInitialDeposit() > 0) {
        transactionService.deposit(saved.getAccountNumber(), pojo.getInitialDeposit());
    }

    // ✅ 8. Return as Pojo
    return new AccountPojo(
            saved.getAccountNumber(),
            user.getUserId(),
            AccountPojo.AccountType.valueOf(saved.getAccountType().name()),
            saved.getBalance(),
            AccountPojo.Status.valueOf(saved.getStatus().name())
    );
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
    AccountEntity account  = accountDao.findByAccountNumber(accountNumber)
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



 // Customer requests account
   public AccountRequestEntity createRequest(AccountRequestPojo pojo) {
        // 🔹 Get user
        UserEntity user = userDao.findById(pojo.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 🔹 Create request entity dynamically
        AccountRequestEntity request = new AccountRequestEntity();
        request.setUser(user);
        request.setAccountType(AccountRequestEntity.AccountType.valueOf(pojo.getAccountType().name()));
        request.setInitialDeposit(pojo.getInitialDeposit());
        request.setStatus(AccountRequestEntity.Status.PENDING);
       request.setInitialDeposit(pojo.getInitialDeposit());
        // Additional fields
        request.setBranchName(pojo.getBranchName());
        request.setIfscCode(pojo.getIfscCode());
        request.setNomineeName(pojo.getNomineeName());
        request.setNomineeRelation(pojo.getNomineeRelation());
        request.setDebitCardRequired(pojo.getDebitCardRequired());
        request.setNetBankingEnabled(pojo.getNetBankingEnabled());

        // 🔹 Save to DB
        return accountRequestDao.saveAndFlush(request);
    }

   // ✅ Admin approves or rejects account request
public AccountRequestEntity updateStatus(Long requestId, AccountRequestEntity.Status newStatus) {

    // 1️⃣ Fetch request
    AccountRequestEntity req = accountRequestDao.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Request not found"));

    // 2️⃣ Update request status
    req.setStatus(newStatus);
    accountRequestDao.save(req);

    // 3️⃣ Log status update
    AuditLogEntity statusLog = new AuditLogEntity();
    statusLog.setUserId(req.getUserId());
    statusLog.setAction("ACCOUNT_REQUEST_STATUS_UPDATE");
    statusLog.setPerformedBy("ADMIN");
    statusLog.setDetails("Request ID " + requestId + " changed to " + newStatus);
    statusLog.setTimestamp(java.time.LocalDateTime.now());
    auditLogDao.save(statusLog);

    // 4️⃣ If approved → create account from request
    if (newStatus == AccountRequestEntity.Status.APPROVED) {

        try {
            // Create account based on the approved request
            AccountPojo createdAccount = createAccount(req);

            // 5️⃣ Log account creation
            AuditLogEntity accountLog = new AuditLogEntity();
            accountLog.setUserId(req.getUser().getUserId());
            accountLog.setAction("ACCOUNT_CREATED");
            accountLog.setPerformedBy("ADMIN");
            accountLog.setDetails("Account " + createdAccount.getAccountNumber() +
                    " created for user ID " + req.getUser().getUserId() +
                    " from request ID " + requestId);
            accountLog.setTimestamp(java.time.LocalDateTime.now());
            auditLogDao.save(accountLog);

        } catch (Exception e) {
            throw new RuntimeException("Account creation failed after approval: " + e.getMessage());
        }
    }

    return req;
}


    public List<AccountRequestEntity> getPendingRequests() {
        return accountRequestDao.findByStatus(AccountRequestEntity.Status.PENDING);
    }

    public List<AccountRequestEntity> getUserRequests(Long userId) {
        return accountRequestDao.findByUser_UserId(userId);
    }
}

