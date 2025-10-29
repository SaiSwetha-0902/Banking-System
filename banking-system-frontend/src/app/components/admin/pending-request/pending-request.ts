import { Component } from '@angular/core';
import { AccountRequest } from '../../../models/AccountRequest';
import { AccountRequestService } from '../../../service/account-request-service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-pending-request',
  imports: [CommonModule],
  templateUrl: './pending-request.html',
  styleUrl: './pending-request.css',
})
export class PendingRequest {
  pendingRequests: AccountRequest[] = [];

  constructor(private requestService: AccountRequestService) {}

  ngOnInit() {
    this.loadPending();
  }

  loadPending() {
    this.requestService.getPendingRequests().subscribe(data => this.pendingRequests = data);
  }

  updateStatus(id: number | undefined, status: string) {
    if (!id) {
    console.error('Request ID missing');
    return;
  }
    this.requestService.updateStatus(id, status).subscribe(() => this.loadPending());
    this.loadPending();
  }

}
