package com.bank.service;
import com.bank.dao.AccountDao;
import com.bank.dao.TransactionDao;
import com.bank.dao.UserDao;
import com.bank.dao.entity.AccountEntity;
import com.bank.dao.entity.AuditLogEntity;
import com.bank.dao.entity.TransactionEntity;
import com.bank.dao.entity.UserEntity;
import com.bank.pojos.AccountPojo;
import com.bank.pojos.AuditLogPojo;
import com.bank.pojos.RolePojo;
import com.bank.pojos.TransactionPojo;
import com.bank.pojos.UserPojo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private AccountDao accountDao;

    @Autowired
    private TransactionDao transactionDao;

    @Autowired
    private AuditLogService auditLogService;

    // --------------------------
    // 1️⃣ Fetch all users with accounts and transactions
    // --------------------------
    public List<UserPojo> getAllUsersWithAccountsAndTransactions() {
        List<UserEntity> users = userDao.findAll();

        return users.stream().map(user -> {
            // Map roles
            List<RolePojo> roles = user.getRoles().stream()
                    .map(r -> new RolePojo(r.getRoleId(), r.getRoleName()))
                    .toList();

            // Map accounts with transactions
            List<AccountPojo> accounts = user.getAccounts().stream().map(account -> {
                List<TransactionPojo> outgoing = account.getOutgoingTransactions().stream()
                        .map(txn -> new TransactionPojo(
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

                List<TransactionPojo> incoming = account.getIncomingTransactions().stream()
                        .map(txn -> new TransactionPojo(
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

                AccountPojo pojo = new AccountPojo(
                        account.getAccountNumber(),
                        account.getUser().getUserId(),
                        AccountPojo.AccountType.valueOf(account.getAccountType().name()),
                        account.getBalance(),
                        AccountPojo.Status.valueOf(account.getStatus().name())
                );

                pojo.setOutgoingTransactions(outgoing);
                pojo.setIncomingTransactions(incoming);

                return pojo;
            }).toList();

            UserPojo userPojo = new UserPojo(
                    user.getUserId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getPassword(),
                    user.getPhone(),
                    UserPojo.Status.valueOf(user.getStatus().name())
            );

            userPojo.setRoles(roles);
            userPojo.setAccounts(accounts);

            return userPojo;
        }).toList();
    }
    // --------------------------
    // 2️⃣ Freeze Account
    // --------------------------
    public void freezeAccount(String accountNumber) {
        String adminUsername ="admin";
        AccountEntity account = accountDao.findById(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));
          UserEntity user = account.getUser();
    Integer userId = (user != null) ? user.getUserId() : null;


        account.setStatus(AccountEntity.Status.FROZEN);
        accountDao.save(account);

        auditLogService.logAction(userId,"ACCOUNT_FROZEN", adminUsername, "Account frozen: " + accountNumber);
    }

    // --------------------------
    // 3️⃣ Unfreeze Account
    // --------------------------
    public void unfreezeAccount(String accountNumber) {
        String adminUsername ="admin";
        AccountEntity account = accountDao.findById(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        
           
          
   Integer userId = (account != null && account.getUser() != null)
            ? account.getUser().getUserId()
            : null;

        account.setStatus(AccountEntity.Status.ACTIVE);
        accountDao.save(account);

        auditLogService.logAction(userId,"ACCOUNT_UNFROZEN", adminUsername, "Account unfrozen: " + accountNumber);
    }

    // --------------------------
    // 4️⃣ Activate/Deactivate User
    // --------------------------
    public void setUserStatus(Integer userId, UserPojo.Status status) {
        String adminUsername ="admin";
        UserEntity user = userDao.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setStatus(UserEntity.Status.valueOf(status.name()));
        userDao.save(user);

        auditLogService.logAction(
                userId,
                "USER_STATUS_CHANGED",
                adminUsername,
                "User " + user.getUsername() + " status set to " + status
        );
    }

    // --------------------------
    // 5️⃣ Flag/Unflag Transaction
    // --------------------------
    public void setTransactionSuspicious(Integer txnId, boolean isSuspicious, String reason) {
         String adminUsername ="admin";
        TransactionEntity txn = transactionDao.findById(txnId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        txn.setIsSuspicious(isSuspicious);
        txn.setSuspiciousReason(reason);
        transactionDao.save(txn);
           // ✅ Manually find account & user
    AccountEntity account = accountDao.findById(txn.getFromAccountNumber())
            .orElse(null);
          
   Integer userId = (account != null && account.getUser() != null)
            ? account.getUser().getUserId()
            : null;

        auditLogService.logAction(
                userId,
                "TRANSACTION_FLAGGED",
                adminUsername,
                "Transaction " + txnId + " flagged: " + reason
        );
    }

    // --------------------------
    // 6️⃣ Fetch Audit Logs
    // --------------------------
    public List<AuditLogPojo> getAuditLogs() {
        List<AuditLogPojo> logs = auditLogService.getAllLogs().stream()
        .map(this::mapToPojo)
        .collect(Collectors.toList());

        return logs;
    }
    public AuditLogPojo mapToPojo(AuditLogEntity entity) {
    return new AuditLogPojo(
         
            entity.getId(),
            entity.getAction(),
            entity.getPerformedBy(),
            entity.getDetails(),
            entity.getTimestamp(),
            entity.getUserId()
    );
}

}
