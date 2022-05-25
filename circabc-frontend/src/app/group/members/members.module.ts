import { NgModule } from '@angular/core';
import { ServiceAccessGuard } from 'app/group/guards/service-guard.service';
import { BulkInviteComponent } from 'app/group/members/bulk-invite/bulk-invite.component';
import { ChangeProfilesMultipleComponent } from 'app/group/members/change-profiles-multiple/change-profiles-multiple.component';
import { ChangeUserProfileComponent } from 'app/group/members/change-user-profile/change-user-profile.component';
import { ContactComponent } from 'app/group/members/contact/contact.component';
import { EditExpirationComponent } from 'app/group/members/edit-expiration/edit-expiration.component';
import { MemberAccountComponent } from 'app/group/members/member-account/member-account.component';
import { MemberCardComponent } from 'app/group/members/member-account/member-card/member-card.component';
import { MembersRoutingModule } from 'app/group/members/members-routing.module';
import { MembersComponent } from 'app/group/members/members.component';
import { UninviteMultipleComponent } from 'app/group/members/uninvite-multiple/uninvite-multiple.component';
import { UninviteUserComponent } from 'app/group/members/uninvite/uninvite-user.component';
import { InviteUserComponent } from 'app/group/members/invite-user/invite-user.component';
import { PrimengComponentsModule } from 'app/primeng-components/primeng-components.module';
import { SharedModule } from 'app/shared/shared.module';
import { MembersDropdownComponent } from 'app/group/members/members-dropdown.component';
import { MaterialComponentsModule } from 'app/material-components/material-components.module';

@NgModule({
  imports: [
    MembersRoutingModule,
    SharedModule,
    PrimengComponentsModule,
    MaterialComponentsModule,
  ],
  exports: [],
  declarations: [
    BulkInviteComponent,
    MembersComponent,
    InviteUserComponent,
    UninviteUserComponent,
    ContactComponent,
    UninviteMultipleComponent,
    MemberAccountComponent,
    MemberCardComponent,
    ChangeUserProfileComponent,
    ChangeProfilesMultipleComponent,
    EditExpirationComponent,
    MembersDropdownComponent,
  ],
  providers: [ServiceAccessGuard],
})
export class MembersModule {}
