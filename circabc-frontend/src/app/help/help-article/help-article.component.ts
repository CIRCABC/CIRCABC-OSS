import { moveItemInArray } from '@angular/cdk/drag-drop';
import { DatePipe } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { DomSanitizer } from '@angular/platform-browser';
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
import { DeleteHelpArticleComponent } from 'app/help/delete-help-article/delete-help-article.component';
import { ArticleListSelectComponent } from 'app/help/help-article/article-list-select/article-list-select.component';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-help-article',
  templateUrl: './help-article.component.html',
  styleUrl: './help-article.component.scss',
  imports: [
    HorizontalLoaderComponent,
    ReactiveFormsModule,
    RouterLink,
    ArticleListSelectComponent,
    DeleteHelpArticleComponent,
    AddHelpArticleComponent,
    DatePipe,
    I18nPipe,
    TranslocoModule,
  ],
})
export class HelpArticleComponent implements OnInit {
  public categories: HelpCategory[] = [];
  public articles: HelpArticle[] = [];
  public article!: HelpArticle;
  public category!: HelpCategory;
  public subcategory: HelpSubcategory | undefined;
  public loading = false;
  public dropdownVisible = false;
  public showDeleteModal = false;
  public showEditModal = false;
  public showCreateModal = false;
  public selectedArticleId: string | undefined;
  public switchCategoryForm!: FormGroup;
  public loadingError = false;

  constructor(
    private helpService: HelpService,
    private route: ActivatedRoute,
    private router: Router,
    private loginService: LoginService,
    private translateService: TranslocoService,
    private sanitizer: DomSanitizer,
    private fb: FormBuilder,
    private uiMessageService: UiMessageService
  ) {}

  async ngOnInit() {
    this.switchCategoryForm = this.fb.group({
      categoryId: [''],
    });

    this.loading = true;
    try {
      this.categories = await firstValueFrom(
        this.helpService.getHelpCategories()
      );

      this.route.params.subscribe(async (params) => {
        if (params.categoryId) {
          await this.loadCategory(params.categoryId);
          this.switchCategoryForm.controls.categoryId.setValue(
            params.categoryId
          );
        }

        if (params.subcategoryId) {
          await this.loadSubcategory(params.subcategoryId);
        } else {
          this.subcategory = undefined;
        }

        if (params.articleId) {
          await this.loadArticle(params.articleId);
        }
      });
    } catch (error) {
      console.error(error);
    }

    this.loading = false;
  }

  async loadCategory(id: string) {
    try {
      this.category = await firstValueFrom(
        this.helpService.getHelpCategory(id)
      );

      this.articles = await firstValueFrom(
        this.helpService.getCategoryArticles(id, true)
      );
    } catch (_error) {
      // eslint-disable-next-line @typescript-eslint/no-floating-promises
      this.router.navigate(['/help']);
    }
  }

  async loadSubcategory(id: string) {
    try {
      this.subcategory = await firstValueFrom(
        this.helpService.getHelpSubcategory(id)
      );

      this.articles = await firstValueFrom(
        this.helpService.getSubcategoryArticles(id, true)
      );
    } catch (error) {
      console.error('Failed to load subcategory', error);
      this.subcategory = undefined;
    }
  }

