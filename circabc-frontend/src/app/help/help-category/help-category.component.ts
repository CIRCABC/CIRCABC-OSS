import { DatePipe } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import {
  CdkDragDrop,
  CdkDrag,
  CdkDropList,
  moveItemInArray,
} from '@angular/cdk/drag-drop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { ActionEmitterResult, ActionResult } from 'app/action-result';
import {
  HelpArticle,
  HelpCategory,
  HelpService,
  HelpSubcategory,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { AddHelpArticleComponent } from 'app/help/add-help-article/add-help-article.component';
import { AddHelpSubcategoryComponent } from 'app/help/add-help-subcategory/add-help-subcategory.component';
import { CategoryListSelectComponent } from 'app/help/help-category/category-list-select/category-list-select.component';
import { HelpActionButtonsComponent } from 'app/help/components/help-action-buttons/help-action-buttons.component';
import { HelpCategoryFormModalComponent } from 'app/help/components/help-category-form-modal/help-category-form-modal.component';
import { HelpSubcategoryFormModalComponent } from 'app/help/components/help-subcategory-form-modal/help-subcategory-form-modal.component';
import { HelpDeleteConfirmationModalComponent } from 'app/help/components/help-delete-confirmation-modal/help-delete-confirmation-modal.component';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-help-category',
  templateUrl: './help-category.component.html',
  styleUrl: './help-category.component.scss',
  imports: [
    HorizontalLoaderComponent,
    RouterLink,
    CategoryListSelectComponent,
    AddHelpArticleComponent,
    AddHelpSubcategoryComponent,
    HelpActionButtonsComponent,
    HelpCategoryFormModalComponent,
    HelpSubcategoryFormModalComponent,
    HelpDeleteConfirmationModalComponent,
    DatePipe,
    I18nPipe,
    TranslocoModule,
    CdkDropList,
    CdkDrag,
  ],
})
export class HelpCategoryComponent implements OnInit {
  public articles: HelpArticle[] = [];
  public categories: HelpCategory[] = [];
  public category!: HelpCategory;
  public subcategories: HelpSubcategory[] = [];
  public loading = false;
  public dropdownVisible = false;
  public showCreateModal = false;
  public showDeleteModal = false;
  public showUpdateModal = false;
  public showCreateSubcategoryModal = false;
  public showCreateCategoryModal = false;
  public showEditCategoryModal = false;
  public editingCategoryId: string | undefined = undefined;

  // Subcategory edit/delete modal state
  public showEditSubcategoryModal = false;
  public showDeleteSubcategoryModal = false;
  public selectedSubcategoryForEdit: HelpSubcategory | undefined = undefined;
  public selectedSubcategoryForDelete: HelpSubcategory | undefined = undefined;

  constructor(
    private helpService: HelpService,
    private route: ActivatedRoute,
    private router: Router,
    private loginService: LoginService,
    private uiMessageService: UiMessageService,
    private translocoService: TranslocoService
  ) {}

  async ngOnInit() {
    this.loading = true;
    try {
      this.categories = await firstValueFrom(
        this.helpService.getHelpCategories()
      );

      this.route.params.subscribe(async (params) => {
        if (params.categoryId) {
          await this.loadCategory(params.categoryId);
        }
      });
    } catch (error) {
      console.error(error);
    }

    this.loading = false;
  }

  async loadCategory(id: string) {
    this.category = await firstValueFrom(this.helpService.getHelpCategory(id));

    if (this.category.id) {
      this.subcategories = await firstValueFrom(
        this.helpService.getCategorySubcategories(this.category.id)
      );

      this.articles = await firstValueFrom(
        this.helpService.getCategoryArticles(this.category.id, true)
      );
    }
  }

