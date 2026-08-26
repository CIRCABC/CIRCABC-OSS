import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { canActivateNodeAccess } from 'app/group/guards/access-guard.service';

import { canActivateAdmin } from 'app/group/guards/admin-guard.service';
import { canActivateNodeEdit } from 'app/group/guards/edit-guard.service';
import { LeaveFileUploadGuard } from 'app/group/guards/leave-file-upload-guard';

export const libraryRoutes: Routes = [
  {
    path: ':nodeId',
    loadComponent: () =>
      import('app/group/library/library.component').then(
        (m) => m.LibraryComponent
      ),
    canActivate: [canActivateNodeAccess],
  },
  {
    path: ':nodeId/details',
    loadComponent: () =>
      import('app/group/library/details/details.component').then(
        (m) => m.DetailsComponent
      ),
    canActivate: [canActivateNodeAccess],
  },
  {
    path: ':nodeId/details/:versionLabel',
    loadComponent: () =>
      import('app/group/library/details/details.component').then(
        (m) => m.DetailsComponent
      ),
    canActivate: [canActivateNodeAccess],
  },
  {
    path: ':nodeId/edit',
    loadComponent: () =>
      import('app/group/library/edit/edit-node.component').then(
        (m) => m.EditNodeComponent
      ),
    canActivate: [canActivateNodeEdit],
  },
  {
    path: ':nodeId/translations/add',
    loadComponent: () =>
      import(
        'app/group/library/add-translation/add-translation.component'
      ).then((m) => m.AddTranslationComponent),
    canActivate: [canActivateNodeEdit],
  },
  {
    path: ':nodeId/auto-upload',
    loadComponent: () =>
      import(
        'app/group/library/auto-upload-library/auto-upload-library.component'
      ).then((m) => m.AutoUploadLibraryComponent),
    canActivate: [canActivateNodeEdit],
  },
  {
    path: ':nodeId/manage-space-sharing',
    loadComponent: () =>
      import(
        'app/group/library/manage-space-sharing/manage-space-sharing.component'
      ).then((m) => m.ManageSpaceSharingComponent),
    canActivate: [canActivateAdmin],
  },
  {
    path: ':nodeId/upload',
    loadComponent: () =>
      import('app/group/library/upload-form/upload-form.component').then(
        (m) => m.UploadFormComponent
      ),
    canActivate: [canActivateNodeEdit],
    canDeactivate: [LeaveFileUploadGuard],
  },
];

@NgModule({
  imports: [RouterModule.forChild(libraryRoutes)],
  exports: [RouterModule],
})
export class LibraryRoutingModule {}
