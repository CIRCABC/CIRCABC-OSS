import {
  ChangeDetectionStrategy,
  Component,
  inject,
  resource,
} from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { ActionEmitterResult } from 'app/action-result';
import {
  InterestGroup,
  Profile,
  User,
  UserService,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { QuitGroupComponent } from 'app/me/quit-group/quit-group.component';
import { CreateUserComponent } from 'app/shared/create-user-wizard/create-user.component';
import { DataCyDirective } from 'app/shared/directives/data-cy.directive';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { SetTitlePipe } from 'app/shared/pipes/set-title.pipe';

/**
 * Standalone page component (selector `cbc-roles`) displayed in the current
 * user's personal area ("me" section).
 *
 * It renders an overview of the roles the logged-in user holds across the
 * platform: the list of interest-group memberships together with their
 * associated profiles/roles, and the categories the user is administrator of.
 * From this view the user can refresh their memberships, trigger the quit-group
 * confirmation flow, and (for privileged users) open the create-user wizard.
 *
 * Key collaborators:
 * - {@link LoginService} to resolve the current user, username and admin flags.
 * - {@link UserService} (generated CIRCABC API client) to fetch the user's
 *   memberships and categories.
 * - {@link I18nPipe} to resolve localized names/titles of groups and profiles.
 *
 * Child components used by the template include the quit-group confirmation
 * ({@link QuitGroupComponent}) and the create-user wizard
 * ({@link CreateUserComponent}).
 */
@Component({
  selector: 'cbc-roles',
  templateUrl: './roles.component.html',
  styleUrl: './roles.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    HorizontalLoaderComponent,
    DataCyDirective,
    CreateUserComponent,
    RouterLink,
    QuitGroupComponent,
    SetTitlePipe,
    TranslocoModule,
  ],
})
export class RolesComponent {
  /** Service used to access the current user, username and admin properties. */
  private readonly loginService = inject(LoginService);
  /** Generated CIRCABC API client used to fetch the user's memberships and categories. */
  private readonly userService = inject(UserService);
  /** Pipe used to resolve the localized name/title of groups and profiles. */
  private readonly i18nPipe = inject(I18nPipe);

  /** The current authenticated user, resolved once from {@link LoginService}. */
  private readonly user: User = this.loginService.getUser();

  /**
   * Resource loading the interest-group memberships (with their profiles/roles)
   * for the current user. Never ends in the error state: failures are logged
   * and resolved to an empty list, matching the absence of a dedicated error UI.
   */
  private readonly membershipsResource = resource({
    loader: async () => {
      try {
        return await this.userService.getUserMembershipAsync({
          userId: this.getUserId(),
        });
      } catch (e) {
        console.error(e);
        return [];
      }
    },
    defaultValue: [],
  });

  /**
   * Resource loading the categories for which the current user has a role
   * (e.g. administration). Never ends in the error state: failures are logged
   * and resolved to an empty list, matching the absence of a dedicated error UI.
   */
  private readonly categoriesResource = resource({
    loader: async () => {
      try {
        return await this.userService.getUserCategoriesAsync({
          userId: this.getUserId(),
        });
      } catch (e) {
        console.error(e);
        return [];
      }
    },
    defaultValue: [],
  });

  /** Interest-group memberships (with their profiles/roles) for the current user. */
  public readonly memberships = this.membershipsResource.value;
  /** Categories for which the current user has a role (e.g. administration). */
  public readonly categories = this.categoriesResource.value;
  /** True while the memberships list is being loaded from the backend. */
  public readonly loadingMemberships = this.membershipsResource.isLoading;
  /** True while the categories list is being loaded from the backend. */
  public readonly loadingCategories = this.categoriesResource.isLoading;
  /** Controls the visibility of the quit-group confirmation dialog. */
  public displayQuitGroup = false;
  /** The interest group currently selected for the quit-group confirmation flow. */
  public selectedGroup!: InterestGroup;
  /** The username (login) of the currently authenticated user. */
  public readonly username: string = this.loginService.getCurrentUsername();
  /** True when the current user is a platform administrator. */
  public readonly isAdmin: boolean = this.user.properties?.isAdmin === 'true';
  /** True when the current user is a CIRCABC administrator. */
  public readonly isCicabcAdmin: boolean =
    this.user.properties?.isCircabcAdmin === 'true';
  /** Controls the visibility of the create-user wizard. */
  public showWizard = false;

  /**
   * Resolves the identifier of the current user.
   *
   * @returns The current authenticated user's username.
   */
  private getUserId(): string {
    return this.loginService.getCurrentUsername();
  }

  /**
   * Resolves a human-readable label for an interest group or profile.
   *
   * Prefers the localized title (via {@link I18nPipe}); if no localized title is
   * available, falls back to the item's `name`.
   *
   * @param item - The interest group or profile to label, or `undefined`.
   * @returns The localized title, the name as a fallback, or an empty string
   * when `item` is `undefined` or has neither a title nor a name.
   */
  getNameOrTitle(item: InterestGroup | Profile | undefined): string {
    if (item === undefined) {
      return '';
    }

    let result = '';

    if (item.title && Object.keys(item.title).length > 0) {
      result = this.i18nPipe.transform(item.title);
    }

    if (result === '' && item.name) {
      result = item.name;
    }

    return result;
  }

  /**
   * Opens the quit-group confirmation dialog for the given group.
   *
   * Sets {@link selectedGroup} and shows the confirmation only when a group is
   * provided; a `undefined` group is ignored.
   *
   * @param group - The interest group the user intends to quit, or `undefined`.
   */
  public showConfirmation(group: InterestGroup | undefined) {
    if (group) {
      this.displayQuitGroup = true;
      this.selectedGroup = group;
    }
  }

  /**
   * Reloads the current user's memberships, e.g. after the user quits a group.
   */
  public refresh() {
    this.membershipsResource.reload();
  }

  /**
   * Handler invoked when the create-user wizard is closed; hides the wizard.
   *
   * @param _result - The result emitted by the wizard (currently unused).
   * @returns A promise that resolves once the wizard has been hidden.
   */
  public async createUserWizardClosed(_result: ActionEmitterResult) {
    this.showWizard = false;
  }
}
