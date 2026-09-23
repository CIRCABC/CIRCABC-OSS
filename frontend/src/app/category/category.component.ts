import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  resource,
} from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import {
  ActivatedRoute,
  Router,
  RouterLink,
  RouterOutlet,
} from '@angular/router';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result/index';
import { CategoryDescriptorComponent } from 'app/category/category-descriptor/category-descriptor.component';
import { CreateGroupComponent } from 'app/category/create-group/create-group.component';
import { CategoryService, User } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { getErrorTranslation, getSuccessTranslation } from 'app/core/util';
import { DataCyDirective } from 'app/shared/directives/data-cy.directive';
import { HeaderComponent } from 'app/shared/header/header.component';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { NavigatorComponent } from 'app/shared/navigator/navigator.component';

/**
 * Top-level page component for a single CIRCABC category.
 *
 * Renders the category shell: a header, a navigator, the category descriptor
 * and a set of tabbed sub-sections (details, administrators, actions,
 * customisation, statistics, support, group requests and interest groups)
 * displayed through a nested `<router-outlet>`. It also hosts the modal used
 * to create a new interest group within the category.
 *
 * The component resolves the current category id from the route, loads the
 * list of category administrators to determine whether the current user has
 * administrative rights, exposes helpers to highlight the active sub-route in
 * the navigation, and orchestrates the create-group workflow by surfacing
 * success/error notifications and navigating to the newly created group.
 *
 * Key collaborators: {@link Router} and {@link ActivatedRoute} for routing,
 * {@link LoginService} for the current user, {@link CategoryService} for
 * administrator data, {@link TranslocoService} for i18n and
 * {@link UiMessageService} for user feedback.
 */
@Component({
  selector: 'cbc-category',
  templateUrl: './category.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    HeaderComponent,
    NavigatorComponent,
    HorizontalLoaderComponent,
    DataCyDirective,
    CategoryDescriptorComponent,
    RouterLink,
    RouterOutlet,
    CreateGroupComponent,
    TranslocoModule,
  ],
})
export class CategoryComponent {
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly loginService = inject(LoginService);
  private readonly translateService = inject(TranslocoService);
  private readonly uiMessageService = inject(UiMessageService);
  private readonly categoryService = inject(CategoryService);

  /** Controls the visibility of the "create interest group" modal. */
  public showModalCreate = false;

  /** Raw route parameters, kept up to date as the router navigates. */
  private readonly routeParams = toSignal(this.route.params, {
    initialValue: {} as Record<string, string>,
  });

  /** Identifier of the category currently displayed, resolved from the route. */
  public readonly categoryId = computed(() => this.routeParams()?.id ?? '');

  /**
   * Loads the administrators of the current {@link categoryId}. Idle while
   * no id is present in the route, mirroring the previous `if (params?.id)`
   * guard. Falls back to an empty list on error, matching the previous
   * behaviour (no dedicated error UI for this call).
   */
  private readonly adminsResource = resource<User[], string>({
    params: () => this.categoryId() || undefined,
    loader: async ({ params: id }) => {
      try {
        return await this.categoryService.getCategoryAdministratorsAsync({
          id,
        });
      } catch (e) {
        console.error(e);
        return [];
      }
    },
  });

  /** Administrators of the current category, used to derive admin permissions. */
  private readonly admins = computed(() => this.adminsResource.value() ?? []);

  /** Indicates whether an asynchronous operation is in progress (loading state). */
  public readonly loading = this.adminsResource.isLoading;

  /**
   * Determines whether the current router URL corresponds to the given
   * sub-route, used to highlight the active navigation entry.
   *
   * @param routeName The route fragment to test against the current URL.
   * @returns `true` if the current URL contains `routeName`, otherwise `false`.
   */
  public checkCurrentRouteActive(routeName: string): boolean {
    return this.router.url.includes(routeName);
  }

  /**
   * @returns `true` if the "details" sub-route is currently active.
   */
  public isDetailsRoute(): boolean {
    return this.checkCurrentRouteActive('details');
  }

  /**
   * @returns `true` if the "administrators" sub-route is currently active.
   */
  public isAdministratorsRoute(): boolean {
    return this.checkCurrentRouteActive('administrators');
  }

