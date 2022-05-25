import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { ServiceAccessGuard } from 'app/group/guards/service-guard.service';
import { BulkInviteComponent } from 'app/group/members/bulk-invite/bulk-invite.component';
import { ContactComponent } from 'app/group/members/contact/contact.component';
import { MemberAccountComponent } from 'app/group/members/member-account/member-account.component';
import { MembersComponent } from 'app/group/members/members.component';

const membersRoutes: Routes = [
  { path: '', component: MembersComponent, canActivate: [ServiceAccessGuard] },
  { path: 'contact', component: ContactComponent },
  { path: 'account/:userid', component: MemberAccountComponent },
  { path: 'bulk-invite/:igId', component: BulkInviteComponent },
];

@NgModule({
  imports: [RouterModule.forChild(membersRoutes)],
  exports: [RouterModule],
})
export class MembersRoutingModule {}
