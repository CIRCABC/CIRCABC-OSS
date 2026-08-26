import {
  Component,
  input,
  output,
  model,
  inject,
  effect,
  signal,
} from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { HelpService } from 'app/core/generated/circabc';
import { HelpArticle as HelpArticleApi } from 'app/core/generated/circabc/model/helpArticle';
import { LoginService } from 'app/core/login.service';
import { AddHelpArticleComponent } from 'app/help/add-help-article/add-help-article.component';
import { DeleteHelpArticleComponent } from 'app/help/delete-help-article/delete-help-article.component';
import { ActionEmitterResult, ActionResult } from 'app/action-result';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-help-article-modal',
  standalone: true,
  imports: [
    CommonModule,
    TranslocoModule,
    DatePipe,
    AddHelpArticleComponent,
    DeleteHelpArticleComponent,
  ],
  templateUrl: './help-article-modal.component.html',
  styleUrl: './help-article-modal.component.css',
})
export class HelpArticleModalComponent {
  private readonly helpService = inject(HelpService);
  private readonly sanitizer = inject(DomSanitizer);
  private readonly translateService = inject(TranslocoService);
  private readonly loginService = inject(LoginService);

  readonly articleId = input.required<string>();
  readonly visible = model.required<boolean>();
  readonly closed = output<void>();
  readonly articleData = signal<HelpArticleApi | null>(null);
  readonly isLoading = signal<boolean>(false);
  readonly errorMessage = signal<string | null>(null);
  readonly showEditModal = signal<boolean>(false);
  readonly showDeleteModal = signal<boolean>(false);

  constructor() {
    effect(() => {
      if (this.visible() && this.articleId()) {
        this.loadArticle();
      }
    });
  }

  private loadArticle(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.articleData.set(null);

    this.helpService.getHelpArticle(this.articleId()).subscribe({
      next: (article) => {
        this.articleData.set(article);
        this.isLoading.set(false);
      },
      error: (error) => {
        console.error('[HelpArticleModal] Failed to load article:', error);
        this.errorMessage.set(
          'Failed to load article content. Please try again.'
        );
        this.isLoading.set(false);
      },
    });
  }

  getTitle(): string {
    const article = this.articleData();
    if (!article?.title) {
      return '';
    }

    const titleObj = article.title;
    if (typeof titleObj === 'string') {
      return titleObj;
    }

    const lang = this.translateService.getActiveLang();
    const defaultLang = this.translateService.getDefaultLang();

    if (titleObj[lang]) {
      return titleObj[lang];
    }

    if (titleObj[defaultLang]) {
      return titleObj[defaultLang];
    }

    const keys = Object.keys(titleObj);
    return keys.length > 0 ? titleObj[keys[0]] : '';
  }

  getContent(): SafeHtml {
    const article = this.articleData();
    if (!article?.content) {
      return '';
    }

    const contentObj = article.content;
    let result = '';

    if (typeof contentObj === 'string') {
      result = contentObj;
    } else {
      const lang = this.translateService.getActiveLang();
      const defaultLang = this.translateService.getDefaultLang();

      if (contentObj[lang]) {
        result = contentObj[lang];
      } else if (contentObj[defaultLang]) {
        result = contentObj[defaultLang];
      } else {
        const keys = Object.keys(contentObj);
        if (keys.length > 0) {
          result = contentObj[keys[0]];
        }
      }
    }

    return this.sanitizer.bypassSecurityTrustHtml(result);
  }

  isAdminOrSupport(): boolean {
    if (!this.loginService.isGuest()) {
      const user = this.loginService.getUser();
      return (
        user.properties !== undefined &&
        (user.properties.isAdmin === 'true' ||
          user.properties.isCircabcAdmin === 'true')
      );
    }
    return false;
  }

  async toggleHighlight(): Promise<void> {
    const article = this.articleData();
    if (!article?.id) {
      return;
    }

    this.isLoading.set(true);
    try {
      const updatedArticle = await firstValueFrom(
        this.helpService.toggleHighlightArticle(article.id)
      );
      this.articleData.set(updatedArticle);
    } catch (error) {
      console.error('[HelpArticleModal] Failed to toggle highlight:', error);
    }
    this.isLoading.set(false);
  }

  async onArticleUpdated(result: ActionEmitterResult): Promise<void> {
    if (result.result === ActionResult.SUCCEED) {
      this.visible.set(true);
      await this.loadArticle();
    }
  }

  onArticleDeleted(result: ActionEmitterResult): void {
    if (result.result === ActionResult.SUCCEED) {
      this.visible.set(false);
      this.closed.emit();
    }
  }

  onClose(): void {
    this.visible.set(false);
    this.closed.emit();
  }
}
