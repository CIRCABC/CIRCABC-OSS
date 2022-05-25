import { NgModule } from '@angular/core';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { NoPreloading, RouterModule, Routes } from '@angular/router';
import { AccessDeniedComponent } from 'app/access-denied/access-denied.component';
import { NoContentFoundComponent } from 'app/no-content-found/no-content-found.component';
import { PageNotFoundComponent } from 'app/page-not-found.component';
import { WelcomeComponent } from 'app/welcome/welcome.component';
import { BrowseComponent } from './browse/browse.component';
import { NodeResolver } from './core/guards/node.resolver';

const appRoutes: Routes = [
  { path: '', redirectTo: '/welcome', pathMatch: 'full' },
  { path: 'welcome', component: WelcomeComponent },
  { path: 'w/browse/:id', component: BrowseComponent,resolve: {
    node: NodeResolver,
  }, },
  {
    path: 'login',
    loadChildren: () =>
      import('app/login/login.module').then(m => m.LoginModule),
  },
  {
    path: 'explore',
    loadChildren: () =>
      import('app/explorer/explorer.module').then(m => m.ExplorerModule),
  },
  {
    path: 'help',
    loadChildren: () =>
      import('app/help/help.module').then(m => m.HelpModule),
  },
  {
    path: 'group',
    loadChildren: () =>
      import('app/group/group.module').then(m => m.GroupModule),
  },
  {
    path: 'me',
    loadChildren: () => import('app/me/me.module').then(m => m.MeModule),
  },
  {
    path: 'admin',
    loadChildren: () =>
      import('app/admin/admin.module').then(m => m.AdminModule),
  },
  {
    path: 'category',
    loadChildren: () =>
      import('app/category/category.module').then(m => m.CategoryModule),
  },
  {
    path: 'office',
    loadChildren: () =>
      import('app/office/office.module').then(m => m.OfficeModule),
  },
  {
    path: 'support',
    loadChildren: () =>
      import('app/support/support.module').then(m => m.SupportModule),
  },
  { path: 'denied', component: AccessDeniedComponent },
  { path: 'no-content', component: NoContentFoundComponent },
  { path: '**', component: PageNotFoundComponent },
];

@NgModule({
  imports: [
    RouterModule.forRoot(appRoutes, {
      preloadingStrategy: NoPreloading,
      paramsInheritanceStrategy: 'always',
      scrollPositionRestoration: 'enabled',
      enableTracing: false, // !environment.production
    }),
  ],
  exports: [RouterModule],
})
export class AppRoutingModule {}
