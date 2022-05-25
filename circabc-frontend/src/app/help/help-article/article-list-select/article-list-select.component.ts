import { Component, Input } from '@angular/core';
import { HelpArticle } from 'app/core/generated/circabc';

@Component({
  selector: 'cbc-article-list-select',
  templateUrl: './article-list-select.component.html',
  styleUrls: ['./article-list-select.component.scss'],
})
export class ArticleListSelectComponent {
  @Input()
  articles: HelpArticle[] = [];
  @Input()
  currentId: string | undefined;
}
