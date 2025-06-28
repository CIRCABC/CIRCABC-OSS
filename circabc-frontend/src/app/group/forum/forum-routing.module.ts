import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { canActivateNodeAccess } from 'app/group/guards/access-guard.service';

export const forumRoutes: Routes = [
  {
    path: ':nodeId',
    loadComponent: () =>
      import('app/group/forum/forum.component').then((m) => m.ForumComponent),
    canActivate: [canActivateNodeAccess],
  },
  {
    path: 'topic/:nodeId',
    loadComponent: () =>
      import('app/group/forum/topic/topic.component').then(
        (m) => m.TopicComponent
      ),
    canActivate: [canActivateNodeAccess],
  },
  {
    path: ':forumId/details',
    loadComponent: () =>
      import(
        'app/group/forum/view-edit-details-forum/view-edit-details-forum.component'
      ).then((m) => m.ViewEditDetailsForumComponent),
    canActivate: [canActivateNodeAccess],
  },
  {
    path: ':topicId/topic-details',
    loadComponent: () =>
      import(
        'app/group/forum/topic/view-edit-details-topic/view-edit-details-topic.component'
      ).then((m) => m.ViewEditDetailsTopicComponent),
    canActivate: [canActivateNodeAccess],
  },
];

@NgModule({
  imports: [RouterModule.forChild(forumRoutes)],
  exports: [RouterModule],
})
export class ForumRoutingModule {}
