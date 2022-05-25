import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { DomSanitizer } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslocoService } from '@ngneat/transloco';
import { ActionEmitterResult, ActionResult } from 'app/action-result';
import {
  HelpArticle,
  HelpCategory,
  HelpService,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-help-article',
  templateUrl: './help-article.component.html',
  styleUrls: ['./help-article.component.scss'],
})
export class HelpArticleComponent implements OnInit {
  public categories: HelpCategory[] = [];
  public articles: HelpArticle[] = [];
  public article!: HelpArticle;
  public category!: HelpCategory;
  public loading = false;
  public dropdownVisible = false;
  public showDeleteModal = false;
  public showEditModal = false;
  public switchCategoryForm!: FormGroup;
  public loadingError = false;

  constructor(
    private helpService: HelpService,
    private route: ActivatedRoute,
    private router: Router,
    private loginService: LoginService,
    private translateService: TranslocoService,
    private sanitizer: DomSanitizer,
    private fb: FormBuilder
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
      });

      this.route.params.subscribe(async (params) => {
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
    } catch (error) {
      // eslint-disable-next-line @typescript-eslint/no-floating-promises
      this.router.navigate(['/help']);
    }
  }

  async loadArticle(id: string) {
    try {
      this.article = await firstValueFrom(this.helpService.getHelpArticle(id));
    } catch (error) {
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

  public async redirectAfterDeletion(res: ActionEmitterResult) {
    if (res.result === ActionResult.SUCCEED) {
      // eslint-disable-next-line @typescript-eslint/no-floating-promises
      this.router.navigate(['../../'], { relativeTo: this.route });
    }
  }

  public async refresh(_result: ActionEmitterResult) {
    if (this.article.id) {
      await this.loadArticle(this.article.id);
    }
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
}
