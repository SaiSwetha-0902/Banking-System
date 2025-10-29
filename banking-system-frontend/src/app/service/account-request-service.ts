import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AccountRequest } from '../models/AccountRequest';


@Injectable({ providedIn: 'root' })
export class AccountRequestService {
  private baseUrl = 'http://localhost:8050/account-requests';

  constructor(private http: HttpClient) {}
submitRequest(request: AccountRequest): Observable<any> {
    return this.http.post(`${this.baseUrl}/create`, request);
  }

  getUserRequests(userId: number): Observable<AccountRequest[]> {
    return this.http.get<AccountRequest[]>(`${this.baseUrl}/user/${userId}`);
  }

  getPendingRequests(): Observable<AccountRequest[]> {
    return this.http.get<AccountRequest[]>(`${this.baseUrl}/pending`);
  }

  updateStatus(requestId: number, status: string): Observable<AccountRequest> {
    return this.http.put<AccountRequest>(`${this.baseUrl}/${requestId}/status`, null, {
      params: { status },
    });
  }


  getAllRequests(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/all`);
  }

  approveRequest(id: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/${id}/approve`, {});
  }

  rejectRequest(id: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/${id}/reject`, {});
  }
}
