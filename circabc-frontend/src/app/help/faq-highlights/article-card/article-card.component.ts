import { Component, Input } from '@angular/core';
import { HelpArticle } from 'app/core/generated/circabc';

@Component({
  selector: 'cbc-article-card',
  templateUrl: './article-card.component.html',
  styleUrls: ['./article-card.component.scss'],
})
export class ArticleCardComponent {
  @Input()
  article!: HelpArticle;
}
