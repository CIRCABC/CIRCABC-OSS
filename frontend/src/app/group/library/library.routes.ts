import { Routes } from '@angular/router';
import { canActivateNodeAccess } from 'app/group/guards/access-guard.service';
// LibraryComponent is lazy-loaded via loadComponent in the route below

import { canActivateAdmin } from 'app/group/guards/admin-guard.service';
import { canActivateNodeEdit } from 'app/group/guards/edit-guard.service';
import { canDeactivateUploadForm } from 'app/group/guards/leave-file-upload-guard';

/**
 * Route configuration for the group Library feature area.
 *
 * Every route is keyed by a `:nodeId` path parameter identifying the library
 * node (space, document or folder) being acted upon, and each screen is
 * lazy-loaded via `loadComponent` so its code is only fetched on demand.
 *
 * Access to each route is protected by node-level guards:
 * - `canActivateNodeAccess` — read access to the node (browse, details).
 * - `canActivateNodeEdit` — edit access (edit, add translation, auto-upload,
 *   upload).
 * - `canActivateAdmin` — administrative access (manage space sharing).
 * The upload route additionally uses `canDeactivateUploadForm` (`canDeactivate`)
 * to warn/prevent navigation away while an upload is in progress.
 *
 * Routes:
 * - `:nodeId` → `LibraryComponent`: browse a library node's contents.
 * - `:nodeId/details` → `DetailsComponent`: view node details/metadata.
 * - `:nodeId/details/:versionLabel` → `DetailsComponent`: view details for a
 *   specific version of the node.
 * - `:nodeId/edit` → `EditNodeComponent`: edit node metadata/properties.
 * - `:nodeId/translations/add` → `AddTranslationComponent`: add a translation
 *   for the node.
 * - `:nodeId/auto-upload` → `AutoUploadLibraryComponent`: configure/perform
 *   automatic uploads into the node.
 * - `:nodeId/manage-space-sharing` → `ManageSpaceSharingComponent`: manage
 *   sharing settings for a space (admin only).
 * - `:nodeId/upload` → `UploadFormComponent`: upload files into the node.
 */
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
      import('app/group/library/add-translation/add-translation.component').then(
        (m) => m.AddTranslationComponent
      ),
    canActivate: [canActivateNodeEdit],
  },
  {
    path: ':nodeId/auto-upload',
    loadComponent: () =>
      import('app/group/library/auto-upload-library/auto-upload-library.component').then(
        (m) => m.AutoUploadLibraryComponent
      ),
    canActivate: [canActivateNodeEdit],
  },
  {
    path: ':nodeId/manage-space-sharing',
    loadComponent: () =>
      import('app/group/library/manage-space-sharing/manage-space-sharing.component').then(
        (m) => m.ManageSpaceSharingComponent
      ),
    canActivate: [canActivateAdmin],
  },
  {
    path: ':nodeId/upload',
    loadComponent: () =>
      import('app/group/library/upload-form/upload-form.component').then(
        (m) => m.UploadFormComponent
      ),
    canActivate: [canActivateNodeEdit],
    canDeactivate: [canDeactivateUploadForm],
  },
];
