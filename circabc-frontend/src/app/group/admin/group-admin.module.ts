import { NgModule } from '@angular/core';
import { AddConfigurationComponent } from 'app/group/admin/auto-upload/add-configuration/add-configuration.component';
import { AutoUploadComponent } from 'app/group/admin/auto-upload/auto-upload.component';
import { DeleteGroupComponent } from 'app/group/admin/delete-group/delete-group.component';
import { DeletedItemsComponent } from 'app/group/admin/documents/deleted-items/deleted-items.component';
import { DocumentLifecycleComponent } from 'app/group/admin/documents/document-lifecycle.component';
import { ExpiredItemsComponent } from 'app/group/admin/documents/expired-items/expired-items.component';
import { PurgeItemComponent } from 'app/group/admin/documents/purge-item/purge-item.component';
import { RestoreItemComponent } from 'app/group/admin/documents/restore-item/restore-item.component';
import { ExternalRepositoryComponent } from 'app/group/admin/external-repository/external-repository.component';
import { AdminGeneralComponent } from 'app/group/admin/general/admin-general.component';
import { GroupAdminRoutingModule } from 'app/group/admin/group-admin-routing.module';
import { GroupAdminComponent } from 'app/group/admin/group-admin.component';
import { LogComponent } from 'app/group/admin/log/log.component';
import { AddGroupLogoComponent } from 'app/group/admin/logos/add-group-logo/add-group-logo.component';
import { LogosComponent } from 'app/group/admin/logos/logos.component';
import { NotificationStatusComponent } from 'app/group/admin/notification-status/notification-status.component';
import { PasteNotificationsComponent } from 'app/group/admin/paste-notifications/paste-notifications.component';
import { AdminSecurityComponent } from 'app/group/admin/security/admin-security.component';
import { AdminSummaryComponent } from 'app/group/admin/summary/admin-summary.component';
import { StructureTreeComponent } from 'app/group/admin/summary/structure-tree/structure-tree.component';
import { BreadcrumbModule } from 'app/group/breadcrumb/breadcrumb.module';
import { GroupDeleteGuard } from 'app/group/guards/group-delete-guard.service';
import { MaterialComponentsModule } from 'app/material-components/material-components.module';
import { PrimengComponentsModule } from 'app/primeng-components/primeng-components.module';
import { SharedModule } from 'app/shared/shared.module';
import { UpdateExpiredDateComponent } from './documents/expired-items/update-expired-date/update-expired-date.component';
import { ExternalRepositoryHistoryComponent } from './external-repository/ext-repo-history/cbc-external-repository-history.component';
import { ExternalRepositoryPropertiesComponent } from './external-repository/ext-repo-properties/external-repository-properties.component';

@NgModule({
  imports: [
    GroupAdminRoutingModule,
    PrimengComponentsModule,
    MaterialComponentsModule,
    SharedModule,
    BreadcrumbModule,
  ],
  exports: [],
  declarations: [
    AddConfigurationComponent,
    AdminSummaryComponent,
    AutoUploadComponent,
    DeletedItemsComponent,
    DocumentLifecycleComponent,
    ExternalRepositoryComponent,
    ExternalRepositoryHistoryComponent,
    ExternalRepositoryPropertiesComponent,
    ExpiredItemsComponent,
    UpdateExpiredDateComponent,
    GroupAdminComponent,
    PurgeItemComponent,
    RestoreItemComponent,
    AdminGeneralComponent,
    AdminSecurityComponent,
    StructureTreeComponent,
    PasteNotificationsComponent,
    NotificationStatusComponent,
    LogComponent,
    LogosComponent,
    AddGroupLogoComponent,
    DeleteGroupComponent,
  ],
  providers: [GroupDeleteGuard],
})
export class GroupAdminModule {}
