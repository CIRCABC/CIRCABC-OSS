import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { HelpArticle } from 'app/core/generated/circabc';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';

@Component({
  selector: 'cbc-article-list-select',
  templateUrl: './article-list-select.component.html',
  styleUrl: './article-list-select.component.scss',
  imports: [RouterLink, I18nPipe, TranslocoModule],
})
export class ArticleListSelectComponent {
  readonly articles = input<HelpArticle[]>([]);
  readonly currentId = input<string>();
}