  /**
   * @returns `true` if the "actions" sub-route is currently active.
   */
  public isActionsRoute(): boolean {
    return this.checkCurrentRouteActive('actions');
  }

  /**
   * @returns `true` if the "customisation" sub-route is currently active.
   */
  public isCustomisationRoute(): boolean {
    return this.checkCurrentRouteActive('customisation');
  }

  /**
   * @returns `true` if the interest-group statistics ("ig-statistics")
   * sub-route is currently active.
   */
  public isStatisticsRoute(): boolean {
    return this.checkCurrentRouteActive('ig-statistics');
  }

  /**
   * @returns `true` if the "support" sub-route is currently active.
   */
  public isSupportRoute(): boolean {
    return this.checkCurrentRouteActive('support');
  }

  /**
   * @returns `true` if the "group-requests" sub-route is currently active.
   */
  public isGroupRequestsRoute(): boolean {
    return this.checkCurrentRouteActive('group-requests');
  }

  /**
   * @returns `true` if the "interest-groups" sub-route is currently active.
   */
  public isInterestGroupsRoute(): boolean {
    return this.checkCurrentRouteActive('interest-groups');
  }

  /**
   * Checks whether the currently logged-in user is an administrator of this
   * category. Guests are never considered administrators.
   *
   * @returns `true` if the current user's id matches one of the loaded
   * {@link admins}, otherwise `false`.
   */
  public isCategoryAdmin(): boolean {
    if (!this.loginService.isGuest()) {
      const user = this.loginService.getUser();

      for (const admin of this.admins()) {
        if (admin.userId === user.userId) {
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Handles the result emitted when the create-group modal is closed.
   *
   * Depending on the {@link ActionEmitterResult}, this either hides the modal
   * on cancellation, completes the successful creation flow, or surfaces the
   * appropriate error message when the group already exists or creation fails.
   *
   * @param res The result emitted by the create-group modal, describing the
   * action type and its outcome.
   * @returns A promise that resolves once the result has been processed.
   */
  public async onCreateGroupClosed(res: ActionEmitterResult) {
    const isCreateGroup = res.type === ActionType.CREATE_INTEREST_GROUP;
    const isGroupExists = res.type === ActionType.CREATE_INTEREST_GROUP_EXISTS;

    if (res.result === ActionResult.CANCELED && isCreateGroup) {
      this.showModalCreate = false;
    } else if (res.result === ActionResult.SUCCEED && isCreateGroup) {
      this.handleCreateSuccess(res);
    } else if (res.result === ActionResult.FAILED && isGroupExists) {
      this.showErrorMessage(ActionType.CREATE_INTEREST_GROUP_EXISTS, false);
    } else if (res.result === ActionResult.FAILED && isCreateGroup) {
      this.showModalCreate = false;
      this.showErrorMessage(ActionType.CREATE_INTEREST_GROUP, true);
    }
  }

  /**
   * Completes a successful group-creation flow: closes the modal, shows a
   * success notification and, when a node is present in the result, navigates
   * to the newly created group.
   *
   * @param res The successful create-group result, optionally containing the
   * created group node.
   */
  private handleCreateSuccess(res: ActionEmitterResult) {
    this.showModalCreate = false;
    this.showSuccessMessage(ActionType.CREATE_INTEREST_GROUP);
    if (res.node) {
      this.router.navigate(['group', res.node.id]);
    }
  }

  /**
   * Displays a translated success message for the given action type via the
   * UI message service.
   *
   * @param actionType The action type whose success translation is shown.
   */
  private showSuccessMessage(actionType: ActionType) {
    const text = this.translateService.translate(
      getSuccessTranslation(actionType)
    );
    if (text) {
      this.uiMessageService.addSuccessMessage(text, true);
    }
  }

  /**
   * Displays a translated error message for the given action type via the UI
   * message service.
   *
   * @param actionType The action type whose error translation is shown.
   * @param closeModal Whether the create-group modal should be closed when the
   * message is displayed.
   */
  private showErrorMessage(actionType: ActionType, closeModal: boolean) {
    const text = this.translateService.translate(
      getErrorTranslation(actionType)
    );
    if (text) {
      this.uiMessageService.addErrorMessage(text, closeModal);
    }
  }
}
