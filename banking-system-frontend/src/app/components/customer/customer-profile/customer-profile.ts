import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';


import { ActivatedRoute } from '@angular/router';
import { User } from '../../../models/user';
import { Account } from '../../../models/account';
import { Accountservice } from '../../../service/accountservice';


@Component({
  selector: 'app-customer-profile',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './customer-profile.html',
  styleUrls: ['./customer-profile.css']  // fixed typo
})
export class CustomerProfile {
  userId : number | undefined;
  


  user?: User;
    accounts: Account[] = [];

  constructor(private accountService: Accountservice, private route: ActivatedRoute) {}

  ngOnInit(): void {
    this.userId = Number(this.route.snapshot.paramMap.get('userId'));

    this.accountService.getAllUsers().subscribe({
      next: (users) => {
        this.user = users.find(u => u.userId === this.userId);
        this.accounts = this.user?.accounts || [];
      },
      error: (err) => console.error(err)
    });
  }
}
