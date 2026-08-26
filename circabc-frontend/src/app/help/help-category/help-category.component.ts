import { DatePipe } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { ActionEmitterResult, ActionResult } from 'app/action-result';
import {
  HelpArticle,
  HelpCategory,
  HelpService,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { AddHelpArticleComponent } from 'app/help/add-help-article/add-help-article.component';
import { AddHelpCategoryComponent } from 'app/help/add-help-category/add-help-category.component';
import { DeleteHelpCategoryComponent } from 'app/help/delete-help-category/delete-help-category.component';
import { CategoryListSelectComponent } from 'app/help/help-category/category-list-select/category-list-select.component';
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
    DeleteHelpCategoryComponent,
    AddHelpCategoryComponent,
    DatePipe,
    I18nPipe,
    TranslocoModule,
  ],
})
export class HelpCategoryComponent implements OnInit {
  public articles: HelpArticle[] = [];
  public categories: HelpCategory[] = [];
  public category!: HelpCategory;
  public loading = false;
  public dropdownVisible = false;
  public showCreateModal = false;
  public showDeleteModal = false;
  public showUpdateModal = false;

  constructor(
    private helpService: HelpService,
    private route: ActivatedRoute,
    private router: Router,
    private loginService: LoginService
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
    if (res.result === ActionResult.SUCCEED && this.category.id) {
      await this.loadCategory(this.category.id);
    }
  }

  public async reload(res: ActionEmitterResult) {
    if (res.result === ActionResult.SUCCEED && this.category.id) {
      this.categories = await firstValueFrom(
        this.helpService.getHelpCategories()
      );
      await this.loadCategory(this.category.id);
    }
  }

  public async redirect() {
    // eslint-disable-next-line @typescript-eslint/no-floating-promises
    this.router.navigate(['../../'], { relativeTo: this.route });
  }
}
