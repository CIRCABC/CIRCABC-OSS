import { DatePipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  resource,
} from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
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

/**
 * Standalone Angular component rendering the help section for a single help
 * category.
 *
 * It displays the list of available help categories, the currently selected
 * category and the articles that belong to it. For privileged users
 * (administrators / CIRCABC administrators) it also exposes the modals used to
 * create, update and delete categories as well as to add articles.
 *
 * Selected via the `cbc-help-category` selector, the component reads the
 * `categoryId` route parameter to determine which category to load and relies
 * on {@link HelpService} for data access and {@link LoginService} for the
 * permission checks.
 *
 * @see HelpService
 * @see LoginService
 */
@Component({
  selector: 'cbc-help-category',
  templateUrl: './help-category.component.html',
  styleUrl: './help-category.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
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
export class HelpCategoryComponent {
  /** Backend API client used to fetch help categories and their articles. */
  private readonly helpService = inject(HelpService);
  /** Provides access to the current route, including the `categoryId` param. */
  private readonly route = inject(ActivatedRoute);
  /** Angular router used to navigate away from the current category view. */
  private readonly router = inject(Router);
  /** Supplies the authenticated user and guest status for permission checks. */
  private readonly loginService = inject(LoginService);

  /** Raw route parameters, kept up to date as the router navigates. */
  private readonly routeParams = toSignal(this.route.params, {
    initialValue: {} as Record<string, string>,
  });

  /** Identifier of the help category currently displayed, resolved from the route. */
  private readonly categoryId = computed(
    () => this.routeParams()?.categoryId ?? ''
  );

  /**
   * Loads the full list of help categories, used to populate the category
   * list selector. Loaded once, without parameters.
   */
  private readonly categoriesResource = resource({
    loader: () => this.helpService.getHelpCategoriesAsync(),
  });

  /** All help categories available for selection. */
  public readonly categories = computed(
    () => this.categoriesResource.value() ?? []
  );

  /**
   * Loads the currently selected {@link HelpCategory} together with the
   * articles it contains. Idle while no category id is present in the route,
   * mirroring the previous `if (params.categoryId)` guard. There is no
   * dedicated error UI for this call, so failures are caught and the resource
   * resolves to empty defaults instead of ending in the error state.
   */
  private readonly categoryResource = resource<
    { category: HelpCategory | undefined; articles: HelpArticle[] },
    string
  >({
    params: () => this.categoryId() || undefined,
    loader: async ({ params: id }) => {
      try {
        const category = await this.helpService.getHelpCategoryAsync({ id });
        const articles = category.id
          ? await this.helpService.getCategoryArticlesAsync({
              id: category.id,
              skipcontent: true,
            })
          : [];
        return { category, articles };
      } catch (error) {
        console.error(error);
        return { category: undefined, articles: [] };
      }
    },
  });

  /** The currently selected/displayed help category. */
  public readonly category = computed(
    () => this.categoryResource.value()?.category
  );
  /** Articles belonging to the currently selected category. */
  public readonly articles = computed(
    () => this.categoryResource.value()?.articles ?? []
  );

  /** Whether an asynchronous data load is currently in progress. */
  public readonly loading = computed(
    () =>
      this.categoriesResource.isLoading() || this.categoryResource.isLoading()
  );

  /** Controls the visibility of the category actions dropdown menu. */
  public dropdownVisible = false;
  /** Controls the visibility of the "create category/article" modal. */
  public showCreateModal = false;
  /** Controls the visibility of the "delete category" modal. */
  public showDeleteModal = false;
  /** Controls the visibility of the "update category" modal. */
  public showUpdateModal = false;

  /**
   * Determines whether the current user is allowed to manage help content.
   *
   * @returns `true` when the user is authenticated (not a guest) and has either
   * the `isAdmin` or `isCircabcAdmin` property set; otherwise `false`.
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
   * Reloads the current category after a successful child action (e.g. adding
   * or updating an article). No-op when the action did not succeed or when no
   * category is currently selected.
   *
   * @param res - The result emitted by a child action component.
   */
  public refresh(res: ActionEmitterResult) {
    const category = this.category();
    if (res.result === ActionResult.SUCCEED && category?.id) {
      this.categoryResource.reload();
    }
  }

  /**
   * Reloads both the full list of categories and the current category after a
   * successful child action that may have altered the category set (e.g.
   * creating or deleting a category). No-op when the action did not succeed or
   * when no category is currently selected.
   *
   * @param res - The result emitted by a child action component.
   */
  public reload(res: ActionEmitterResult) {
    const category = this.category();
    if (res.result === ActionResult.SUCCEED && category?.id) {
      this.categoriesResource.reload();
      this.categoryResource.reload();
    }
  }

  /**
   * Navigates two levels up relative to the current route, typically used to
   * leave the current category view after it has been deleted.
   */
  public redirect() {
    this.router.navigate(['../../'], { relativeTo: this.route });
  }
}
