package com.bank.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.bank.pojos.TransactionPojo;
import com.bank.service.TransactionService;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/transactions")
public class TransactionController {
    @Autowired
    private TransactionService transactionService;

    // --------------------------
    // Deposit money
    // --------------------------
      @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @PostMapping("/deposit/{toAccountNumber}/{amount}")
    public ResponseEntity<TransactionPojo> deposit(
            @PathVariable String toAccountNumber,
            @PathVariable double amount
    ) {
        TransactionPojo transaction = transactionService.deposit(toAccountNumber, amount);
        return new ResponseEntity<>(transaction, HttpStatus.OK);
    }

    // --------------------------
    // Withdraw money
    // --------------------------
      @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @PostMapping("/withdraw/{fromAccountNumber}/{amount}")
    public ResponseEntity<TransactionPojo> withdraw(
            @PathVariable String fromAccountNumber,
            @PathVariable double amount
    ) {
        TransactionPojo transaction = transactionService.withdraw(fromAccountNumber, amount);
        return new ResponseEntity<>(transaction, HttpStatus.OK);
    }

    // --------------------------
    // Transfer money
    // --------------------------
      @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @PostMapping("/transfer/{fromAccountNumber}/{toAccountNumber}/{amount}")
    public ResponseEntity<TransactionPojo> transfer(
            @PathVariable String fromAccountNumber,
            @PathVariable String toAccountNumber,
            @PathVariable double amount
    ) {
        TransactionPojo transaction = transactionService.transfer(fromAccountNumber, toAccountNumber, amount);
        return new ResponseEntity<>(transaction, HttpStatus.OK);
    }

    // --------------------------
    // Get all transactions of an account
    // --------------------------
      @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<List<TransactionPojo>> getTransactionsOfAccount(
            @PathVariable String accountNumber
    ) {
        List<TransactionPojo> transactions = transactionService.getTransactionsOfAccount(accountNumber);
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }
}
