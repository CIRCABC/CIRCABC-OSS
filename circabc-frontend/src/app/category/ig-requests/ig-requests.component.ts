import { Component } from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';
import { GroupRequestsDeleteComponent } from './group-requests-delete/group-requests-delete.component';
import { GroupRequestsCreateComponent } from './group-requests-create/group-requests-create.component';
import { MatTabsModule } from '@angular/material/tabs';

@Component({
  selector: 'cbc-ig-requets',
  imports: [
    TranslocoModule,
    GroupRequestsDeleteComponent,
    GroupRequestsCreateComponent,
    MatTabsModule,
  ],
  templateUrl: './ig-requests.component.html',
})
export class IgRequestsComponent {}
