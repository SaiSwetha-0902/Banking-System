import { Component } from '@angular/core';

import { CommonModule, DatePipe } from '@angular/common';
import { AuditLog } from '../../../models/audit-log';
import { Accountservice } from '../../../service/accountservice';

@Component({
  selector: 'app-audit-logs',
  imports: [DatePipe,CommonModule],
  templateUrl: './audit-logs.html',
  styleUrl: './audit-logs.css',
})
export class AuditLogs {
   auditLogs: AuditLog[] = [];
  baseUrl = 'http://localhost:8050/admin'; // backend base URL

  constructor(private accountService : Accountservice) {}

  ngOnInit(): void {
    this.loadAuditLogs();
  }

  loadAuditLogs() {
    this.accountService.getAllLogs().subscribe({
      next: (logs:any) => this.auditLogs = logs,
      error: (err:any) => console.error(err)
    });
  }

}
