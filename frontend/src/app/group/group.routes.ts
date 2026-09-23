import { Routes } from '@angular/router';
import { resolveGroup } from 'app/group/group.resolver';
import { canActivateAdmin } from 'app/group/guards/admin-guard.service';
import { canActivateGroup } from 'app/group/guards/group-guard.service';
import { canActivateGroupMembersAdmin } from 'app/group/guards/group-members-admin-guard.service';

/**
 * Child route configuration for the interest group feature area.
 *
 * All routes are nested under the `:id` path parameter, which identifies the
 * target group. The parent route lazily loads {@link GroupComponent} and
 * resolves the group data via {@link resolveGroup} before activation, using
 * the `pathParamsChange` strategy so guards and resolvers re-run whenever the
 * group id changes.
 *
 * The child routes lazily load the feature sub-route arrays (library, members,
 * forum, agenda, information, keywords, profiles, dynamic-properties, admin,
 * applicants, permissions and notifications) as well as the notification
 * status component. Access to each child is protected by route guards:
 * - `canActivateGroup` ensures the user may access the group.
 * - `canActivateAdmin` restricts administrative sections to group admins.
 * - `canActivateGroupMembersAdmin` restricts member/profile administration.
 */
export const groupRoutes: Routes = [
  {
    path: ':id',
    loadComponent: () =>
      import('app/group/group.component').then((m) => m.GroupComponent),
    resolve: {
      group: resolveGroup,
    },
    runGuardsAndResolvers: 'pathParamsChange',
    children: [
      {
        path: '',
        loadComponent: () =>
          import('app/group/dashboard/dashboard.component').then(
            (m) => m.DashboardComponent
          ),
        canActivate: [canActivateGroup],
      },
      {
        path: 'library',
        loadChildren: () =>
          import('./library/library.routes').then((m) => m.libraryRoutes),
      },
      {
        path: 'members',
        canActivate: [canActivateGroup],
        loadChildren: () =>
          import('./members/members.routes').then((m) => m.membersRoutes),
      },
      {
        path: 'forum',
        canActivate: [canActivateGroup],
        loadChildren: () =>
          import('./forum/forum.routes').then((m) => m.forumRoutes),
      },
      {
        path: 'agenda',
        canActivate: [canActivateGroup],
        loadChildren: () =>
          import('./agenda/agenda.routes').then((m) => m.agendaRoutes),
      },
      {
        path: 'information',
        canActivate: [canActivateGroup],
        loadChildren: () =>
          import('./information/information.routes').then(
            (m) => m.informationRoutes
          ),
      },
      {
        path: 'keywords',
        canActivate: [canActivateGroup, canActivateAdmin],
        loadChildren: () =>
          import('./keywords/keywords.routes').then((m) => m.keywordsRoutes),
      },
      {
        path: 'profiles',
        canActivate: [canActivateGroup, canActivateGroupMembersAdmin],
        loadChildren: () =>
          import('./profiles/profiles.routes').then((m) => m.profilesRoutes),
      },
      {
        path: 'dynamic-properties',
        canActivate: [canActivateGroup, canActivateAdmin],
        loadChildren: () =>
          import('./dynamic-properties/dynamic-properties.routes').then(
            (m) => m.dynPropRoutes
          ),
      },
      {
        path: 'admin',
        canActivate: [canActivateGroup, canActivateAdmin],
        loadChildren: () =>
          import('./admin/group-admin.routes').then((m) => m.groupAdminRoutes),
      },
      {
        path: 'applicants',
        canActivate: [canActivateGroup],
        loadChildren: () =>
          import('./applicants/applicants.routes').then(
            (m) => m.applicantsRoutes
          ),
      },
      {
        path: 'permissions',
        canActivate: [canActivateGroup],
        loadChildren: () =>
          import('./permissions/permissions.routes').then(
            (m) => m.permissionsRoutes
          ),
      },
      {
        path: 'notifications',
        canActivate: [canActivateGroup],
        loadChildren: () =>
          import('./notifications/notifications.routes').then(
            (m) => m.notificationsRoutes
          ),
      },
      {
        path: 'notification-status/:nodeId',
        loadComponent: () =>
          import('app/group/admin/notification-status/notification-status.component').then(
            (m) => m.NotificationStatusComponent
          ),
        canActivate: [canActivateGroup],
      },
    ],
  },
];
