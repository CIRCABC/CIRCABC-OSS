import { Routes } from '@angular/router';

/**
 * Lazy-loaded route configuration for the Help feature area.
 *
 * The single top-level route loads {@link HelpComponent} as the layout shell
 * and defines the child routes rendered within it:
 * - `start` — landing help page (default; empty path redirects here)
 * - `about` — about page
 * - `category/:categoryId` — help articles for a given category
 * - `category/:categoryId/article/:articleId` — a specific help article
 * - `contact` — contact support page
 * - `legal-notice` and `legal-notice/:link` — legal notice page
 *
 * All components are loaded lazily via `loadComponent` to keep the initial
 * bundle small.
 */
export const helpRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('app/help/help.component').then((m) => m.HelpComponent),
    children: [
      { path: '', redirectTo: 'start', pathMatch: 'full' },
      {
        path: 'start',
        loadComponent: () =>
          import('app/help/start/start.component').then(
            (m) => m.StartComponent
          ),
      },
      {
        path: 'about',
        loadComponent: () =>
          import('./about/about.component').then((m) => m.AboutComponent),
      },
      {
        path: 'category/:categoryId',
        loadComponent: () =>
          import('app/help/help-category/help-category.component').then(
            (m) => m.HelpCategoryComponent
          ),
      },
      {
        path: 'category/:categoryId/article/:articleId',
        loadComponent: () =>
          import('app/help/help-article/help-article.component').then(
            (m) => m.HelpArticleComponent
          ),
      },
      {
        path: 'contact',
        loadComponent: () =>
          import('app/help/contact-support/contact-support.component').then(
            (m) => m.ContactSupportComponent
          ),
      },
      {
        path: 'legal-notice',
        loadComponent: () =>
          import('app/help/legal-notice/legal-notice.component').then(
            (m) => m.LegalNoticeComponent
          ),
      },
      {
        path: 'legal-notice/:link',
        loadComponent: () =>
          import('app/help/legal-notice/legal-notice.component').then(
            (m) => m.LegalNoticeComponent
          ),
      },
    ],
  },
];
