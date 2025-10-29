package com.bank.controller;


import com.bank.dao.entity.AccountRequestEntity;
import com.bank.pojos.AccountRequestPojo;
import com.bank.service.AccountService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/account-requests")
@RequiredArgsConstructor
public class AccountRequestController {

@Autowired
   AccountService requestService;

   @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @PostMapping("/create")
public ResponseEntity<AccountRequestEntity> createRequest(@RequestBody AccountRequestPojo pojo) {
    AccountRequestEntity saved = requestService.createRequest(pojo);
    return ResponseEntity.ok(saved);
}


    @GetMapping("/user/{userId}")
     @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public List<AccountRequestEntity> getUserRequests(@PathVariable Long userId) {
        return requestService.getUserRequests(userId);
    }

    // ADMIN
    @GetMapping("/pending")
     @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public List<AccountRequestEntity> getPendingRequests() {
        return requestService.getPendingRequests();
    }

    @PutMapping("/{requestId}/status")
     @PreAuthorize("hasAnyRole('ADMIN')")
    public AccountRequestEntity updateStatus(@PathVariable Long requestId,
                                             @RequestParam String status) {
        return requestService.updateStatus(
                requestId,
                AccountRequestEntity.Status.valueOf(status.toUpperCase())
        );
    }
}
