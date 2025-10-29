package com.bank.pojos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountPojo {

   public AccountPojo(String accountNumber, Integer userId, AccountType accountType, Double balance, Status status) {
    this.accountNumber = accountNumber;
    this.userId = userId;
    this.accountType = accountType;
    this.balance = balance;
    this.status = status;
}

    private String accountNumber;
    private AccountType accountType;
    private Double balance;
    private Status status;
    private Integer userId; 
    private List<TransactionPojo> outgoingTransactions; // Sent transactions
    private List<TransactionPojo> incomingTransactions; // Received transactions

     private String branchName;
    private String ifscCode;
    private String nomineeName;
    private String nomineeRelation;
    private boolean debitCardRequired;
    private boolean netBankingEnabled;

    public enum AccountType {
        SAVINGS(1000.0),  // minimum balance for savings
        CURRENT(0.0);      // minimum balance for current

        private final double minBalance;

        AccountType(double minBalance) {
            this.minBalance = minBalance;
        }

        public double getMinBalance() {
            return minBalance;
        }
    }
    public enum Status { ACTIVE, FROZEN }

}
