import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

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

@NgModule({
  imports: [RouterModule.forChild(helpRoutes)],
  exports: [RouterModule],
})
export class HelpRoutingModule {}
