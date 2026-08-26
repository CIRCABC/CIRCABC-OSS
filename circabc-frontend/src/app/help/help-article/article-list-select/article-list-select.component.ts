import { CdkDrag, CdkDropList } from '@angular/cdk/drag-drop';
import { Component, input, output } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { HelpArticle } from 'app/core/generated/circabc';
import { HelpActionButtonsComponent } from 'app/help/components/help-action-buttons/help-action-buttons.component';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';

@Component({
  selector: 'cbc-article-list-select',
  templateUrl: './article-list-select.component.html',
  styleUrl: './article-list-select.component.scss',
  imports: [
    RouterLink,
    I18nPipe,
    TranslocoModule,
    CdkDropList,
    CdkDrag,
    HelpActionButtonsComponent,
  ],
})
export class ArticleListSelectComponent {
  readonly articles = input<HelpArticle[]>([]);
  readonly currentId = input<string>();
  readonly isAdminOrSupport = input<boolean>(false);
  readonly articleDropped = output<{
    previousIndex: number;
    currentIndex: number;
  }>();
  readonly addArticleClicked = output<void>();
  readonly editArticleClicked = output<string>();
  readonly deleteArticleClicked = output<string>();

  onDrop(event: { previousIndex: number; currentIndex: number }) {
    this.articleDropped.emit(event);
  }

  onAddArticle(): void {
    this.addArticleClicked.emit();
  }

  onEditArticle(articleId: string | undefined): void {
    if (articleId) {
      this.editArticleClicked.emit(articleId);
    }
  }

  onDeleteArticle(articleId: string | undefined): void {
    if (articleId) {
      this.deleteArticleClicked.emit(articleId);
    }
  }
}
