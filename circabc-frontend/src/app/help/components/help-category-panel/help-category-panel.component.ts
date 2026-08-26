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
import { MatDialog } from '@angular/material/dialog';
import { firstValueFrom } from 'rxjs';
import {
  HelpCategory,
  HelpSubcategory,
  HelpArticle,
} from '../../models/help-hierarchy.model';
import { HelpService } from 'app/core/generated/circabc';
import { AccordionStateService } from '../../services/accordion-state.service';
import { HelpHierarchyService } from '../../services/help-hierarchy.service';
import { LoginService } from '../../../core/login.service';
import { HelpSubcategoryPanelComponent } from '../help-subcategory-panel/help-subcategory-panel.component';
import { HelpArticleItemComponent } from '../help-article-item/help-article-item.component';
import { HelpActionButtonsComponent } from '../help-action-buttons/help-action-buttons.component';
import { ArticleViewerService } from '../../services/article-viewer.service';
import { ArticleClipboardService } from '../../services/article-clipboard.service';
import { HelpCategoryFormModalComponent } from '../help-category-form-modal/help-category-form-modal.component';
import { HelpSubcategoryFormModalComponent } from '../help-subcategory-form-modal/help-subcategory-form-modal.component';
import { HelpDeleteConfirmationModalComponent } from '../help-delete-confirmation-modal/help-delete-confirmation-modal.component';
import {
  AddChoiceDialogComponent,
  AddChoiceType,
} from '../add-choice-dialog/add-choice-dialog.component';
import { AddHelpArticleComponent } from '../../add-help-article/add-help-article.component';
import { ActionEmitterResult, ActionResult } from 'app/action-result';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';

