import { DatePipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  OnInit,
  signal,
  inject,
} from '@angular/core';
import {
  CdkDragDrop,
  CdkDrag,
  CdkDropList,
  moveItemInArray,
} from '@angular/cdk/drag-drop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { ActionEmitterResult, ActionResult } from 'app/action-result';
import {
  HelpArticle,
  HelpCategory,
  HelpService,
  HelpSubcategory,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { AddHelpArticleComponent } from 'app/help/add-help-article/add-help-article.component';
import { AddHelpSubcategoryComponent } from 'app/help/add-help-subcategory/add-help-subcategory.component';
import { DeleteHelpSubcategoryComponent } from 'app/help/delete-help-subcategory/delete-help-subcategory.component';
import { HelpActionButtonsComponent } from 'app/help/components/help-action-buttons/help-action-buttons.component';
import { HelpSubcategoryFormModalComponent } from 'app/help/components/help-subcategory-form-modal/help-subcategory-form-modal.component';
import { HelpDeleteConfirmationModalComponent } from 'app/help/components/help-delete-confirmation-modal/help-delete-confirmation-modal.component';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-help-subcategory',
  templateUrl: './help-subcategory.component.html',
  styleUrl: './help-subcategory.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    HorizontalLoaderComponent,
    RouterLink,
    DatePipe,
    I18nPipe,
    TranslocoModule,
    CdkDropList,
    CdkDrag,
    AddHelpArticleComponent,
    AddHelpSubcategoryComponent,
    DeleteHelpSubcategoryComponent,
    HelpActionButtonsComponent,
    HelpSubcategoryFormModalComponent,
    HelpDeleteConfirmationModalComponent,
  ],
})
export class HelpSubcategoryComponent implements OnInit {
  private readonly helpService = inject(HelpService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly loginService = inject(LoginService);

  readonly loading = signal(false);
  readonly category = signal<HelpCategory | undefined>(undefined);
  readonly subcategory = signal<HelpSubcategory | undefined>(undefined);
  readonly articles = signal<HelpArticle[]>([]);
  public showCreateArticleModal = false;
  public showEditSubcategoryModal = false;
  public showDeleteSubcategoryModal = false;

  // New modal visibility signals for new components
  readonly showSubcategoryFormModal = signal<boolean>(false);
  readonly showSubcategoryDeleteModal = signal<boolean>(false);
  readonly selectedSubcategoryForEdit = signal<string | undefined>(undefined);

  async ngOnInit(): Promise<void> {
    this.loading.set(true);
    try {
      this.route.params.subscribe(async (params) => {
        if (params['categoryId'] && params['subcategoryId']) {
          await this.loadData(params['categoryId'], params['subcategoryId']);
        }
      });
    } catch (error) {
      console.error('Failed to initialize subcategory page', error);
    }
    this.loading.set(false);
  }

  async loadData(categoryId: string, subcategoryId: string): Promise<void> {
    this.loading.set(true);
    try {
      const [cat, subcat] = await Promise.all([
        firstValueFrom(this.helpService.getHelpCategory(categoryId)),
        firstValueFrom(this.helpService.getHelpSubcategory(subcategoryId)),
      ]);
      this.category.set(cat);
      this.subcategory.set(subcat);

      const arts = await firstValueFrom(
        this.helpService.getSubcategoryArticles(subcategoryId, true)
      );
      this.articles.set(arts);
    } catch (error) {
      console.error('Failed to load subcategory data', error);
    }
    this.loading.set(false);
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

  async refreshArticles(): Promise<void> {
    const subcat = this.subcategory();
    if (subcat?.id) {
      const arts = await firstValueFrom(
        this.helpService.getSubcategoryArticles(subcat.id, true)
      );
      this.articles.set(arts);
    }
  }

  async onArticleCreated(res: ActionEmitterResult): Promise<void> {
    if (res.result === ActionResult.SUCCEED) {
      await this.refreshArticles();
    }
  }

  async onSubcategoryUpdated(res: ActionEmitterResult): Promise<void> {
    const cat = this.category();
    const subcat = this.subcategory();
    if (res.result === ActionResult.SUCCEED && cat?.id && subcat?.id) {
      await this.loadData(cat.id, subcat.id);
    }
  }

  async onSubcategoryDeleted(): Promise<void> {
    const cat = this.category();
    if (cat?.id) {
      await this.router.navigate(['/help/category', cat.id]);
    }
  }

  async onArticleDrop(event: CdkDragDrop<HelpArticle[]>): Promise<void> {
    const subcat = this.subcategory();
    if (!subcat?.id) {
      return;
    }

    const previousOrder = [...this.articles()];
    const current = [...this.articles()];

    moveItemInArray(current, event.previousIndex, event.currentIndex);
    this.articles.set(current);

    try {
      const articleIds = current
        .map((a) => a.id)
        .filter((id): id is string => id !== undefined);

      await firstValueFrom(
        this.helpService.putSubcategoryArticlesOrder(subcat.id, articleIds)
      );
    } catch (error) {
      console.error('Failed to save article order', error);
      this.articles.set(previousOrder);
    }
  }

  // Action button handlers for subcategory
  onEditSubcategory(): void {
    const subcat = this.subcategory();
    if (subcat?.id) {
      this.selectedSubcategoryForEdit.set(subcat.id);
      this.showSubcategoryFormModal.set(true);
    }
  }

  onDeleteSubcategory(): void {
    this.showSubcategoryDeleteModal.set(true);
  }

  onAddArticle(): void {
    this.showCreateArticleModal = true;
  }

  // Handler for subcategory form save
  async onSubcategorySaved(res: ActionEmitterResult): Promise<void> {
    const cat = this.category();
    const subcat = this.subcategory();
    if (res.result === ActionResult.SUCCEED && cat?.id && subcat?.id) {
      await this.loadData(cat.id, subcat.id);
    }
  }

  // Handler for subcategory delete confirmation
  async onSubcategoryDeleteConfirmed(): Promise<void> {
    const subcat = this.subcategory();
    if (!subcat?.id) {
      return;
    }

    try {
      await firstValueFrom(this.helpService.deleteHelpSubcategory(subcat.id));
      // Navigate back to category page after successful deletion
      const cat = this.category();
      if (cat?.id) {
        await this.router.navigate(['/help/category', cat.id]);
      }
    } catch (error) {
      console.error('Failed to delete subcategory', error);
    }
  }
}
