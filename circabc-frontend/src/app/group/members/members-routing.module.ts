import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { canActivateService } from 'app/group/guards/service-guard.service';

export const membersRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('app/group/members/members.component').then(
        (m) => m.MembersComponent
      ),
    canActivate: [canActivateService],
  },
  {
    path: 'contact',
    loadComponent: () =>
      import('app/group/members/contact/contact.component').then(
        (m) => m.ContactComponent
      ),
  },
  {
    path: 'account/:userid',
    loadComponent: () =>
      import('app/group/members/member-account/member-account.component').then(
        (m) => m.MemberAccountComponent
      ),
  },
  {
    path: 'bulk-invite/:igId',
    loadComponent: () =>
      import('app/group/members/bulk-invite/bulk-invite.component').then(
        (m) => m.BulkInviteComponent
      ),
  },
];

@NgModule({
  imports: [RouterModule.forChild(membersRoutes)],
  exports: [RouterModule],
})
export class MembersRoutingModule {}
