import { Transaction } from "./transaction";


export interface Account {
  accountNumber: string;
  accountType: 'SAVINGS' | 'CURRENT';
  balance: number;
  status: 'ACTIVE' | 'FROZEN';
  userId: number;
  outgoingTransactions?: Transaction[];
  incomingTransactions?: Transaction[];
   branchName: string;
  ifscCode: string;
  nomineeName?: string;
  nomineeRelation?: string;
  debitCardRequired: boolean;
  netBankingEnabled: boolean;
}
