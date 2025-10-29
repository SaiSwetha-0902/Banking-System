
package com.bank.controller;

import com.bank.dao.entity.AccountRequestEntity;
import com.bank.pojos.AccountPojo;
import com.bank.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    // -------------------------------
    // 1️⃣ Create Account
    // -------------------------------
@PostMapping("/create")
@PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
public ResponseEntity<AccountPojo> createAccount(@RequestBody AccountRequestEntity requestEntity) {
    AccountPojo createdAccount = accountService.createAccount(requestEntity);
    return ResponseEntity.ok(createdAccount);
}


    // -------------------------------
    // 2️⃣ Get Account by Account Number
    // -------------------------------
    @GetMapping("/{accountNumber}")
      @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
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
      @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<List<AccountPojo>> getAccountsByUser(@PathVariable Integer userId) {
        return new ResponseEntity<>(
                accountService.getAccountsByUser(userId),
                HttpStatus.OK
        );
    }

    // -------------------------------
    // 4️⃣ Update Account Status
    // -------------------------------
    @PutMapping("/{accountNumber}/status")
      @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<AccountPojo> updateStatus(
            @PathVariable String accountNumber,
            @RequestParam AccountPojo.Status status
    ) {
        return new ResponseEntity<>(
                accountService.updateAccountStatus(accountNumber, status),
                HttpStatus.OK
        );
    }

    
    // -------------------------------
    // 7️⃣ Check if Destination is New
    // -------------------------------
    @GetMapping("/is-new-destination")
      @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
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
