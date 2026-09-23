import { Routes } from '@angular/router';

import { canActivateService } from 'app/group/guards/service-guard.service';

/**
 * Route configuration for the applicants feature area.
 *
 * Declares a single lazy-loaded route that resolves to
 * {@link ApplicantsComponent}. Activation is protected by
 * {@link canActivateService}, which ensures the current group/service
 * context is available before the component is displayed.
 */
export const applicantsRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('app/group/applicants/applicants.component').then(
        (m) => m.ApplicantsComponent
      ),
    canActivate: [canActivateService],
  },
];