@Component({
  selector: 'app-help-category-panel',
  standalone: true,
  imports: [
    CdkDrag,
    CdkDropList,
    CdkDragHandle,
    HelpSubcategoryPanelComponent,
    HelpArticleItemComponent,
    HelpActionButtonsComponent,
    HelpCategoryFormModalComponent,
    HelpSubcategoryFormModalComponent,
    HelpDeleteConfirmationModalComponent,
    AddHelpArticleComponent,
    TranslocoModule,
  ],
  templateUrl: './help-category-panel.component.html',
  styleUrl: './help-category-panel.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HelpCategoryPanelComponent {
  category = input.required<HelpCategory>();

  private readonly accordionState = inject(AccordionStateService);
  private readonly hierarchyService = inject(HelpHierarchyService);
  private readonly helpService = inject(HelpService);
  private readonly loginService = inject(LoginService);
  private readonly uiMessageService = inject(UiMessageService);
  private readonly translocoService = inject(TranslocoService);
  private readonly articleViewerService = inject(ArticleViewerService);
  private readonly clipboardService = inject(ArticleClipboardService);
  private readonly dialog = inject(MatDialog);

  readonly categoryChanged = output<void>();

  readonly hasArticleCopied = this.clipboardService.hasArticleCopied;

  readonly subcategories = computed(() => {
    const cat = this.category();
    if (!cat?.subcategories) {
      return [];
    }

    // Remove duplicates based on ID and filter out the category itself (defensive check)
    const seen = new Set<string>();
    const unique = cat.subcategories.filter((sub) => {
      if (sub.id === cat.id || !sub.id || seen.has(sub.id)) {
        return false;
      }
      seen.add(sub.id);
      return true;
    });

    return [...unique];
  });

  readonly articles = computed(() => {
    const cat = this.category();
    return cat?.articles ? [...cat.articles] : [];
  });

  private readonly localSubcategories = signal<HelpSubcategory[]>([]);
  private readonly localArticles = signal<HelpArticle[]>([]);
  private readonly isDraggingSubcategories = signal<boolean>(false);
  private readonly isDraggingArticles = signal<boolean>(false);

  readonly displaySubcategories = computed(() =>
    this.isDraggingSubcategories()
      ? this.localSubcategories()
      : this.subcategories()
  );

  readonly displayArticles = computed(() =>
    this.isDraggingArticles() ? this.localArticles() : this.articles()
  );

  readonly showEditModal = signal<boolean>(false);
  readonly showDeleteModal = signal<boolean>(false);
  readonly showAddSubcategoryModal = signal<boolean>(false);
  readonly showAddArticleModal = signal<boolean>(false);

  readonly isExpanded = computed(() =>
    this.accordionState.isCategoryExpanded(this.category().id)
  );

  toggleExpansion(): void {
    this.accordionState.toggleCategory(this.category().id);
    this.autoExpandSingleSubcategory();
  }

  private autoExpandSingleSubcategory(): void {
    const categoryId = this.category().id;
    const subcats = this.subcategories();

    if (
      this.accordionState.isCategoryExpanded(categoryId) &&
      subcats.length === 1
    ) {
      const singleSubcategory = subcats[0];
      if (
        !this.accordionState.isSubcategoryExpanded(
          categoryId,
          singleSubcategory.id
        )
      ) {
        this.accordionState.expandSubcategory(categoryId, singleSubcategory.id);
      }
    }
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

  async onSubcategoryDrop(
    event: CdkDragDrop<HelpSubcategory[]>
  ): Promise<void> {
    if (!this.category().id) {
      console.error('[HelpCategoryPanel] No category ID found');
      return;
    }

    const current = [...this.subcategories()];
    this.isDraggingSubcategories.set(true);

    moveItemInArray(current, event.previousIndex, event.currentIndex);
    this.localSubcategories.set(current);

    try {
      const subcategoryIds = current
        .map((s) => s.id)
        .filter((id): id is string => id !== undefined);

      // Remove duplicates using Set
      const uniqueIds = Array.from(new Set(subcategoryIds));

      if (uniqueIds.length !== subcategoryIds.length) {
        console.error(
          '[HelpCategoryPanel] DUPLICATE SUBCATEGORY IDs DETECTED!',
          {
            original: subcategoryIds,
            unique: uniqueIds,
            duplicates: subcategoryIds.filter(
              (id, index) => subcategoryIds.indexOf(id) !== index
            ),
          }
        );
      }

      await firstValueFrom(
        this.hierarchyService.putCategorySubcategoriesOrder(
          this.category().id,
          uniqueIds
        )
      );

      this.categoryChanged.emit();
    } catch (error) {
      console.error(
        '[HelpCategoryPanel] Failed to save subcategory order',
        error
      );

      const errorMessage = this.translocoService.translate(
        'help.reorder.subcategory.failed'
      );
      this.uiMessageService.addErrorMessage(errorMessage);

      // Revert to original order
      this.categoryChanged.emit();
    } finally {
      this.isDraggingSubcategories.set(false);
    }
  }

  async onArticleDrop(event: CdkDragDrop<HelpArticle[]>): Promise<void> {
    if (!this.category().id) {
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
        this.hierarchyService.putCategoryArticlesOrder(
          this.category().id,
          articleIds
        )
      );

      this.categoryChanged.emit();
    } catch (error) {
      console.error('Failed to save article order', error);
    } finally {
      this.isDraggingArticles.set(false);
    }
  }

  onArticleClicked(articleId: string): void {
    const category = this.category();
    this.articleViewerService.openArticle(
      articleId,
      category.id,
      null,
      category.name,
      undefined
    );
  }

  onEditCategory(): void {
    this.showEditModal.set(true);
  }

  onDeleteCategory(): void {
    this.showDeleteModal.set(true);
  }

  onAddClick(): void {
    const dialogRef = this.dialog.open(AddChoiceDialogComponent, {
      width: '500px',
    });

    dialogRef.afterClosed().subscribe((choice: AddChoiceType | undefined) => {
      if (choice === 'article') {
        this.showAddArticleModal.set(true);
      } else if (choice === 'subcategory') {
        this.showAddSubcategoryModal.set(true);
      }
    });
  }

  async onCategoryUpdated(result: ActionEmitterResult): Promise<void> {
    if (result.result === ActionResult.SUCCEED) {
      this.categoryChanged.emit();
    }
  }

  async onSubcategorySaved(result: ActionEmitterResult): Promise<void> {
    if (result.result === ActionResult.SUCCEED) {
      this.categoryChanged.emit();
    }
  }

  async onArticleCreated(result: ActionEmitterResult): Promise<void> {
    if (result.result === ActionResult.SUCCEED) {
      this.categoryChanged.emit();
    }
  }

  async onDeleteCategoryConfirmed(): Promise<void> {
    if (!this.category().id) {
      return;
    }

    try {
      await firstValueFrom(
        this.helpService.deleteHelpCategory(this.category().id)
      );

      this.categoryChanged.emit();
    } catch (error) {
      console.error('Failed to delete category', error);
      const errorMessage = this.translocoService.translate(
        'help.delete.category.failed'
      );
      this.uiMessageService.addErrorMessage(errorMessage);
    }
  }

  onCategoryChanged(): void {
    this.categoryChanged.emit();
  }

  async onPasteArticle(): Promise<void> {
    const copied = this.clipboardService.copiedArticle();
    if (!(copied && this.category().id)) return;

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
        this.helpService.createCategoryArticle(this.category().id, newArticle)
      );

      this.clipboardService.clear();
      this.categoryChanged.emit();
    } catch (error) {
      console.error('[HelpCategoryPanel] Failed to paste article', error);
      const errorMessage = this.translocoService.translate(
        'help.paste.article.failed'
      );
      this.uiMessageService.addErrorMessage(errorMessage);
    }
  }
}
