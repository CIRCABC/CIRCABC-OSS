import { Routes } from '@angular/router';

import { canActivateNodeAccess } from 'app/group/guards/access-guard.service';

/**
 * Lazy-loaded child route configuration for the forum feature area.
 *
 * Each route lazily imports its standalone component via `loadComponent`
 * and is protected by the {@link canActivateNodeAccess} guard, which
 * verifies the current user has access to the targeted node before the
 * component is activated.
 *
 * Available routes:
 * - `:nodeId` — renders the forum listing for the given node
 *   ({@link ForumComponent}).
 * - `topic/:nodeId` — renders a single discussion topic
 *   ({@link TopicComponent}).
 * - `:forumId/details` — renders the view/edit details screen for a forum
 *   ({@link ViewEditDetailsForumComponent}).
 * - `:topicId/topic-details` — renders the view/edit details screen for a
 *   topic ({@link ViewEditDetailsTopicComponent}).
 */
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
      import('app/group/forum/view-edit-details-forum/view-edit-details-forum.component').then(
        (m) => m.ViewEditDetailsForumComponent
      ),
    canActivate: [canActivateNodeAccess],
  },
  {
    path: ':topicId/topic-details',
    loadComponent: () =>
      import('app/group/forum/topic/view-edit-details-topic/view-edit-details-topic.component').then(
        (m) => m.ViewEditDetailsTopicComponent
      ),
    canActivate: [canActivateNodeAccess],
  },
];
