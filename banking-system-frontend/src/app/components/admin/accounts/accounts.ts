import { ChangeDetectorRef, Component, ElementRef, ViewChild } from '@angular/core';
import { Account } from '../../../models/account';

import { User } from '../../../models/user';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import Chart from 'chart.js/auto';
import { Accountservice } from '../../../service/accountservice';
@Component({
  selector: 'app-accounts',
  imports: [CurrencyPipe,CommonModule,FormsModule],
  templateUrl: './accounts.html',
  styleUrl: './accounts.css',
})
export class Accounts {
  @ViewChild('barChart') barChartRef!: ElementRef<HTMLCanvasElement>;
   chart : any;
  accounts: Account[] = [];
  users?:User[];
  user?:User;
  
  
    constructor(
      private accountService: Accountservice,private cdr: ChangeDetectorRef
    ) {}
  
    ngOnInit(): void {
      this.loadAccountsWithTransactions();
    }
    

  ngAfterViewInit(): void {
    // Chart will be created after view init
  }
  loadAccountsWithTransactions() {
    this.accountService.getAllUsers().subscribe({
      next: (users: any[]) => {
        this.accounts = [];

        users.forEach(user => {
          if (user.accounts) {
            user.accounts.forEach((acc: Account) => {
              const account: Account = {
                accountNumber: acc.accountNumber,
                accountType: acc.accountType,
                balance: acc.balance,
                status: acc.status,
                userId: user.userId,
                outgoingTransactions: acc.outgoingTransactions,
                incomingTransactions: acc.incomingTransactions,
                branchName: acc.branchName || 'Main Branch',
                ifscCode: acc.ifscCode || 'BANK0001234',
                debitCardRequired: acc.debitCardRequired ?? false,
                netBankingEnabled: acc.netBankingEnabled ?? true
              };
              this.accounts.push(account);
            });
          }
        });
         this.createBarChart();
      },
      error: (err:any) => console.error(err)
    });
  }

  freezeAccount(account: Account) {
    this.accountService.freezeAccount(account.accountNumber).subscribe({
        next: () => account.status = 'FROZEN',
        
        error: err => console.error(err)
      });
      this.loadAccountsWithTransactions();
  }

  unfreezeAccount(account: Account) {
    this.accountService.unfreezeAccount(account.accountNumber)
      .subscribe({
        next: () => account.status = 'ACTIVE',
        error: err => console.error(err)
      });
      this.loadAccountsWithTransactions();
  }
  createBarChart(): void {
    const activeAccounts = this.accounts.filter(acc => acc.status === 'ACTIVE');
    const labels =activeAccounts.map(acc => acc.accountNumber);
    const data = activeAccounts.map(acc => acc.balance);

    if (this.chart) {
      this.chart.destroy(); // Destroy old chart instance if reloading
    }
this.chart = new Chart(this.barChartRef.nativeElement, {
  type: 'bar',
  data: {
    labels,
    datasets: [{
      label: 'Account Balance (INR)',
      data,
      backgroundColor: 'rgba(34, 100, 230, 0.7)',
      borderColor: 'rgba(34, 100, 230, 1)',
      borderWidth: 1,

      // ✅ Bar thickness control goes here (inside dataset)
      barPercentage: 0.9,         // controls how fat each bar is
      categoryPercentage: 0.8     // controls how close bars are to each other
    }]
  },
  options: {
    responsive: true,
    maintainAspectRatio: false,
    scales: {
      x: {
        beginAtZero: true
      },
      y: {
        beginAtZero: true
      }
    },
    plugins: {
      legend: {
        display: true,
        position: 'top'
      }
    }
  }
});

  }

}
