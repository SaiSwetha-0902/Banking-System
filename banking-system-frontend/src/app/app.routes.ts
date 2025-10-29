import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CustomerDashboard } from './components/customer/customer-dashboard/customer-dashboard';
import { CustomerAccounts } from './components/customer/customer-accounts/customer-accounts';
import { CustomerProfile } from './components/customer/customer-profile/customer-profile';
import { CustomerTransaction } from './components/customer/customer-transaction/customer-transaction';
import { AdminDashboard } from './components/admin/admin-dashboard/admin-dashboard';
import { Users } from './components/admin/users/users';
import { Accounts } from './components/admin/accounts/accounts';
import { AuditLogs } from './components/admin/audit-logs/audit-logs';
import { Transactions } from './components/admin/transactions/transactions';

import { Register } from './components/register/register';
import { Login } from './components/login/login';
import { Header } from './components/header/header';
import { PendingRequest } from './components/admin/pending-request/pending-request';




export const routes: Routes = [
  {
    path: 'dashboard/:userId',
    component: CustomerDashboard,
    children: [
     
      { path: 'profile/:userId', component: CustomerProfile,runGuardsAndResolvers: 'always'  },
      { path: 'accounts/:userId', component: CustomerAccounts },
      { path: 'transactions/:userId', component: CustomerTransaction },
      
    ]
  },
  { path: 'admin', component: AdminDashboard,
    children: [
      { path: 'users', component: Users },
      { path: 'accounts', component: Accounts },
      { path: 'audit-logs', component: AuditLogs },
      {path:'transactions',component:Transactions},
      {path:'pending',component:PendingRequest},
      { path: '', redirectTo: 'users', pathMatch: 'full' }
    ]
  },
  {path:'login',component: Login},
  {path:'register',component:Register},
  {path:'header',component:Header}
  
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}
