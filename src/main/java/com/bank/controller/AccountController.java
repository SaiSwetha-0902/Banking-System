
package com.bank.controller;

import com.bank.pojos.AccountPojo;
import com.bank.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    // -------------------------------
    // 1️⃣ Create Account
    // -------------------------------
   @PostMapping("/create/{userId}/{type}/{initialDeposit}")
public ResponseEntity<AccountPojo> createAccount(
        @PathVariable Integer userId,
        @PathVariable AccountPojo.AccountType type,
        @PathVariable double initialDeposit
) {
    return new ResponseEntity<>(
            accountService.createAccount(userId, type, initialDeposit),
            HttpStatus.OK
    );
}


    // -------------------------------
    // 2️⃣ Get Account by Account Number
    // -------------------------------
    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountPojo> getAccount(@PathVariable String accountNumber) {
        return new ResponseEntity<>(
                accountService.getAccountByNumber(accountNumber),
                HttpStatus.OK
        );
    }

    // -------------------------------
    // 3️⃣ Get All Accounts for a User
    // -------------------------------
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AccountPojo>> getAccountsByUser(@PathVariable Integer userId) {
        return new ResponseEntity<>(
                accountService.getAccountsByUser(userId),
                HttpStatus.OK
        );
    }

    // -------------------------------
    // 4️⃣ Update Account Status
    // -------------------------------
    @PutMapping("/{accountNumber}/{status}")
    public ResponseEntity<AccountPojo> updateStatus(
            @PathVariable String accountNumber,
            @PathVariable AccountPojo.Status status
    ) {
        return new ResponseEntity<>(
                accountService.updateAccountStatus(accountNumber, status),
                HttpStatus.OK
        );
    }

    // -------------------------------
    // 5️⃣ Freeze Account
    // -------------------------------
    

    // -------------------------------
    // 7️⃣ Check if Destination is New
    // -------------------------------
    @GetMapping("/is-new-destination")
    public ResponseEntity<Boolean> isNewDestination(
            @RequestParam String fromAccountNumber,
            @RequestParam String toAccountNumber
    ) {
        return new ResponseEntity<>(
                accountService.isNewDestination(fromAccountNumber, toAccountNumber),
                HttpStatus.OK
        );
    }
}
