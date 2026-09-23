import { DatePipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  effect,
  inject,
  resource,
} from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { DomSanitizer } from '@angular/platform-browser';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { ActionEmitterResult, ActionResult } from 'app/action-result';
import {
  HelpArticle,
  HelpCategory,
  HelpService,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { AddHelpArticleComponent } from 'app/help/add-help-article/add-help-article.component';
import { DeleteHelpArticleComponent } from 'app/help/delete-help-article/delete-help-article.component';
import { ArticleListSelectComponent } from 'app/help/help-article/article-list-select/article-list-select.component';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';

/**
 * Standalone Angular component that renders a single help article together
 * with its surrounding help category context.
 *
 * The template displays the localized title and (sanitized) HTML content of
 * the currently selected {@link HelpArticle}, the list of sibling articles in
 * the current {@link HelpCategory}, and a category switcher. When the logged
 * in user is a platform/CIRCABC administrator it also exposes administrative
 * affordances such as highlighting, editing and deleting the article via the
 * embedded {@link AddHelpArticleComponent} and {@link DeleteHelpArticleComponent}.
 *
 * Article and category data are resolved from the route parameters
 * (`categoryId` / `articleId`) and fetched through the generated
 * {@link HelpService}. Localization is handled by {@link TranslocoService} and
 * HTML content is trusted through {@link DomSanitizer}.
 *
 * Selector: `cbc-help-article`.
 */
@Component({
  selector: 'cbc-help-article',
  templateUrl: './help-article.component.html',
  styleUrl: './help-article.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
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
export class HelpArticleComponent {
  /** Generated API client used to fetch help categories and articles. */
  private readonly helpService = inject(HelpService);
  /** Provides access to the current route parameters (`categoryId`, `articleId`). */
  private readonly route = inject(ActivatedRoute);
  /** Used to navigate away after deletion or when switching category. */
  private readonly router = inject(Router);
  /** Supplies the current user and guest status for permission checks. */
  private readonly loginService = inject(LoginService);
  /** Resolves the active/default language for localized title and content. */
  private readonly translateService = inject(TranslocoService);
  /** Marks raw article HTML content as trusted for rendering. */
  private readonly sanitizer = inject(DomSanitizer);
  /** Builds the reactive form used by the category switcher. */
  private readonly fb = inject(FormBuilder);

  /** Raw route parameters, kept up to date as the router navigates. */
  private readonly routeParams = toSignal(this.route.params, {
    initialValue: {} as Record<string, string>,
  });

  /** Identifier of the help category currently displayed, resolved from the route. */
  private readonly categoryId = computed(
    () => this.routeParams()?.categoryId ?? ''
  );
  /** Identifier of the help article currently displayed, resolved from the route. */
  private readonly articleId = computed(
    () => this.routeParams()?.articleId ?? ''
  );

  /**
   * Loads the full list of help categories, used to populate the category
   * switcher. Loaded once, without parameters.
   */
  private readonly categoriesResource = resource({
    loader: () => this.helpService.getHelpCategoriesAsync(),
  });

  /** All available help categories, used to populate the category switcher. */
  public readonly categories = computed(
    () => this.categoriesResource.value() ?? []
  );

  /**
   * Loads the currently selected {@link HelpCategory} together with the
   * articles it contains. Idle while no category id is present in the route,
   * mirroring the previous `if (params.categoryId)` guard. On failure the user
   * is redirected back to the help landing page, matching the previous
   * behaviour, and the resource resolves to empty defaults instead of ending
   * in the error state (there is no dedicated error UI for this call).
   */
  private readonly categoryResource = resource<
    { category: HelpCategory | undefined; articles: HelpArticle[] },
    string
  >({
    params: () => this.categoryId() || undefined,
    loader: async ({ params: id }) => {
      try {
        const category = await this.helpService.getHelpCategoryAsync({ id });
        const articles = await this.helpService.getCategoryArticlesAsync({
          id,
          skipcontent: true,
        });
        return { category, articles };
      } catch (error) {
        console.error(error);
        this.router.navigate(['/help']);
        return { category: undefined, articles: [] };
      }
    },
  });

  /** The category the current article belongs to. */
  public readonly category = computed(
    () => this.categoryResource.value()?.category
  );
  /** Articles belonging to the currently loaded category. */
  public readonly articles = computed(
    () => this.categoryResource.value()?.articles ?? []
  );

  /**
   * Loads the currently displayed {@link HelpArticle}. Idle while no article
   * id is present in the route. The error state is preserved (not caught) so
   * that {@link loadingError} can drive the template's dedicated error UI.
   */
  private readonly articleResource = resource({
    params: () => this.articleId() || undefined,
    loader: ({ params: id }) => this.helpService.getHelpArticleAsync({ id }),
  });

  /** The currently displayed help article. */
  public readonly article = computed(() =>
    this.articleResource.hasValue() ? this.articleResource.value() : undefined
  );
  /** True when the requested article could not be loaded. */
  public readonly loadingError = computed(
    () => this.articleResource.status() === 'error'
  );

  /** Whether an asynchronous data operation is in progress (drives the loader). */
  public readonly loading = computed(
    () =>
      this.categoriesResource.isLoading() ||
      this.categoryResource.isLoading() ||
      this.articleResource.isLoading()
  );

  /** Whether the actions dropdown menu is currently visible. */
  public dropdownVisible = false;
  /** Whether the delete-article confirmation modal is shown. */
  public showDeleteModal = false;
  /** Whether the edit-article modal is shown. */
  public showEditModal = false;
  /** Reactive form holding the selected category id for the category switcher. */
  public switchCategoryForm: FormGroup = this.fb.group({
    categoryId: [''],
  });

  constructor() {
    // Keeps the category switcher form in sync with the route-resolved
    // category id. This is a genuine imperative side effect on the
    // (non-signal) reactive-forms API, not state derivation between signals.
    effect(() => {
      const categoryId = this.categoryId();
      if (categoryId) {
        this.switchCategoryForm.controls.categoryId.setValue(categoryId, {
          emitEvent: false,
        });
      }
    });
  }

  /**
   * Determines whether the current user has administrative privileges
   * (platform admin or CIRCABC admin) that unlock the article management
   * actions. Guests always return `false`.
   *
   * @returns `true` if the logged in user is an admin or CIRCABC admin,
   *   otherwise `false`.
   */
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

  /**
   * Handles the result emitted after an article deletion. When the deletion
   * succeeded the user is navigated two levels up in the route hierarchy.
   *
   * @param res - The result emitted by the delete-article component.
   * @returns A promise that resolves once navigation (if any) is triggered.
   */
  public async redirectAfterDeletion(res: ActionEmitterResult) {
    if (res.result === ActionResult.SUCCEED) {
      this.router.navigate(['../../'], { relativeTo: this.route });
    }
  }

  /**
   * Reloads the currently displayed article, typically after it has been
   * edited, so the view reflects the latest data.
   *
   * @param _result - The result emitted by the editing component (unused).
   */
  public refresh(_result: ActionEmitterResult) {
    this.articleResource.reload();
  }

  /**
   * Resolves the article content for display, preferring the active language
   * and falling back to the default language, then to the first available
   * translation. The resulting HTML is trusted through {@link DomSanitizer}
   * because help content is authored by administrators.
   *
   * @returns A `SafeHtml` value for the localized content, or an empty string
   *   when no content is available.
   */
  public getContent() {
    const article = this.article();
    if (article?.content === undefined) {
      return '';
    }

    let result = '';
    const lang = this.translateService.getActiveLang();
    const defaultLang = this.translateService.getDefaultLang();

    if (article.content[lang] === undefined) {
      if (article.content[defaultLang] === undefined) {
        const keys = Object.keys(article.content);
        if (keys.length > 0) {
          result = article.content[keys[0]];
        }
      } else {
        result = article.content[defaultLang];
      }
    } else {
      result = article.content[lang];
    }
    // NOSONAR: Safe - HTML content comes from admin-managed help articles stored in backend database
    return this.sanitizer.bypassSecurityTrustHtml(result); // NOSONAR
  }

  /**
   * Resolves the article title for display, preferring the active language and
   * falling back to the default language, then to the first available
   * translation.
   *
   * @returns The localized title string, or an empty string when no title is
   *   available.
   */
  public getTitle() {
    const article = this.article();
    if (article?.title === undefined) {
      return '';
    }

    let result = '';
    const lang = this.translateService.getActiveLang();
    const defaultLang = this.translateService.getDefaultLang();

    if (article.title[lang] === undefined) {
      if (article.title[defaultLang] === undefined) {
        const keys = Object.keys(article.title);
        if (keys.length > 0) {
          result = article.title[keys[0]];
        }
      } else {
        result = article.title[defaultLang];
      }
    } else {
      result = article.title[lang];
    }
    return result;
  }

  /**
   * Toggles the "highlighted" status of the current article via the backend
   * and updates the local article reference with the returned value.
   *
   * @returns A promise that resolves once the toggle request completes.
   */
  public async toggleHighlight() {
    const article = this.article();
    if (article?.id) {
      try {
        this.articleResource.value.set(
          await this.helpService.toggleHighlightArticleAsync({
            id: article.id,
          })
        );
      } catch (error) {
        console.error(error);
      }
    }
  }

  /**
   * Navigates to the help view for the category currently selected in the
   * {@link switchCategoryForm}, effectively switching the displayed category.
   *
   * @returns A promise that resolves once navigation is triggered.
   */
  public async refreshAfterSelection() {
    this.router.navigate(
      ['../../../', this.switchCategoryForm.value.categoryId],
      { relativeTo: this.route }
    );
  }
}
