import { Routes } from '@angular/router';

import { canActivateService } from 'app/group/guards/service-guard.service';

/**
 * Child route definitions for the group Information service feature.
 *
 * These routes are lazily loaded and map URL paths to standalone
 * components:
 * - `''`: the {@link InformationComponent} listing/dashboard view, protected
 *   by the {@link canActivateService} guard which verifies that the current
 *   user is allowed to access the service.
 * - `add/:infoId`: the {@link AddNewsComponent} used to create a news item,
 *   parameterised by the information node id.
 * - `:newsId/edit`: the {@link AddNewsComponent} reused in edit mode,
 *   parameterised by the news item id.
 *
 * All component targets use `loadComponent` for per-route code splitting.
 */
export const informationRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('app/group/information/information.component').then(
        (m) => m.InformationComponent
      ),
    canActivate: [canActivateService],
  },
  {
    path: 'add/:infoId',
    loadComponent: () =>
      import('app/group/information/add-news/add-news.component').then(
        (m) => m.AddNewsComponent
      ),
  },
  {
    path: ':newsId/edit',
    loadComponent: () =>
      import('app/group/information/add-news/add-news.component').then(
        (m) => m.AddNewsComponent
      ),
  },
];
