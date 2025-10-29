package com.bank.dao.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transactions")
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer transactionId; 

    @Column(name = "from_account_number")
    private String fromAccountNumber; 


    @Column(name = "to_account_number")
    private String toAccountNumber;  

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    private Double amount;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Transaction type is required")
    private Type type;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    @Size(max = 255)
    private String description;

    @Column(name = "is_suspicious", nullable = false)
    private Boolean isSuspicious = false;

    @Size(max = 255)
    @Column(name = "suspicious_reason")
    private String suspiciousReason;


    private LocalDateTime timestamp = LocalDateTime.now();

    // -------------------
    // Enums
    // -------------------
    public enum Type { DEPOSIT, WITHDRAWAL, TRANSFER }
    public enum Status { SUCCESS, FAILED, PENDING }
}
