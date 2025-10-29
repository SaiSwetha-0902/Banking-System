import { Component } from '@angular/core';

import { Transaction } from '../../../models/transaction';
import { CommonModule } from '@angular/common';
import { Accountservice } from '../../../service/accountservice';

@Component({
  selector: 'app-transactions',
  imports: [CommonModule],
  templateUrl: './transactions.html',
  styleUrl: './transactions.css',
})
export class Transactions {

  constructor(private accountService : Accountservice){}
  transactions : Transaction[] =[];
  ngOnInit(): void {
    this.loadAllTransactions();
  }
loadAllTransactions() {
    this.accountService.getAllUsers().subscribe({
      next: (users) => {
        const uniqueTxns = new Map<number, Transaction>();

        users?.forEach(user => {
          user.accounts?.forEach(account => {

            // ✅ Include transactions only if the account is FROZEN
            if (account.status === 'FROZEN') {

              // Outgoing transactions
              account.outgoingTransactions?.forEach((txn: Transaction) => {
                if (!uniqueTxns.has(txn.transactionId)) {
                  uniqueTxns.set(txn.transactionId, txn);
                }
              });

              // Incoming transactions
              account.incomingTransactions?.forEach((txn: Transaction) => {
                if (!uniqueTxns.has(txn.transactionId)) {
                  uniqueTxns.set(txn.transactionId, txn);
                }
              });
            }
          });
        });

        // Convert Map to array
        this.transactions = Array.from(uniqueTxns.values());

        // Sort newest first
        this.transactions.sort((a, b) => {
          const timeA = a.timestamp ? new Date(a.timestamp).getTime() : 0;
          const timeB = b.timestamp ? new Date(b.timestamp).getTime() : 0;
          return timeB - timeA;
        });
      },
      error: (err) => console.error(err)
    });
  }
hasSuspiciousTransactions(): boolean {
  return Array.isArray(this.transactions) && this.transactions.some(t => t.isSuspicious);
}

}