  public isAdminOrSupport(): boolean {
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

  public async refresh(res: ActionEmitterResult) {
    if (res.result === ActionResult.SUCCEED) {
      // Reload category data to update subcategories list and numberOfSubcategories counter
      if (this.category.id) {
        await this.loadCategory(this.category.id);
      }
    }
  }

  public async reload(res: ActionEmitterResult) {
    if (res.result === ActionResult.SUCCEED) {
      // Reload categories list to update numberOfSubcategories counter
      this.categories = await firstValueFrom(
        this.helpService.getHelpCategories()
      );

      // Reload current category data if viewing one
      if (this.category.id) {
        await this.loadCategory(this.category.id);
      }
    }
  }

  public async redirect() {
    // eslint-disable-next-line @typescript-eslint/no-floating-promises
    this.router.navigate(['../../'], { relativeTo: this.route });
  }

  public async onSubcategoryDrop(
    event: CdkDragDrop<HelpSubcategory[]>
  ): Promise<void> {
    if (!this.category.id) {
      return;
    }

    const previousOrder = [...this.subcategories];
    moveItemInArray(
      this.subcategories,
      event.previousIndex,
      event.currentIndex
    );

    try {
      const subcategoryIds = this.subcategories
        .map((s) => s.id)
        .filter((id): id is string => id !== undefined);

      await firstValueFrom(
        this.helpService.putCategorySubcategoriesOrder(
          this.category.id,
          subcategoryIds
        )
      );
    } catch (error) {
      console.error('Failed to save subcategory order', error);
      this.subcategories = previousOrder;
    }
  }

  public async onArticleDrop(event: CdkDragDrop<HelpArticle[]>): Promise<void> {
    if (!this.category.id) {
      return;
    }

    const previousOrder = [...this.articles];

    moveItemInArray(this.articles, event.previousIndex, event.currentIndex);

    try {
      const articleIds = this.articles
        .map((a) => a.id)
        .filter((id): id is string => id !== undefined);

      await firstValueFrom(
        this.helpService.putCategoryArticlesOrder(this.category.id, articleIds)
      );
    } catch (error) {
      console.error('Failed to save article order', error);
      this.articles = previousOrder;
    }
  }

  public onAddCategoryClick(): void {
    this.editingCategoryId = undefined;
    this.showCreateCategoryModal = true;
  }

  public onEditCategoryClick(): void {
    this.editingCategoryId = this.category.id;
    this.showEditCategoryModal = true;
  }

  public onDeleteCategoryClick(): void {
    this.showDeleteModal = true;
  }

  public onAddSubcategoryClick(): void {
    this.showCreateSubcategoryModal = true;
  }

  public async onDeleteCategoryConfirmed(): Promise<void> {
    if (!this.category.id) {
      return;
    }

    try {
      await firstValueFrom(
        this.helpService.deleteHelpCategory(this.category.id)
      );

      // Redirect to help home after successful deletion
      await this.redirect();
    } catch (error) {
      console.error('Failed to delete category', error);

      // Display error message
      this.uiMessageService.addErrorMessage(
        this.translocoService.translate('help.delete.category.failed'),
        true,
        5000
      );
    }
  }

  // Subcategory action handlers
  public onEditSubcategoryClick(subcategory: HelpSubcategory): void {
    this.selectedSubcategoryForEdit = subcategory;
    this.showEditSubcategoryModal = true;
  }

  public onDeleteSubcategoryClick(subcategory: HelpSubcategory): void {
    this.selectedSubcategoryForDelete = subcategory;
    this.showDeleteSubcategoryModal = true;
  }

  public async onSubcategorySaved(res: ActionEmitterResult): Promise<void> {
    if (res.result === ActionResult.SUCCEED) {
      // Reload category data to update subcategories list
      if (this.category.id) {
        await this.loadCategory(this.category.id);
      }

      // Clear selection
      this.selectedSubcategoryForEdit = undefined;
    }
  }

  public async onDeleteSubcategoryConfirmed(): Promise<void> {
    const subcategory = this.selectedSubcategoryForDelete;
    if (!subcategory?.id) {
      return;
    }

    try {
      await firstValueFrom(
        this.helpService.deleteHelpSubcategory(subcategory.id)
      );

      // Reload category data to update subcategories list
      if (this.category.id) {
        await this.loadCategory(this.category.id);
      }

      // Display success message
      this.uiMessageService.addSuccessMessage(
        this.translocoService.translate('help.delete.subcategory.succeed'),
        true,
        3000
      );
    } catch (error) {
      console.error('Failed to delete subcategory', error);

      // Display error message
      this.uiMessageService.addErrorMessage(
        this.translocoService.translate('help.delete.subcategory.failed'),
        true,
        5000
      );
    } finally {
      // Clear selection
      this.selectedSubcategoryForDelete = undefined;
    }
  }
}
