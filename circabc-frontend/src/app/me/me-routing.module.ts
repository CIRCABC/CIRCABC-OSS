import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from 'app/core/auth-guard.service';
import { AccountComponent } from 'app/me/account/account.component';
import { UserDashboardComponent } from 'app/me/dashboard/user-dashboard.component';
import { MeComponent } from 'app/me/me.component';
import { MyCalendarComponent } from 'app/me/my-calendar/my-calendar.component';
import { RolesComponent } from 'app/me/roles/roles.component';

const meRoutes: Routes = [
  {
    path: '',
    component: MeComponent,
    canActivate: [AuthGuard],
    children: [
      { path: '', component: UserDashboardComponent },
      { path: 'calendar', component: MyCalendarComponent },
      { path: 'account', component: AccountComponent },
      { path: 'roles', component: RolesComponent },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(meRoutes)],
  exports: [RouterModule],
})
export class MeRoutingModule {}
