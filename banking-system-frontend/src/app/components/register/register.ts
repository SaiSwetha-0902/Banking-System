import { Component } from '@angular/core';

import { Router, RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Authservice } from '../../service/authservice';

@Component({
  selector: 'app-register',
  imports: [RouterOutlet,CommonModule,FormsModule],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class Register {
  email: string = '';
  password: string = '';
  confirmPassword: string = '';
  message: string = '';

   constructor(private authService: Authservice, private router: Router) {}
  
  register() {
    if (this.password !== this.confirmPassword) {
      this.message = 'Passwords do not match';
      return;
    }

    this.authService.register(this.email, this.password).subscribe({
      next: (res: any) => {
        // Backend returns a plain string message
        this.message = res; // e.g., "User registered successfully"
        if (res === 'User registered successfully') {
          // Navigate to login automatically
          this.router.navigate(['/login']);
        }
      },
      error: (err) => {
        // Backend returns error message as plain string
        this.message = err.error; // e.g., "Email already exists"
      }
    });
  }

}
