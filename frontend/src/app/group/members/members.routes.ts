import { Routes } from '@angular/router';

import { canActivateService } from 'app/group/guards/service-guard.service';

/**
 * Lazy-loaded route configuration for the group "Members" feature area.
 *
 * Each route uses `loadComponent` to lazily import a standalone component,
 * keeping the members feature isolated in its own bundle. Routes cover:
 * - `''` — the members listing view ({@link MembersComponent}), protected by
 *   {@link canActivateService} to ensure the members service is accessible.
 * - `'contact'` — the contact view ({@link ContactComponent}).
 * - `'account/:userid'` — a single member's account detail view
 *   ({@link MemberAccountComponent}), parameterised by `userid`.
 * - `'bulk-invite/:igId'` — the bulk invitation view
 *   ({@link BulkInviteComponent}), parameterised by the interest group id `igId`.
 */
export const membersRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('app/group/members/members.component').then(
        (m) => m.MembersComponent
      ),
    canActivate: [canActivateService],
  },
  {
    path: 'contact',
    loadComponent: () =>
      import('app/group/members/contact/contact.component').then(
        (m) => m.ContactComponent
      ),
  },
  {
    path: 'account/:userid',
    loadComponent: () =>
      import('app/group/members/member-account/member-account.component').then(
        (m) => m.MemberAccountComponent
      ),
  },
  {
    path: 'bulk-invite/:igId',
    loadComponent: () =>
      import('app/group/members/bulk-invite/bulk-invite.component').then(
        (m) => m.BulkInviteComponent
      ),
  },
];
