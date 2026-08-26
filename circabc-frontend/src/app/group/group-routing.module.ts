import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { GroupResolver } from 'app/group/group.resolver';
import { canActivateAdmin } from 'app/group/guards/admin-guard.service';
import { canActivateGroup } from 'app/group/guards/group-guard.service';
import { canActivateGroupMembersAdmin } from 'app/group/guards/group-members-admin-guard.service';
import { libraryRoutes } from './library/library-routing.module';
import { membersRoutes } from './members/members-routing.module';
import { forumRoutes } from './forum/forum-routing.module';
import { agendaRoutes } from './agenda/agenda-routing.module';
import { informationRoutes } from './information/information-routing.module';
import { keywordsRoutes } from './keywords/keywords-routing.module';
import { dynPropRoutes } from './dynamic-properties/dynamic-properties-routing.module';
import { groupAdminRoutes } from './admin/group-admin-routing.module';
import { applicantsRoutes } from './applicants/applicants-routing.module';
import { profilesRoutes } from './profiles/profiles-routing.module';
import { permissionsRoutes } from './permissions/permissions-routing.module';
import { notificationsRoutes } from './notifications/notifications-routing.module';

export const groupRoutes: Routes = [
  {
    path: ':id',
    loadComponent: () => import('app/group/group.component').then(m => m.GroupComponent),
    resolve: {
      group: GroupResolver,
    },
    runGuardsAndResolvers: 'always',
    children: [

    { path: '', loadComponent: () => import('app/group/dashboard/dashboard.component').then(m => m.DashboardComponent) , canActivate: [canActivateGroup] },
      {
        path: 'library',
        children: libraryRoutes
      },
      {
        path: 'members',
        canActivate: [canActivateGroup],
        children: membersRoutes,
      },
      {
        path: 'forum',
        canActivate: [canActivateGroup],
        children: forumRoutes,
      },
      {
        path: 'agenda',
        canActivate: [canActivateGroup],
        children: agendaRoutes
      },
      {
        path: 'information',
        canActivate: [canActivateGroup],
        children: informationRoutes
      },
      {
        path: 'keywords',
        canActivate: [canActivateGroup,canActivateAdmin],
        children: keywordsRoutes
      },
      {
        path: 'profiles',
        canActivate: [canActivateGroup,canActivateGroupMembersAdmin],
        children: profilesRoutes
        
      },
      {
        path: 'dynamic-properties',
        canActivate: [canActivateGroup,canActivateAdmin],
        children: dynPropRoutes,
      },
      {
        path: 'admin',
        canActivate: [canActivateGroup,canActivateAdmin],
        children: groupAdminRoutes,
      },
      {
        path: 'applicants',
        canActivate: [canActivateGroup],
        children: applicantsRoutes
      },
      {
        path: 'permissions',
        canActivate: [canActivateGroup],
        children: permissionsRoutes
      },
      {
        path: 'notifications',
        canActivate: [canActivateGroup],
        children: notificationsRoutes
      },
      {
        path: 'notification-status/:nodeId',
        loadComponent: () => import('app/group/admin/notification-status/notification-status.component').then(m => m.NotificationStatusComponent),
        canActivate: [canActivateGroup]
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(groupRoutes)],
  exports: [RouterModule],
  providers: [GroupResolver],
})
export class GroupRoutingModule {}
