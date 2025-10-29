export interface AccountRequest {
  requestId?: number;
  userId: number;
  accountType: 'SAVINGS' | 'CURRENT';
  initialDeposit: number;
  status?: 'PENDING' | 'APPROVED' | 'REJECTED';
    ifscCode: string;
  branchName: string;
  nomineeName?: string;
  nomineeRelation?: string;
  debitCardRequired: boolean;
  netBankingEnabled: boolean;
}
