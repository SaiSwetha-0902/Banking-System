import { Component, OnInit } from '@angular/core';


import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { ActivatedRoute } from '@angular/router';

import { User } from '../../../models/user';
import { Transaction } from '../../../models/transaction';
import { Accountservice } from '../../../service/accountservice';

@Component({
  selector: 'app-customer-transaction',
  standalone: true,
  imports: [CommonModule, FormsModule, DatePipe],
  templateUrl: './customer-transaction.html',
  styleUrls: ['./customer-transaction.css'],
})
export class CustomerTransaction implements OnInit  {
  userId : number | undefined;
  user?: User;
  savingsTransactions: Transaction[] = [];
  currentTransactions: Transaction[] = [];
  isLoading = true; // loading flag

  constructor(private accountService: Accountservice,private route : ActivatedRoute) {}

  ngOnInit(): void {
     this.userId = Number(this.route.snapshot.paramMap.get('userId'));
    this.loadUserTransactions();
  }

  loadUserTransactions(): void {
    this.isLoading = true;
    this.accountService.getAllUsers().subscribe({
      next: (users) => {
        this.user = users.find(u => u.userId === this.userId);
        if (!this.user) {
          this.isLoading = false;
          return;
        }

        // Make sure accounts array exists
        this.user.accounts = this.user.accounts || [];

        // Reset transactions
        this.savingsTransactions = [];
        this.currentTransactions = [];

        // Extract transactions
        this.user.accounts.forEach(acc => {
          const allTxns: Transaction[] = [
            ...(acc.incomingTransactions || []),
            ...(acc.outgoingTransactions || [])
          ];

          if (acc.accountType === 'SAVINGS') {
            this.savingsTransactions.push(...allTxns);
          } else if (acc.accountType === 'CURRENT') {
            this.currentTransactions.push(...allTxns);
          }
        });

        this.isLoading = false; // data loaded
      },
      error: (err:any) => {
        console.error('Error loading users', err);
        this.isLoading = false;
      }
    });
  }

  get savingsDeposits() { return this.savingsTransactions.filter(t => t.type === 'DEPOSIT'); }
  get savingsWithdrawals() { return this.savingsTransactions.filter(t => t.type === 'WITHDRAWAL'); }
  get savingsTransfers() { return this.savingsTransactions.filter(t => t.type === 'TRANSFER'); }
  get currentDeposits() { return this.currentTransactions.filter(t => t.type === 'DEPOSIT'); }
  get currentWithdrawals() { return this.currentTransactions.filter(t => t.type === 'WITHDRAWAL'); }
  get currentTransfers() { return this.currentTransactions.filter(t => t.type === 'TRANSFER'); }
}
