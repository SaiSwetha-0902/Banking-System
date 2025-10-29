import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, timer, of } from 'rxjs';
import { switchMap, catchError } from 'rxjs/operators';
import { Transaction } from '../models/transaction';

@Injectable({
  providedIn: 'root'
})
export class AlertService {
  private baseUrl = 'http://localhost:8050/admin/transactions/suspicious';
  private alertsSubject = new BehaviorSubject<Transaction[]>([]);

  constructor(private http: HttpClient) {
    // Start polling every 1 minute
    timer(0, 60 * 1000)
      .pipe(
        switchMap(() => this.fetchSuspiciousTransactions()),
        catchError(err => {
          console.error('Error fetching suspicious transactions:', err);
          return of([]);  // fallback empty result
        })
      )
      .subscribe(txns => {
        this.alertsSubject.next(txns);
      });
  }

  // Fetch suspicious transactions from backend
  fetchSuspiciousTransactions(): Observable<Transaction[]> {
    return this.http.get<Transaction[]>(this.baseUrl);
  }

  // Public observable stream for components
  getAlerts(): Observable<Transaction[]> {
    return this.alertsSubject.asObservable();
  }

  // Convenience method for one-time fetch (manual trigger)
  getSuspiciousTransactions(): Observable<Transaction[]> {
    return this.http.get<Transaction[]>(this.baseUrl);
  }
}
