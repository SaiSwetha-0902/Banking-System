import { Component, ChangeDetectorRef, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router, RouterLink, RouterOutlet, NavigationEnd } from '@angular/router';
import { Subscription } from 'rxjs';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-customer-dashboard',
  standalone: true,
  imports: [RouterOutlet, RouterLink],
  templateUrl: './customer-dashboard.html',
  styleUrls: ['./customer-dashboard.css'],
})
export class CustomerDashboard implements OnDestroy {
  userId: number | undefined;
  private subs: Subscription[] = [];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {
    // ✅ Listen to route param changes
    this.subs.push(
      this.route.paramMap.subscribe((params) => {
        this.userId = Number(params.get('userId'));
        this.cdr.detectChanges();
      })
    );

    // ✅ Detect when user navigates between child routes (Profile / Accounts / Transactions)
    this.subs.push(
      this.router.events
        .pipe(filter((e) => e instanceof NavigationEnd))
        .subscribe(() => {
          this.cdr.detectChanges(); // 👈 Forces full view refresh on every click
        })
    );
  }

  ngOnDestroy() {
    this.subs.forEach((s) => s.unsubscribe());
  }
}
