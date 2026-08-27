import { Component, input, output, inject } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HelpArticle } from '../../models/help-hierarchy.model';
import { TranslocoModule } from '@jsverse/transloco';
import { ArticleClipboardService } from '../../services/article-clipboard.service';
import { LoginService } from '../../../core/login.service';

@Component({
  selector: 'app-help-article-item',
  standalone: true,
  imports: [TranslocoModule],
  templateUrl: './help-article-item.component.html',
  styleUrl: './help-article-item.component.css',
})
export class HelpArticleItemComponent {
  article = input.required<HelpArticle>();
  readonly articleClicked = output<string>();
  readonly articleCopied = output<string>();

  private readonly datePipe = new DatePipe('en-US');
  private readonly clipboardService = inject(ArticleClipboardService);
  private readonly loginService = inject(LoginService);

  onArticleClick(): void {
    const articleData = this.article();
    if (articleData?.id) {
      this.articleClicked.emit(articleData.id);
    }
  }

  onCopyClick(event: Event): void {
    event.stopPropagation();
    const articleData = this.article();
    if (articleData?.id) {
      this.clipboardService.copyArticle({
        articleId: articleData.id,
        title: articleData.title,
        sourceCategoryId: articleData.categoryId || '',
        sourceSubcategoryId: articleData.subcategoryId || null,
      });
    }
  }

  handleKeyDown(event: KeyboardEvent): void {
    if (event.key === 'Enter') {
      event.preventDefault();
      this.onArticleClick();
    }
  }

  formatDate(date: string): string {
    return this.datePipe.transform(date, 'dd/MM/yyyy') || '';
  }

  isAdminOrSupport(): boolean {
    if (!this.loginService.isGuest()) {
      const user = this.loginService.getUser();
      return (
        user.properties !== undefined &&
        (user.properties['isAdmin'] === 'true' ||
          user.properties['isCircabcAdmin'] === 'true')
      );
    }
    return false;
  }
}
