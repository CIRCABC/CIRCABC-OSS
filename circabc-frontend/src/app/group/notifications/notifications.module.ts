import { NgModule } from '@angular/core';

import { NotificationsRoutingModule } from 'app/group/notifications/notifications-routing.module';
import { NotificationsComponent } from 'app/group/notifications/notifications.component';
import { SharedModule } from 'app/shared/shared.module';

@NgModule({
  imports: [NotificationsRoutingModule, SharedModule],
  exports: [],
  declarations: [NotificationsComponent],
  providers: [],
})
export class NotificationsModule {}
