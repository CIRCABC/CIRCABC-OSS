import { ChangeDetectionStrategy, Component } from '@angular/core';
import { MatTabsModule } from '@angular/material/tabs';
import { TranslocoModule } from '@jsverse/transloco';
import { GroupRequestsCreateComponent } from './group-requests-create/group-requests-create.component';
import { GroupRequestsDeleteComponent } from './group-requests-delete/group-requests-delete.component';

/**
 * Container component for managing interest group (IG) requests within a
 * category.
 *
 * Renders a tabbed interface (via Angular Material tabs) that groups together
 * the two administrative actions available for interest group requests:
 * - creation requests, delegated to {@link GroupRequestsCreateComponent}
 * - deletion requests, delegated to {@link GroupRequestsDeleteComponent}
 *
 * The component itself holds no state and exposes no inputs or outputs; it acts
 * purely as a layout host that composes the child request components and
 * provides Transloco-backed translations for the tab labels. Change detection
 * is OnPush as configured on the decorator.
 */
@Component({
  selector: 'cbc-ig-requets',
  imports: [
    TranslocoModule,
    GroupRequestsDeleteComponent,
    GroupRequestsCreateComponent,
    MatTabsModule,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './ig-requests.component.html',
})
export class IgRequestsComponent {}
