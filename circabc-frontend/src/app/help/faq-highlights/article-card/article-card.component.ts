import { DatePipe } from '@angular/common';
import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { type HelpArticle } from 'app/core/generated/circabc';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';

@Component({
  selector: 'cbc-article-card',
  templateUrl: './article-card.component.html',
  styleUrl: './article-card.component.scss',
  imports: [RouterLink, DatePipe, I18nPipe],
})
export class ArticleCardComponent {
  readonly article = input.required<HelpArticle>();
}
