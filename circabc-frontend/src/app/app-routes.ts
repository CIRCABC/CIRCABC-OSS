import { Routes } from '@angular/router';
import { NodeResolver } from 'app/core/guards/node.resolver';
import { helpRoutes } from './help/help-routing.module';
import { groupRoutes } from './group/group-routing.module';
import { meRoutes } from './me/me-routing.module';
import { adminRoutes } from './admin/admin-routing.module';
import { categoryRoutes } from './category/category-routing.module';
import { supportRoutes } from './support/support-routing.module';
import { loginRoutes } from './login/login-routing.module';
import { explorerRoutes } from './explorer/explorer-routing.module';

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
      node: NodeResolver,
    },
  },
  {
    path: 'login',
    children: loginRoutes,
  },
  {
    path: 'explore',
    children: explorerRoutes,
  },
  {
    path: 'help',
    children: helpRoutes,
  },
  {
    path: 'group',
    children: groupRoutes,
  },
  {
    path: 'me',
    children: meRoutes,
  },
  {
    path: 'admin',
    children: adminRoutes,
  },
  {
    path: 'category',
    children: categoryRoutes,
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
    children: supportRoutes,
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
