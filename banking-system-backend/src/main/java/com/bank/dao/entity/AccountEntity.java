package com.bank.dao.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "accounts")
public class AccountEntity {

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull
    private UserEntity user;

    @Id
    @NotBlank(message = "Account number is required")
    @Size(min = 10, max = 20, message = "Account number must be 10-20 characters")
    @Column(name = "account_number", nullable = false, unique = true)
    private String accountNumber;
  @Version
    private Long version; 
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    @NotNull
    @DecimalMin(value = "0.0")
    @Column(nullable = false)
    private Double balance = 0.0;

    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;

    @OneToMany(mappedBy = "fromAccountNumber")
    private List<TransactionEntity> outgoingTransactions;

    @OneToMany(mappedBy = "toAccountNumber")
    private List<TransactionEntity> incomingTransactions;

    public enum AccountType {
        SAVINGS(1000.0), 
        CURRENT(0.0);      

        private final double minBalance;

        AccountType(double minBalance) {
            this.minBalance = minBalance;
        }

        public double getMinBalance() {
            return minBalance;
        }
    }
    public enum Status { ACTIVE, FROZEN }

      // ✅ Added realistic details for account creation
    @Column(name = "branch_name", nullable = false, length = 100)
    private String branchName;

    @Column(name = "ifsc_code", nullable = false, length = 15)
    private String ifscCode;

    @Column(name = "nominee_name", length = 100)
    private String nomineeName;

    @Column(name = "nominee_relation", length = 50)
    private String nomineeRelation;

    @Column(name = "debit_card_required", nullable = false)
    private boolean debitCardRequired = false;

    @Column(name = "net_banking_enabled", nullable = false)
    private boolean netBankingEnabled = true;
   
}
