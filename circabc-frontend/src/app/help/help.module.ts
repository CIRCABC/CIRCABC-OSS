import { NgModule } from '@angular/core';

import { AddLinkComponent } from 'app/help//add-link/add-link.component';
import { AddHelpArticleComponent } from 'app/help/add-help-article/add-help-article.component';
import { AddHelpCategoryComponent } from 'app/help/add-help-category/add-help-category.component';
import { ContactSupportComponent } from 'app/help/contact-support/contact-support.component';
import { DeleteHelpArticleComponent } from 'app/help/delete-help-article/delete-help-article.component';
import { DeleteHelpCategoryComponent } from 'app/help/delete-help-category/delete-help-category.component';
import { ArticleCardComponent } from 'app/help/faq-highlights/article-card/article-card.component';
import { FaqHighlightsComponent } from 'app/help/faq-highlights/faq-highlights.component';
import { ArticleListSelectComponent } from 'app/help/help-article/article-list-select/article-list-select.component';
import { HelpArticleComponent } from 'app/help/help-article/help-article.component';
import { CategoryListSelectComponent } from 'app/help/help-category/category-list-select/category-list-select.component';
import { HelpCategoryComponent } from 'app/help/help-category/help-category.component';
import { HelpLinksComponent } from 'app/help/help-links/help-links.component';
import { HelpRoutingModule } from 'app/help/help-routing.module';
import { HelpSearchDisplayerComponent } from 'app/help/help-search-displayer/help-search-displayer.component';
import { HelpComponent } from 'app/help/help.component';
import { LegalNoticeComponent } from 'app/help/legal-notice/legal-notice.component';
import { StartComponent } from 'app/help/start/start.component';
import { PrimengComponentsModule } from 'app/primeng-components/primeng-components.module';
import { SharedModule } from 'app/shared/shared.module';
import { AboutComponent } from './about/about.component';
import { NgxExtendedPdfViewerModule } from 'ngx-extended-pdf-viewer';

@NgModule({
  imports: [
    HelpRoutingModule,
    SharedModule,
    PrimengComponentsModule,
    NgxExtendedPdfViewerModule,
  ],
  exports: [],
  declarations: [
    HelpComponent,
    StartComponent,
    FaqHighlightsComponent,
    HelpCategoryComponent,
    HelpArticleComponent,
    AddHelpArticleComponent,
    DeleteHelpArticleComponent,
    DeleteHelpCategoryComponent,
    AddHelpCategoryComponent,
    ArticleCardComponent,
    CategoryListSelectComponent,
    ArticleListSelectComponent,
    HelpLinksComponent,
    AddLinkComponent,
    HelpSearchDisplayerComponent,
    ContactSupportComponent,
    LegalNoticeComponent,
    AboutComponent,
  ],
  providers: [],
})
export class HelpModule {}
