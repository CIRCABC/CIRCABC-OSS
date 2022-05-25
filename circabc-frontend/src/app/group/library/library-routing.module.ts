import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { NodeAccessGuard } from 'app/group/guards/access-guard.service';
import { AdminGuard } from 'app/group/guards/admin-guard.service';
import { NodeEditGuard } from 'app/group/guards/edit-guard.service';
import { LeaveFileUploadGuard } from 'app/group/guards/leave-file-upload-guard';
import { AddTranslationComponent } from 'app/group/library/add-translation/add-translation.component';
import { AutoUploadLibraryComponent } from 'app/group/library/auto-upload-library/auto-upload-library.component';
import { DetailsComponent } from 'app/group/library/details/details.component';
import { EditNodeComponent } from 'app/group/library/edit/edit-node.component';
import { LibraryComponent } from 'app/group/library/library.component';
import { ManageSpaceSharingComponent } from 'app/group/library/manage-space-sharing/manage-space-sharing.component';
import { UploadFormComponent } from 'app/group/library/upload-form/upload-form.component';

const libraryRoutes: Routes = [
  {
    path: ':nodeId',
    component: LibraryComponent,
  },
  {
    path: ':nodeId/details',
    component: DetailsComponent,
    canActivate: [NodeAccessGuard],
  },
  {
    path: ':nodeId/details/:versionLabel',
    component: DetailsComponent,
    canActivate: [NodeAccessGuard],
  },
  {
    path: ':nodeId/edit',
    component: EditNodeComponent,
    canActivate: [NodeEditGuard],
  },
  {
    path: ':nodeId/translations/add',
    component: AddTranslationComponent,
    canActivate: [NodeEditGuard],
  },
  {
    path: ':nodeId/auto-upload',
    component: AutoUploadLibraryComponent,
    canActivate: [NodeEditGuard],
  },
  {
    path: ':nodeId/manage-space-sharing',
    component: ManageSpaceSharingComponent,
    canActivate: [AdminGuard],
  },
  {
    path: ':nodeId/upload',
    component: UploadFormComponent,
    canActivate: [NodeEditGuard],
    canDeactivate: [LeaveFileUploadGuard],
  },
];

@NgModule({
  imports: [RouterModule.forChild(libraryRoutes)],
  exports: [RouterModule],
})
export class LibraryRoutingModule {}
