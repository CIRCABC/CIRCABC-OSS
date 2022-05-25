import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AutoUploadComponent } from 'app/group/admin/auto-upload/auto-upload.component';
import { DeleteGroupComponent } from 'app/group/admin/delete-group/delete-group.component';
import { DocumentLifecycleComponent } from 'app/group/admin/documents/document-lifecycle.component';
import { ExternalRepositoryComponent } from 'app/group/admin/external-repository/external-repository.component';
import { AdminGeneralComponent } from 'app/group/admin/general/admin-general.component';
import { GroupAdminComponent } from 'app/group/admin/group-admin.component';
import { LogComponent } from 'app/group/admin/log/log.component';
import { LogosComponent } from 'app/group/admin/logos/logos.component';
import { NotificationStatusComponent } from 'app/group/admin/notification-status/notification-status.component';
import { PasteNotificationsComponent } from 'app/group/admin/paste-notifications/paste-notifications.component';
import { AdminSecurityComponent } from 'app/group/admin/security/admin-security.component';
import { AdminSummaryComponent } from 'app/group/admin/summary/admin-summary.component';
import { GroupDeleteGuard } from 'app/group/guards/group-delete-guard.service';

const routes: Routes = [
  {
    path: '',
    component: GroupAdminComponent,
    children: [
      { path: '', redirectTo: 'general', pathMatch: 'full' },
      { path: 'general', component: AdminGeneralComponent },
      { path: 'security', component: AdminSecurityComponent },
      { path: 'summary', component: AdminSummaryComponent },
      { path: 'documents', component: DocumentLifecycleComponent },
      { path: 'auto-upload', component: AutoUploadComponent },
      { path: 'paste-notifications', component: PasteNotificationsComponent },
      { path: 'notification-status', component: NotificationStatusComponent },
      { path: 'log', component: LogComponent },
      { path: 'logos', component: LogosComponent },
      { path: 'external-repository', component: ExternalRepositoryComponent },
      {
        path: 'delete',
        component: DeleteGroupComponent,
        canActivate: [GroupDeleteGuard],
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class GroupAdminRoutingModule {}
