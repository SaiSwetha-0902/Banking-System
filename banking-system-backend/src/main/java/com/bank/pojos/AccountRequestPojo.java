package com.bank.pojos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountRequestPojo {

    private Integer requestId;
    private Integer userId;
    private AccountType accountType;
    private Double initialDeposit;
    private Status status;

    private String branchName;
    private String ifscCode;
    private String nomineeName;
    private String nomineeRelation;
    private Boolean debitCardRequired;
    private Boolean netBankingEnabled;
     private Integer intialDeposit ;

    public enum Status { PENDING, APPROVED, REJECTED }



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
}
