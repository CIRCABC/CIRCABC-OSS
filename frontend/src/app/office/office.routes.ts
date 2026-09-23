import { Routes } from '@angular/router';

/**
 * Child route definitions for the Office feature area.
 *
 * Declares a single default route (`path: ''`) that lazily loads the
 * standalone `OfficeComponent` via a dynamic `import()`, ensuring the
 * office view is only fetched when the corresponding route is activated.
 */
export const officeRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./office/office.component').then((m) => m.OfficeComponent),
  },
];
