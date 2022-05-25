import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { ContactSupportComponent } from 'app/help/contact-support/contact-support.component';
import { HelpArticleComponent } from 'app/help/help-article/help-article.component';
import { HelpCategoryComponent } from 'app/help/help-category/help-category.component';
import { HelpComponent } from 'app/help/help.component';
import { LegalNoticeComponent } from 'app/help/legal-notice/legal-notice.component';
import { StartComponent } from 'app/help/start/start.component';
import { AboutComponent } from './about/about.component';

const helpRoutes: Routes = [
  {
    path: '',
    component: HelpComponent,
    children: [
      { path: '', redirectTo: 'start', pathMatch: 'full' },
      { path: 'start', component: StartComponent },
      { path: 'about', component: AboutComponent },
      { path: 'category/:categoryId', component: HelpCategoryComponent },
      {
        path: 'category/:categoryId/article/:articleId',
        component: HelpArticleComponent,
      },
      { path: 'contact', component: ContactSupportComponent },
      { path: 'legal-notice', component: LegalNoticeComponent },
      { path: 'legal-notice/:link', component: LegalNoticeComponent },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(helpRoutes)],
  exports: [RouterModule],
})
export class HelpRoutingModule {}
