import { Component } from '@angular/core';
import { Authservice } from '../../service/authservice';
import { NavigationEnd, Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { filter } from 'rxjs';

@Component({
  selector: 'app-header',
  imports: [CommonModule,RouterLink],
  templateUrl: './header.html',
  styleUrl: './header.css',
})
export class Header {
  showStaticContent = true;
  constructor(private authService: Authservice,private route:Router) {
    this.route.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe((event: any) => {
        const hiddenRoutes = ['/login', '/register', '/dashboard', '/admin'];
        this.showStaticContent = !hiddenRoutes.some(route =>
          event.url.startsWith(route)
        );
      });
  }

  get isLoggedIn(): boolean {
  const hasToken = this.authService.getToken() !== null;
  console.log("isLoggedIn:", hasToken);
  return hasToken;
}


  onLogout() {
    this.authService.logout();
    
    this.route.navigate(['/login']);
  }

}
