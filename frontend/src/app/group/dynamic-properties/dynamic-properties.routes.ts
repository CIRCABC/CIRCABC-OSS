import { Routes } from '@angular/router';

/**
 * Lazy-loaded child route definitions for the dynamic-properties feature area.
 *
 * Declares two routes:
 * - `''`: the default route that lazily loads `DynamicPropertiesComponent`,
 *   which lists/manages the group's dynamic properties.
 * - `':dpId/edit'`: an edit route that lazily loads
 *   `EditDynamicPropertyComponent`, using the `dpId` path parameter to
 *   identify the dynamic property being edited.
 */
export const dynPropRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('app/group/dynamic-properties/dynamic-properties.component').then(
        (m) => m.DynamicPropertiesComponent
      ),
  },
  {
    path: ':dpId/edit',
    loadComponent: () =>
      import('app/group/dynamic-properties/edit-dynamic-property/edit-dynamic-property.component').then(
        (m) => m.EditDynamicPropertyComponent
      ),
  },
];
