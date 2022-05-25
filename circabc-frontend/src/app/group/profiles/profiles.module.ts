import { NgModule } from '@angular/core';

import { CreateProfileComponent } from 'app/group/profiles/create/create-profile.component';
import { PermissionDescriptorComponent } from 'app/group/profiles/create/permission-descriptor/permission-descriptor.component';
import { DeleteProfileComponent } from 'app/group/profiles/delete/delete-profile.component';
import { ImportProfileComponent } from 'app/group/profiles/import/import-profile.component';
import { ProfilesRoutingModule } from 'app/group/profiles/profiles-routing.module';
import { ProfilesComponent } from 'app/group/profiles/profiles.component';
import { SharedModule } from 'app/shared/shared.module';
import { PrimengComponentsModule } from 'app/primeng-components/primeng-components.module';
import { MaterialComponentsModule } from 'app/material-components/material-components.module';

@NgModule({
  imports: [
    ProfilesRoutingModule,
    SharedModule,
    PrimengComponentsModule,
    MaterialComponentsModule,
  ],
  exports: [],
  declarations: [
    ProfilesComponent,
    CreateProfileComponent,
    DeleteProfileComponent,
    ImportProfileComponent,
    PermissionDescriptorComponent,
  ],
  providers: [],
})
export class ProfilesModule {}
