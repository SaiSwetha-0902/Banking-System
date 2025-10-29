import { Component, EventEmitter, Output } from '@angular/core';
import { Transaction } from '../../models/transaction';

import { CommonModule } from '@angular/common';
import { AlertService } from '../../service/alert';

@Component({
  selector: 'app-alert',
  imports: [CommonModule],
  templateUrl: './alert.html',
  styleUrl: './alert.css',
})
export class Alert {
suspiciousTransactions: Transaction[] = [];

  constructor(private alertService: AlertService) {}
ngOnInit(): void {
     this.alertService.getAlerts().subscribe((txns:any) => {
      console.log('Suspicious transactions:', txns);
      if (txns && txns.length > 0) {
        
        this.suspiciousTransactions = txns;
      
      }
    
    });
  
}

}