  async loadArticle(id: string) {
    try {
      this.article = await firstValueFrom(this.helpService.getHelpArticle(id));
    } catch (_error) {
      this.loadingError = true;
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

  public getContent() {
    if (this.article.content === undefined) {
      return '';
    }

    let result = '';
    const lang = this.translateService.getActiveLang();
    const defaultLang = this.translateService.getDefaultLang();

    if (this.article.content[lang] === undefined) {
      if (this.article.content[defaultLang] === undefined) {
        const keys = Object.keys(this.article.content);
        if (keys.length > 0) {
          result = this.article.content[keys[0]];
        }
      } else {
        result = this.article.content[defaultLang];
      }
    } else {
      result = this.article.content[lang];
    }
    return this.sanitizer.bypassSecurityTrustHtml(result);
  }

  public getTitle() {
    if (this.article.title === undefined) {
      return '';
    }

    let result = '';
    const lang = this.translateService.getActiveLang();
    const defaultLang = this.translateService.getDefaultLang();

    if (this.article.title[lang] === undefined) {
      if (this.article.title[defaultLang] === undefined) {
        const keys = Object.keys(this.article.title);
        if (keys.length > 0) {
          result = this.article.title[keys[0]];
        }
      } else {
        result = this.article.title[defaultLang];
      }
    } else {
      result = this.article.title[lang];
    }
    return result;
  }

  public async toggleHighlight() {
    this.loading = true;
    try {
      if (this.article.id) {
        this.article = await firstValueFrom(
          this.helpService.toggleHighlightArticle(this.article.id)
        );
      }
    } catch (error) {
      console.error(error);
    }
    this.loading = false;
  }

  public async refreshAfterSelection() {
    // eslint-disable-next-line @typescript-eslint/no-floating-promises
    this.router.navigate(
      ['../../../', this.switchCategoryForm.value.categoryId],
      { relativeTo: this.route }
    );
  }

  public async onArticleDrop(event: {
    previousIndex: number;
    currentIndex: number;
  }) {
    const previousOrder = this.articles.map((a) => a.id);
    moveItemInArray(this.articles, event.previousIndex, event.currentIndex);
    const newOrder = this.articles
      .map((a) => a.id)
      .filter((id): id is string => id !== undefined);

    try {
      const subcategoryId = this.subcategory?.id;
      const categoryId = this.category?.id;
      if (subcategoryId) {
        await firstValueFrom(
          this.helpService.putSubcategoryArticlesOrder(subcategoryId, newOrder)
        );
      } else if (categoryId) {
        await firstValueFrom(
          this.helpService.putCategoryArticlesOrder(categoryId, newOrder)
        );
      }
    } catch (error) {
      console.error('Failed to save article order', error);
      // Revert to previous order on error
      this.articles = previousOrder
        .map((id) => this.articles.find((a) => a.id === id))
        .filter((a): a is HelpArticle => a !== undefined);
    }
  }

  public onAddArticle(): void {
    this.selectedArticleId = undefined;
    this.showCreateModal = true;
  }

  public onEditArticle(articleId: string): void {
    this.selectedArticleId = articleId;
    this.showEditModal = true;
  }

  public onDeleteArticle(articleId: string): void {
    this.selectedArticleId = articleId;
    this.showDeleteModal = true;
  }

  public async onArticleCreated(result: ActionEmitterResult): Promise<void> {
    if (result.result === ActionResult.SUCCEED) {
      // Reload articles list
      if (this.subcategory?.id) {
        await this.loadSubcategory(this.subcategory.id);
      } else if (this.category?.id) {
        await this.loadCategory(this.category.id);
      }
    }
  }

  public async onArticleUpdated(result: ActionEmitterResult): Promise<void> {
    if (result.result === ActionResult.SUCCEED) {
      // Reload current article and articles list
      if (this.article?.id) {
        await this.loadArticle(this.article.id);
      }
      if (this.subcategory?.id) {
        await this.loadSubcategory(this.subcategory.id);
      } else if (this.category?.id) {
        await this.loadCategory(this.category.id);
      }
    }
  }

  public async onArticleDeleted(result: ActionEmitterResult): Promise<void> {
    if (result.result === ActionResult.SUCCEED) {
      // Navigate back to category or subcategory page
      if (this.subcategory) {
        // eslint-disable-next-line @typescript-eslint/no-floating-promises
        this.router.navigate(['../../'], { relativeTo: this.route });
      } else {
        // eslint-disable-next-line @typescript-eslint/no-floating-promises
        this.router.navigate(['../../'], { relativeTo: this.route });
      }
    }
  }
}
