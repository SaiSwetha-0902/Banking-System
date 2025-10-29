package com.bank.pojos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionPojo {

  
    private int transactionId;
    private String fromAccountNumber;
    private String toAccountNumber;  
    private Double amount;

    private Type type;
    private Status status;
    private String description;
    private LocalDateTime timestamp;
     private boolean isSuspicious ;
    private String suspiciousReason;

    public enum Type { DEPOSIT, WITHDRAWAL, TRANSFER }
    public enum Status { SUCCESS, FAILED, PENDING }

    public TransactionPojo(
        String fromAccountNumber,
        String toAccountNumber,
        Double amount,
        Type type,
        Status status,
        String description,
        LocalDateTime timestamp
) {
    this.fromAccountNumber = fromAccountNumber;
    this.toAccountNumber = toAccountNumber;
    this.amount = amount;
    this.type = type;
    this.status = status;
    this.description = description;
    this.timestamp = timestamp;
}

     public boolean getIsSuspicious() {
        return isSuspicious;
    }

    public void setIsSuspicious(boolean isSuspicious) {
        this.isSuspicious = isSuspicious;
    }
}
