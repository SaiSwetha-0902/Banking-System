package com.bank.dao.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "account_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountRequestEntity  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer requestId;

  @ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id")
@JsonBackReference  // ✅ prevents recursion while preserving relation
private UserEntity user;

    @Enumerated(EnumType.STRING)
    private AccountType accountType; // SAVINGS / CURRENT

   

    @Enumerated(EnumType.STRING)
    private Status status; // PENDING / APPROVED / REJECTED

    public enum Status {
        PENDING, APPROVED, REJECTED
    }

    public enum AccountType {
        SAVINGS, CURRENT
    }
        public Integer getUserId() {
        return (user != null) ? user.getUserId() : null;
    }
      @Column(name = "ifs_code", length = 100, nullable = false)
        private String ifscCode;

      @Column(name = "branch_name", length = 100, nullable = false)
    private String branchName;

    @Column(name = "nominee_name", length = 100)
    private String nomineeName;

    @Column(name = "nominee_relation", length = 50)
    private String nomineeRelation;

    @Column(name = "debit_card_required", nullable = false)
    private boolean debitCardRequired = false;

    @Column(name = "net_banking_enabled", nullable = false)
    private boolean netBankingEnabled = true;

  @Column(name = "initial_deposit", nullable = false)
    private Double initialDeposit;

}
