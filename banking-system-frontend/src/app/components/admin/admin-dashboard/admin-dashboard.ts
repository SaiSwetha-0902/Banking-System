import { Component } from '@angular/core';
import { ActivatedRoute, RouterLink, RouterOutlet } from '@angular/router';
import { Alert } from '../../alert/alert';

@Component({
  selector: 'app-admin-dashboard',
  imports: [RouterOutlet,RouterLink,Alert],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.css',
})
export class AdminDashboard {
 constructor(private route : ActivatedRoute) {}

}
