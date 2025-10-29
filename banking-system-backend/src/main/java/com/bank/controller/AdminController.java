package com.bank.controller;


import com.bank.pojos.AuditLogPojo;
import com.bank.pojos.TransactionPojo;
import com.bank.pojos.UserPojo;
import com.bank.service.AdminService;
import com.bank.service.TransactionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    TransactionService transactionService ;

    @Autowired
    AdminService adminService;

    // --------------------------
    // 1️⃣ Get all users with accounts and transactions
    // --------------------------
    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public List<UserPojo> getAllUsers() {
        return adminService.getAllUsersWithAccountsAndTransactions();
    }

    // --------------------------
    // 2️⃣ Freeze Account
    // --------------------------
      @PreAuthorize("hasAnyRole('ADMIN')")
    @PutMapping("/accounts/{accountNumber}/freeze")
    public String freezeAccount(@PathVariable String accountNumber) {
        adminService.freezeAccount(accountNumber);
        return "Account " + accountNumber + " frozen successfully.";
    }

    // --------------------------
    // 3️⃣ Unfreeze Account
    // --------------------------
      @PreAuthorize("hasAnyRole('ADMIN')")
    @PutMapping("/accounts/{accountNumber}/unfreeze")
    public String unfreezeAccount(@PathVariable String accountNumber) {
        adminService.unfreezeAccount(accountNumber);
        return "Account " + accountNumber + " unfrozen successfully.";
    }

    // --------------------------
    // 4️⃣ Activate / Deactivate User
    // --------------------------
      @PreAuthorize("hasAnyRole('ADMIN')")
    @PutMapping("/users/{userId}/{status}")
    public String setUserStatus(@PathVariable Integer userId,
                                @PathVariable UserPojo.Status status) {
        adminService.setUserStatus(userId, status);
        return "User " + userId + " status set to " + status;
    }

    // --------------------------
    // 5️⃣ Flag / Unflag Transaction
    // --------------------------
    
    @PutMapping("/transactions/{txnId}/flag")
      @PreAuthorize("hasAnyRole('ADMIN')")
    public String flagTransaction(@PathVariable Integer txnId,
                                  @RequestParam boolean isSuspicious,
                                  @RequestParam String reason) {
        adminService.setTransactionSuspicious(txnId, isSuspicious, reason);
        return "Transaction " + txnId + " flagged successfully.";
    }

    // --------------------------
    // 6️⃣ Fetch Audit Logs
    // --------------------------
    @GetMapping("/audit-logs")
      @PreAuthorize("hasAnyRole('ADMIN')")
    public List<AuditLogPojo> getAuditLogs() {
        return adminService.getAuditLogs();
    }

    @GetMapping("/transactions/suspicious")
      @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<List<TransactionPojo>> getSuspiciousTransactions() {
        return new  ResponseEntity<List<TransactionPojo>>(transactionService.getSuspiciousTransactions(),HttpStatus.OK);
    }

}
