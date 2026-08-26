import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CookieService } from 'ngx-cookie-service';

export const welcomeRoutes: Routes = [
  {
    path: 'welcome',
    loadComponent: () =>
      import('app/welcome/welcome.component').then((m) => m.WelcomeComponent),
  },
];

@NgModule({
  imports: [RouterModule.forChild(welcomeRoutes)],
  exports: [RouterModule],
  providers: [CookieService],
})
export class WelcomeRoutingModule {}
