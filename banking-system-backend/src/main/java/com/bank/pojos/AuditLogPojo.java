package com.bank.pojos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogPojo {

    private Long id;               // audit log id
    private String action;         // e.g., ACCOUNT_FROZEN, USER_STATUS_CHANGED
    private String performedBy;    // admin username or system
    private String details;        // description of the action
    private LocalDateTime timestamp; // when the action occurred
     private Integer userId;
}
