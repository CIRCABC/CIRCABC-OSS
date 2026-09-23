import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  input,
  resource,
} from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';
import {
  Category,
  InterestGroupProfile,
  UserService,
} from 'app/core/generated/circabc';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { CategoryBoxComponent } from './category-box/category-box.component';
import { GroupBoxComponent } from './group-box/group-box.component';

/**
 * Standalone Angular component that renders a focused overview of a single
 * user's platform memberships.
 *
 * For the user identified by {@link FocusedUserMembershipBoxComponent.userId},
 * it fetches and displays two related sets of data:
 * - the interest groups the user belongs to (via nested
 *   {@link GroupBoxComponent} instances), and
 * - the categories the user is associated with (via nested
 *   {@link CategoryBoxComponent} instances).
 *
 * While data is being retrieved it shows loading indicators
 * ({@link HorizontalLoaderComponent} / {@link SpinnerComponent}) and it exposes
 * an error state when the underlying requests fail. Membership and category
 * data are (re)loaded on initialisation and whenever the target user changes.
 *
 * Key collaborator: {@link UserService} (generated CIRCABC API client), used to
 * fetch the user's memberships and categories.
 */
@Component({
  selector: 'cbc-focused-user-membership-box',
  templateUrl: './focused-user-membership-box.component.html',
  styleUrl: './focused-user-membership-box.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    HorizontalLoaderComponent,
    SpinnerComponent,
    CategoryBoxComponent,
    GroupBoxComponent,
    TranslocoModule,
  ],
})
export class FocusedUserMembershipBoxComponent {
  /** Generated CIRCABC API client used to load the user's memberships and categories. */
  private readonly userService = inject(UserService);

  /**
   * Required input: the identifier of the user whose memberships and categories
   * should be displayed. Changing this value triggers a reload of the data.
   */
  readonly userId = input.required<string>();

  /**
   * Loads the current {@link userId}'s memberships and categories from the
   * backend. Idle while no user id is available. Any request failure is
   * captured so the resource never enters the error state (there is no
   * dedicated error UI beyond the shared "nothing to display" message).
   */
  private readonly membershipResource = resource({
    params: () => this.userId() || undefined,
    loader: async ({ params: userId }) => {
      try {
        const memberships = await this.userService.getUserMembershipAsync({
          userId,
        });
        const categories = await this.userService.getUserCategoriesAsync({
          userId,
        });
        return { memberships, categories, error: false };
      } catch (error) {
        console.error(error);
        return { memberships: [], categories: [], error: true };
      }
    },
  });

  /** Whether a data-fetching operation is currently in progress. */
  public readonly loading = this.membershipResource.isLoading;
  /** Whether the last data-fetching operation failed. */
  public readonly error = computed(
    () => this.membershipResource.value()?.error ?? false
  );
  /** The interest group profiles the target user is a member of. */
  public readonly memberships = computed<InterestGroupProfile[]>(
    () => this.membershipResource.value()?.memberships ?? []
  );
  /** The categories the target user is associated with. */
  public readonly categories = computed<Category[]>(
    () => this.membershipResource.value()?.categories ?? []
  );

  /**
   * Re-fetches the current {@link userId}'s memberships and categories,
   * e.g. after a child component uninvites a user from a group or category.
   */
  public mustRefresh() {
    this.membershipResource.reload();
  }
}
