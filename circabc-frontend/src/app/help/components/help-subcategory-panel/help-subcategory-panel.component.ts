import {
  Component,
  input,
  signal,
  computed,
  inject,
  ChangeDetectionStrategy,
  output,
} from '@angular/core';
import {
  CdkDrag,
  CdkDropList,
  CdkDragDrop,
  CdkDragHandle,
} from '@angular/cdk/drag-drop';
import { moveItemInArray } from '@angular/cdk/drag-drop';
import { firstValueFrom } from 'rxjs';
import {
  HelpSubcategory,
  HelpArticle,
} from '../../models/help-hierarchy.model';
import { AccordionStateService } from '../../services/accordion-state.service';
import { HelpHierarchyService } from '../../services/help-hierarchy.service';
import { HelpService } from '../../../core/generated/circabc';
import { LoginService } from '../../../core/login.service';
import { HelpArticleItemComponent } from '../help-article-item/help-article-item.component';
import { HelpActionButtonsComponent } from '../help-action-buttons/help-action-buttons.component';
import { ArticleViewerService } from '../../services/article-viewer.service';
import { ArticleClipboardService } from '../../services/article-clipboard.service';
import { HelpSubcategoryFormModalComponent } from '../help-subcategory-form-modal/help-subcategory-form-modal.component';
import { HelpDeleteConfirmationModalComponent } from '../help-delete-confirmation-modal/help-delete-confirmation-modal.component';
import { AddHelpArticleComponent } from '../../add-help-article/add-help-article.component';
import { ActionEmitterResult, ActionResult } from '../../../action-result';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';

@Component({
  selector: 'app-help-subcategory-panel',
  standalone: true,
  imports: [
    CdkDrag,
    CdkDropList,
    CdkDragHandle,
    HelpArticleItemComponent,
    HelpActionButtonsComponent,
    HelpSubcategoryFormModalComponent,
    HelpDeleteConfirmationModalComponent,
    AddHelpArticleComponent,
    TranslocoModule,
  ],
  templateUrl: './help-subcategory-panel.component.html',
  styleUrl: './help-subcategory-panel.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HelpSubcategoryPanelComponent {
  subcategory = input.required<HelpSubcategory>();
  categoryId = input.required<string>();
  categoryName = input.required<string>();

  private readonly accordionState = inject(AccordionStateService);
  private readonly helpHierarchyService = inject(HelpHierarchyService);
  private readonly helpService = inject(HelpService);
  private readonly loginService = inject(LoginService);
  private readonly articleViewerService = inject(ArticleViewerService);
  private readonly clipboardService = inject(ArticleClipboardService);
  private readonly uiMessageService = inject(UiMessageService);
  private readonly translocoService = inject(TranslocoService);

  readonly subcategoryChanged = output<void>();

  readonly hasArticleCopied = this.clipboardService.hasArticleCopied;

  readonly articles = computed(() => {
    const subcat = this.subcategory();
    return subcat?.articles ? [...subcat.articles] : [];
  });

  private readonly localArticles = signal<HelpArticle[]>([]);
  private readonly isDraggingArticles = signal<boolean>(false);

  readonly displayArticles = computed(() =>
    this.isDraggingArticles() ? this.localArticles() : this.articles()
  );

  readonly showEditModal = signal<boolean>(false);
  readonly showDeleteModal = signal<boolean>(false);
  readonly showAddArticleModal = signal<boolean>(false);

  readonly isExpanded = computed(() =>
    this.accordionState.isSubcategoryExpanded(
      this.categoryId(),
      this.subcategory().id
    )
  );

  toggleExpansion(): void {
    this.accordionState.toggleSubcategory(
      this.categoryId(),
      this.subcategory().id
    );
  }

  handleKeyDown(event: KeyboardEvent): void {
    if (event.key === 'Enter' || event.key === ' ') {
      event.preventDefault();
      this.toggleExpansion();
    }
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

  async onArticleDrop(event: CdkDragDrop<HelpArticle[]>): Promise<void> {
    const subcat = this.subcategory();
    if (!subcat?.id) {
      return;
    }

    const current = [...this.articles()];
    this.isDraggingArticles.set(true);

    moveItemInArray(current, event.previousIndex, event.currentIndex);
    this.localArticles.set(current);

    try {
      const articleIds = current
        .map((a) => a.id)
        .filter((id): id is string => id !== undefined);

      await firstValueFrom(
        this.helpHierarchyService.putSubcategoryArticlesOrder(
          subcat.id,
          articleIds
        )
      );

      this.subcategoryChanged.emit();
    } catch (error) {
      console.error('Failed to save article order', error);
    } finally {
      this.isDraggingArticles.set(false);
    }
  }

  onArticleClicked(articleId: string): void {
    const subcategory = this.subcategory();
    const categoryId = this.categoryId();
    const categoryName = this.categoryName();
    this.articleViewerService.openArticle(
      articleId,
      categoryId,
      subcategory.id,
      categoryName,
      subcategory.name
    );
  }

  onEditSubcategory(): void {
    this.showEditModal.set(true);
  }

  onDeleteSubcategory(): void {
    this.showDeleteModal.set(true);
  }

  onSubcategorySaved(result: ActionEmitterResult): void {
    if (result.result === ActionResult.SUCCEED) {
      this.subcategoryChanged.emit();
    }
  }

  async onDeleteConfirmed(): Promise<void> {
    const subcat = this.subcategory();
    if (!subcat?.id) {
      return;
    }

    try {
      await firstValueFrom(this.helpService.deleteHelpSubcategory(subcat.id));

      // Emit event to parent to reload hierarchy
      this.subcategoryChanged.emit();
    } catch (error) {
      console.error('Failed to delete subcategory', error);
    }
  }

  onAddArticle(): void {
    this.showAddArticleModal.set(true);
  }

  onArticleCreated(result: ActionEmitterResult): void {
    if (result.result === ActionResult.SUCCEED) {
      this.subcategoryChanged.emit();
    }
  }

  async onPasteArticle(): Promise<void> {
    const copied = this.clipboardService.copiedArticle();
    if (!(copied && this.subcategory().id)) return;

    try {
      const article = await firstValueFrom(
        this.helpService.getHelpArticle(copied.articleId)
      );

      const newArticle = {
        title: article.title,
        content: article.content,
        highlighted: false,
      };

      await firstValueFrom(
        this.helpService.postSubcategoryArticle(
          this.subcategory().id,
          newArticle
        )
      );

      this.clipboardService.clear();
      this.subcategoryChanged.emit();
    } catch (error) {
      console.error('[HelpSubcategoryPanel] Failed to paste article', error);
      const errorMessage = this.translocoService.translate(
        'help.paste.article.failed'
      );
      this.uiMessageService.addErrorMessage(errorMessage);
    }
  }
}
