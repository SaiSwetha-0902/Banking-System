package com.bank.controller;


import com.bank.pojos.AuditLogPojo;
import com.bank.pojos.UserPojo;
import com.bank.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    AdminService adminService;

    // --------------------------
    // 1️⃣ Get all users with accounts and transactions
    // --------------------------
    @GetMapping("/users")
    public List<UserPojo> getAllUsers() {
        return adminService.getAllUsersWithAccountsAndTransactions();
    }

    // --------------------------
    // 2️⃣ Freeze Account
    // --------------------------
    @PutMapping("/accounts/{accountNumber}/freeze")
    public String freezeAccount(@PathVariable String accountNumber) {
        adminService.freezeAccount(accountNumber);
        return "Account " + accountNumber + " frozen successfully.";
    }

    // --------------------------
    // 3️⃣ Unfreeze Account
    // --------------------------
    @PutMapping("/accounts/{accountNumber}/unfreeze")
    public String unfreezeAccount(@PathVariable String accountNumber) {
        adminService.unfreezeAccount(accountNumber);
        return "Account " + accountNumber + " unfrozen successfully.";
    }

    // --------------------------
    // 4️⃣ Activate / Deactivate User
    // --------------------------
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
    public List<AuditLogPojo> getAuditLogs() {
        return adminService.getAuditLogs();
    }
}
