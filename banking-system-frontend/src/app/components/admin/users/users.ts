import { Component } from '@angular/core';

import { User } from '../../../models/user';
import { CommonModule } from '@angular/common';
import { Accountservice } from '../../../service/accountservice';


@Component({
  selector: 'app-users',
  imports: [CommonModule],
  templateUrl: './users.html',
  styleUrl: './users.css',
})
export class Users {

    users?: User[];
  
    constructor(private accountService: Accountservice) {}
  
    ngOnInit(): void {
      this.accountService.getAllUsers().subscribe({
        next: (users) => {
          this.users = users;
        },
        error: (err) => console.error(err)
      });
    }

}
