import { Routes } from '@angular/router';
import { resolveNode } from 'app/core/guards/node.resolver';

/**
 * Root routing table for the CIRCABC application.
 *
 * Declares the top-level {@link Routes} consumed by the Angular router to
 * bootstrap the single-page application. All feature areas are lazy-loaded
 * (via `loadComponent` for standalone components or `loadChildren` for nested
 * route modules) to keep the initial bundle small.
 *
 * Route overview:
 * - `''` — redirects to `/welcome` (default landing route).
 * - `welcome` — public welcome/landing page.
 * - `w/browse/:id` — node browsing view; resolves the target node via
 *   {@link resolveNode} before activation.
 * - `login` — authentication flow (EU Login / ECAS) child routes.
 * - `explore` — category/header explorer child routes.
 * - `help` — help pages child routes.
 * - `group` — interest group workspace child routes (library, forum, agenda, …).
 * - `me` — current user's personal area child routes.
 * - `admin` — platform administration child routes.
 * - `category` — category management child routes.
 * - `support` — support pages child routes.
 * - `denied` — access denied page.
 * - `no-content` — page shown when requested content is not found.
 * - `**` — wildcard catch-all rendering the page-not-found component.
 */
export const appRoutes: Routes = [
  { path: '', redirectTo: '/welcome', pathMatch: 'full' },
  {
    path: 'welcome',
    loadComponent: () =>
      import('app/welcome/welcome.component').then((m) => m.WelcomeComponent),
  },
  {
    path: 'w/browse/:id',
    loadComponent: () =>
      import('app/browse/browse.component').then((m) => m.BrowseComponent),
    resolve: {
      node: resolveNode,
    },
  },
  {
    path: 'login',
    loadChildren: () =>
      import('./login/login.routes').then((m) => m.loginRoutes),
  },
  {
    path: 'explore',
    loadChildren: () =>
      import('./explorer/explorer.routes').then((m) => m.explorerRoutes),
  },
  {
    path: 'help',
    loadChildren: () => import('./help/help.routes').then((m) => m.helpRoutes),
  },
  {
    path: 'group',
    loadChildren: () =>
      import('./group/group.routes').then((m) => m.groupRoutes),
  },
  {
    path: 'me',
    loadChildren: () => import('./me/me.routes').then((m) => m.meRoutes),
  },
  {
    path: 'admin',
    loadChildren: () =>
      import('./admin/admin.routes').then((m) => m.adminRoutes),
  },
  {
    path: 'category',
    loadChildren: () =>
      import('./category/category.routes').then((m) => m.categoryRoutes),
  },
  /*
  {
    path: 'office',
    loadChildren: () =>
      import('app/office/office.module').then((m) => m.OfficeModule),
  },
  */
  {
    path: 'support',
    loadChildren: () =>
      import('./support/support.routes').then((m) => m.supportRoutes),
  },
  {
    path: 'denied',
    loadComponent: () =>
      import('app/access-denied/access-denied.component').then(
        (m) => m.AccessDeniedComponent
      ),
  },
  {
    path: 'no-content',
    loadComponent: () =>
      import('app/no-content-found/no-content-found.component').then(
        (m) => m.NoContentFoundComponent
      ),
  },
  {
    path: '**',
    loadComponent: () =>
      import('app/page-not-found.component').then(
        (m) => m.PageNotFoundComponent
      ),
  },
];
