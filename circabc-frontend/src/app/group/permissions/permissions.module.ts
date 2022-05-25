import { NgModule } from '@angular/core';

import { LibraryModule } from 'app/group/library/library.module';
import { AddPermissionsComponent } from 'app/group/permissions/add/add-permissions.component';
import { DropdownComponent } from 'app/group/permissions/dropdown/dropdown.component';
import { PermissionsRoutingModule } from 'app/group/permissions/permissions-routing.module';
import { PermissionsComponent } from 'app/group/permissions/permissions.component';
import { SharedModule } from 'app/shared/shared.module';

@NgModule({
  imports: [PermissionsRoutingModule, SharedModule, LibraryModule],
  exports: [],
  declarations: [
    PermissionsComponent,
    AddPermissionsComponent,
    DropdownComponent,
  ],
  providers: [],
})
export class PermissionsModule {}
