export interface Transaction {
     transactionId : number; 
  
  fromAccountNumber?: string; // nullable for deposit
  toAccountNumber?: string;   // nullable for withdrawal
  amount: number;
  type: 'DEPOSIT' | 'WITHDRAWAL' | 'TRANSFER';
  status: 'SUCCESS' | 'FAILED' | 'PENDING';
  description?: string;
  timestamp?: string; // ISO string from backend
  
   
   isSuspicious : boolean;

   suspiciousReason : String;

}
