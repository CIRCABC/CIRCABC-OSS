import { DatePipe } from '@angular/common';
import { Component, input, output } from '@angular/core';
import { type HelpArticle } from 'app/core/generated/circabc';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';

@Component({
  selector: 'cbc-article-card',
  templateUrl: './article-card.component.html',
  styleUrl: './article-card.component.scss',
  imports: [DatePipe, I18nPipe],
})
export class ArticleCardComponent {
  readonly article = input.required<HelpArticle>();
  readonly articleClicked = output<string>();

  onArticleClick(event: Event): void {
    event.preventDefault();
    const articleId = this.article().id;
    if (articleId) {
      this.articleClicked.emit(articleId);
    }
  }
}
