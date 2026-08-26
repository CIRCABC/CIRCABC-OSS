import {
  Component,
  inject,
  signal,
  computed,
  effect,
  ChangeDetectionStrategy,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { ArticleViewerService } from '../../services/article-viewer.service';
import { HelpService } from '../../../core/generated/circabc';
import { HelpArticle as HelpArticleApi } from '../../../core/generated/circabc/model/helpArticle';
import { LoginService } from '../../../core/login.service';
import { AddHelpArticleComponent } from '../../add-help-article/add-help-article.component';
import { HelpDeleteConfirmationModalComponent } from '../help-delete-confirmation-modal/help-delete-confirmation-modal.component';
import { ActionEmitterResult, ActionResult } from 'app/action-result';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { HelpHierarchyService } from '../../services/help-hierarchy.service';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-article-viewer',
  standalone: true,
  imports: [
    CommonModule,
    TranslocoModule,
    AddHelpArticleComponent,
    HelpDeleteConfirmationModalComponent,
  ],
  templateUrl: './article-viewer.component.html',
  styleUrl: './article-viewer.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ArticleViewerComponent {
  private readonly viewerService = inject(ArticleViewerService);
  private readonly helpService = inject(HelpService);
  private readonly sanitizer = inject(DomSanitizer);
  private readonly translocoService = inject(TranslocoService);
  private readonly loginService = inject(LoginService);
  private readonly uiMessageService = inject(UiMessageService);
  private readonly hierarchyService = inject(HelpHierarchyService);

  readonly isOpen = this.viewerService.isOpen;
  readonly articleId = this.viewerService.articleId;
  readonly categoryId = this.viewerService.categoryId;
  readonly subcategoryId = this.viewerService.subcategoryId;
  readonly categoryName = this.viewerService.categoryName;
  readonly subcategoryName = this.viewerService.subcategoryName;

  readonly isLoading = signal<boolean>(false);
  readonly errorMessage = signal<string>('');
  readonly articleData = signal<HelpArticleApi | null>(null);
  readonly showEditModal = signal<boolean>(false);
  readonly showDeleteModal = signal<boolean>(false);
  readonly isHighlighting = signal<boolean>(false);

  readonly isHighlighted = computed<boolean>(() => {
    const article = this.articleData();
    return article?.highlighted === true;
  });

  readonly title = computed<string>(() => {
    const article = this.articleData();
    if (!article?.title) {
      return '';
    }

    const titleObj = article.title;
    if (typeof titleObj === 'string') {
      return titleObj;
    }

    const lang = this.translocoService.getActiveLang();
    const defaultLang = this.translocoService.getDefaultLang();

    if (titleObj[lang]) {
      return titleObj[lang];
    }

    if (titleObj[defaultLang]) {
      return titleObj[defaultLang];
    }

    const keys = Object.keys(titleObj);
    return keys.length > 0 ? titleObj[keys[0]] : '';
  });

  readonly sanitizedContent = computed<SafeHtml>(() => {
    const article = this.articleData();
    if (!article?.content) {
      return '';
    }

    const contentObj = article.content;
    let result = '';

    if (typeof contentObj === 'string') {
      result = contentObj;
    } else {
      const lang = this.translocoService.getActiveLang();
      const defaultLang = this.translocoService.getDefaultLang();

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
  });

  constructor() {
    effect(() => {
      const id = this.articleId();
      if (id) {
        this.loadArticle(id);
      }
    });
  }

  private async loadArticle(articleId: string): Promise<void> {
    this.isLoading.set(true);
    this.errorMessage.set('');

    try {
      const article = await firstValueFrom(
        this.helpService.getHelpArticle(articleId)
      );

      this.articleData.set(article);
    } catch (error) {
      console.error('[ArticleViewer] Failed to load article:', error);
      this.errorMessage.set(
        'Failed to load article content. Please try again.'
      );
    } finally {
      this.isLoading.set(false);
    }
  }

  onClose(): void {
    this.viewerService.closeArticle();
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

  onEditArticle(): void {
    this.showEditModal.set(true);
  }

  onDeleteArticle(): void {
    this.showDeleteModal.set(true);
  }

  async onArticleUpdated(result: ActionEmitterResult): Promise<void> {
    if (result.result === ActionResult.SUCCEED) {
      const id = this.articleId();
      if (id) {
        await this.loadArticle(id);
      }
    }
  }

  async onDeleteConfirmed(): Promise<void> {
    const id = this.articleId();
    if (!id) {
      return;
    }

    try {
      await firstValueFrom(this.helpService.deleteHelpArticle(id));

      this.hierarchyService.loadHierarchy(true);
      this.onClose();

      const successMessage = this.translocoService.translate(
        'help.delete.article.succeed'
      );
      this.uiMessageService.addSuccessMessage(successMessage);
    } catch (error) {
      console.error('[ArticleViewer] Failed to delete article', error);
      const errorMessage = this.translocoService.translate(
        'help.delete.article.failed'
      );
      this.uiMessageService.addErrorMessage(errorMessage);
    }
  }

  async onToggleHighlight(): Promise<void> {
    const id = this.articleId();
    if (!id || this.isHighlighting()) {
      return;
    }

    this.isHighlighting.set(true);

    try {
      const updatedArticle = await firstValueFrom(
        this.helpService.toggleHighlightArticle(id)
      );

      this.articleData.set(updatedArticle);

      const messageKey = updatedArticle.highlighted
        ? 'help.highlight.article.succeed'
        : 'help.unhighlight.article.succeed';
      const successMessage = this.translocoService.translate(messageKey);
      this.uiMessageService.addSuccessMessage(successMessage);

      this.hierarchyService.loadHierarchy(true);
    } catch (error) {
      console.error('[ArticleViewer] Failed to toggle highlight', error);
      const errorMessage = this.translocoService.translate(
        'help.highlight.article.failed'
      );
      this.uiMessageService.addErrorMessage(errorMessage);
    } finally {
      this.isHighlighting.set(false);
    }
  }
}
