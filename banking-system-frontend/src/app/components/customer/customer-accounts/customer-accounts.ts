import { ChangeDetectorRef, Component, OnInit } from '@angular/core';

import { TransactionService } from '../../../service/transferservice';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { AccountRequestService } from '../../../service/account-request-service';

import { Account } from '../../../models/account';
import { AccountRequest } from '../../../models/AccountRequest';
import { Accountservice } from '../../../service/accountservice';

@Component({
  selector: 'app-customer-accounts',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './customer-accounts.html',
  styleUrls: ['./customer-accounts.css'],
})
export class CustomerAccounts implements OnInit {
  userId: number | undefined;
  accounts: Account[] = [];
  accountRequests: AccountRequest[] = [];
  
  readonly MIN_BALANCE = 1000;

  newAccount: AccountRequest = {
    userId: 0,
    accountType: 'SAVINGS',
    initialDeposit: 0,
    branchName: '',
    ifscCode: '',
    nomineeName: '',
    nomineeRelation: '',
    debitCardRequired: false,
    netBankingEnabled: false,
    status: 'PENDING',
  };

  accountActions: {
    [accountNumber: string]: {
      depositAmount?: number;
      withdrawAmount?: number;
      transferTo?: string;
      transferAmount?: number;
    };
  } = {};

  constructor(
    private accountService: Accountservice,
    private transactionService: TransactionService,
    private route: ActivatedRoute,
    private accountRequestService: AccountRequestService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.userId = Number(this.route.snapshot.paramMap.get('userId'));
    this.loadAccounts();
    this.loadAccountRequests();
  }

  /** 🔹 Load user's accounts */
  loadAccounts(): void {
    this.accountService.getAllUsers().subscribe({
      next: (users) => {
        const user = users.find((u) => u.userId === this.userId);
        this.accounts = user?.accounts || [];
        this.accounts.forEach((acc) => {
          if (!this.accountActions[acc.accountNumber]) {
            this.accountActions[acc.accountNumber] = {};
          }
        });
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Error loading accounts', err),
    });
  }

  /** 🔹 Load user's account requests */
  loadAccountRequests(): void {
    if (!this.userId) return;
    this.accountRequestService.getPendingRequests().subscribe({
      next: (requests) => {
        this.accountRequests = requests.filter((req) => req.userId === this.userId);
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Error loading account requests', err),
    });
  }

  /** 🔹 Check for any pending requests */
  get hasPendingRequest(): boolean {
    if (!Array.isArray(this.accountRequests)) return false;
    return this.accountRequests.some((r) => r?.status === 'PENDING');
  }

  /** 🔹 Check if user already has a type of account */
  hasAccount(type: 'SAVINGS' | 'CURRENT'): boolean {
    return this.accounts.some((acc) => acc.accountType === type);
  }

  /** 🔹 Submit account creation request */
  createAccount(type: 'SAVINGS' | 'CURRENT'): void {
    if (!this.userId) return;

    if (!this.newAccount.branchName || !this.newAccount.ifscCode) {
      alert('Branch name and IFSC code are required.');
      return;
    }

    if (type === 'SAVINGS' && (!this.newAccount.initialDeposit || this.newAccount.initialDeposit < this.MIN_BALANCE)) {
      alert(`Minimum deposit for Savings account is ₹${this.MIN_BALANCE}.`);
      return;
    }

    const requestPayload: AccountRequest = {
      ...this.newAccount,
      userId: this.userId,
      accountType: type,
    };

    this.accountRequestService.submitRequest(requestPayload).subscribe({
      next: () => {
        alert(`${type} account creation request submitted successfully!`);
        this.loadAccountRequests();
        this.cdr.detectChanges();
      },
      error: (err) => alert(`Failed to submit request: ${err.error?.message || err.message}`),
    });
  }

  /** 🔹 Deposit action */
  deposit(account: Account): void {
    const action = this.accountActions[account.accountNumber];
    if (!action.depositAmount || action.depositAmount <= 0) {
      alert('Enter a valid deposit amount.');
      return;
    }

    this.transactionService.deposit(account.accountNumber, action.depositAmount).subscribe({
      next: (res) => {
        alert(`Deposit successful: ${res.status}`);
        action.depositAmount = 0;
        this.loadAccounts();
        this.cdr.detectChanges();
      },
      error: (err) => alert(`Deposit failed: ${err.error?.message || err.message}`),
    });
  }

  /** 🔹 Withdraw action */
  withdraw(account: Account): void {
    const action = this.accountActions[account.accountNumber];
    if (!action.withdrawAmount || action.withdrawAmount <= 0) {
      alert('Enter a valid withdrawal amount.');
      return;
    }
    if (account.balance - action.withdrawAmount < this.MIN_BALANCE) {
      alert(`Cannot withdraw. Minimum balance of ₹${this.MIN_BALANCE} must be maintained.`);
      return;
    }

    this.transactionService.withdraw(account.accountNumber, action.withdrawAmount).subscribe({
      next: (res) => {
        alert(`Withdrawal successful: ${res.status}`);
        action.withdrawAmount = 0;
        this.loadAccounts();
        this.cdr.detectChanges();
      },
      error: (err) => alert(`Withdrawal failed: ${err.error?.message || err.message}`),
    });
  }

  /** 🔹 Transfer action */
  transfer(account: Account): void {
    const action = this.accountActions[account.accountNumber];
    if (!action.transferTo || !action.transferAmount || action.transferAmount <= 0) {
      alert('Enter valid transfer details.');
      return;
    }
    if (account.balance - action.transferAmount < this.MIN_BALANCE) {
      alert(`Cannot transfer. Minimum balance of ₹${this.MIN_BALANCE} must be maintained.`);
      return;
    }

    this.transactionService.transfer(account.accountNumber, action.transferTo, action.transferAmount).subscribe({
      next: (res) => {
        alert(`Transfer successful: ${res.status}`);
        action.transferTo = '';
        action.transferAmount = 0;
        this.loadAccounts();
        this.cdr.detectChanges();
      },
      error: (err) => alert(`Transfer failed: ${err.error?.message || err.message}`),
    });
  }
}
