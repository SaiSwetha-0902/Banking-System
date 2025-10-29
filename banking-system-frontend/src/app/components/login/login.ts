import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { Authservice } from '../../service/authservice';
import { Request } from '../../models/request';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { loginResponse } from '../../models/LoginResponse';

@Component({
  selector: 'app-login',
  standalone: true,
  imports : [CommonModule,FormsModule],
  templateUrl: './login.html',
  styleUrls: ['./login.css'],
})
export class Login {
  request: Request = { email: '', password: '' };
  response?: loginResponse;
  message: string = '';

  constructor(private authService: Authservice, private router: Router) {}

  login() {
    if (!this.request.email || !this.request.password) {
      this.message = 'Please enter both email and password.';
      return;
    }

    this.authService.login(this.request.email, this.request.password).subscribe({
      next: (res) => {
        this.response = res;

        // Save JWT token
        if (res.token) {
          
          this.authService.storeToken(res.token);
        }

        this.message = '';

        // ✅ Determine role-based route
        const roles = res.allRoles?.map((r:{ roleName: string }) => r.roleName.toUpperCase()) || [];

        if (roles.includes('ADMIN')) {
          this.router.navigate(['/admin']);
        } else {
          this.router.navigate(['/dashboard',res.userId]);
        }
      },
      error: (err) => {
        console.error('Login error:', err);
        if (err.status === 401) {
          this.message = 'Bad credentials. Please check your email or password.';
        } else {
          this.message = 'Login failed. Please try again later.';
        }
      },
    });
  }

  logout() {
    this.authService.logout();
    this.response = undefined;
    this.router.navigate(['/register']);
  }
}
