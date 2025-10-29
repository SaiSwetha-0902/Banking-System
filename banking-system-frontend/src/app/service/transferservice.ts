import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Transaction } from '../models/transaction';

export interface TransactionRequest {
  fromAccountNumber?: string;
  toAccountNumber?: string;
  amount: number;
}

@Injectable({
  providedIn: 'root'
})
export class TransactionService {
  private baseUrl = 'http://localhost:8050/api/transactions'; // backend URL

  constructor(private http: HttpClient) {}

  deposit(toAccountNumber: string, amount: number): Observable<any> {
    // Path variables as in backend
    return this.http.post(`${this.baseUrl}/deposit/${toAccountNumber}/${amount}`, {});
  }

  withdraw(fromAccountNumber: string, amount: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/withdraw/${fromAccountNumber}/${amount}`, {});
  }

  transfer(fromAccountNumber: string, toAccountNumber: string, amount: number): Observable<any> {
    return this.http.post(
      `${this.baseUrl}/transfer/${fromAccountNumber}/${toAccountNumber}/${amount}`, {}
    );
  }

  getTransactionsOfAccount(accountNumber: string): Observable<any> {
    return this.http.get(`${this.baseUrl}/account/${accountNumber}`);
  }

  getSuspiciousTransactions(): Observable<Transaction[]> {
  return this.http.get<Transaction[]>(`${this.baseUrl}/admin/transactions/suspicious`);
}
  
}
