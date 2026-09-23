import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  output,
  resource,
  signal,
} from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { TranslocoModule } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { CategoryService, User, UserService } from 'app/core/generated/circabc';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { environment } from 'environments/environment';

/**
 * Modal component that lets an operator search for users and add them as
 * administrators of a given category.
 *
 * Rendered inside a {@link ModalComponent}, it exposes a reactive form to
 * search the directory ({@link UserService}), build up a list of future
 * administrators, and submit them to the backend via {@link CategoryService}.
 * A {@link SpinnerComponent} is shown while user searches are in flight.
 *
 * The component is fully controlled by its parent: visibility is driven by the
 * {@link AddCategoryAdministratorComponent.showModal} input, and the outcome of
 * the interaction is reported back through the
 * {@link AddCategoryAdministratorComponent.modalHide} output.
 */
@Component({
  selector: 'cbc-add-category-administrator',
  templateUrl: './add-category-administrator.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ModalComponent,
    ReactiveFormsModule,
    SpinnerComponent,
    TranslocoModule,
  ],
})
export class AddCategoryAdministratorComponent {
  /** Generated API client used to read and update the category administrators. */
  private readonly categoryService = inject(CategoryService);
  /** Reactive forms factory used to build {@link addUserForm}. */
  private readonly fb = inject(FormBuilder);
  /** Generated API client used to search for candidate users. */
  private readonly userService = inject(UserService);

  /**
   * Input controlling whether the modal is displayed. The value is fully
   * owned by the parent component (migrated from a mutable field to a
   * required signal input).
   */
  readonly showModal = input.required<boolean>();
  /** Input holding the identifier of the category being administered. */
  readonly categoryId = input.required<string>();
  /**
   * Emitted when the modal closes, carrying the result of the interaction
   * (success, failure or cancellation) so the parent can react accordingly.
   */
  public readonly modalHide = output<ActionEmitterResult>();

  /** Whether an add-administrators request is currently in progress. */
  public readonly processing = signal(false);
  /** Reactive form backing the search field, results selection and filter flag. */
  public readonly addUserForm: FormGroup = this.fb.group({
    name: [''],
    possibleUsers: [''],
    filter: [true],
  });
  /** Users returned by the most recent directory search. */
  public readonly availableUsers = signal<User[]>([]);
  /**
   * Reactive resource loading the current list of category administrators for
   * the {@link categoryId} input. The loader stays IDLE while no category id is
   * provided and re-runs whenever the id changes. Failures are swallowed to a
   * safe empty list (matching the old behaviour of showing no admins) so the
   * resource never ends up in the error state, which would make reads of
   * {@link existingAdmins} throw.
   */
  private readonly existingAdminsResource = resource({
    params: () => this.categoryId() || undefined,
    loader: async ({ params: id }) => {
      try {
        return await this.categoryService.getCategoryAdministratorsAsync({
          id,
        });
      } catch (error) {
        console.error(error);
        return [];
      }
    },
    defaultValue: [],
  });
  /** Users who are already administrators of the category. */
  public readonly existingAdmins = this.existingAdminsResource.value;
  /** Users staged to be added as administrators once the form is submitted. */
  public readonly futureMembers = signal<User[]>([]);
  /** Whether a directory search is currently running (drives the spinner). */
  public readonly searchingUsers = signal(false);
  /** Whether the application is running the open-source (OSS) release. */
  public readonly isOSS = environment.circabcRelease === 'oss';

  /**
   * Adds all staged {@link futureMembers} as administrators of the current
   * category. On success the form is reset and the staged list is cleared; on
   * failure the error is logged. In both cases {@link modalHide} is emitted
   * with the corresponding {@link ActionResult}.
   *
   * @returns A promise that resolves once the request completes and the
   * result has been emitted.
   */
  public async addAdmins() {
    this.processing.set(true);
    const res: ActionEmitterResult = { type: ActionType.INVITE_CATEGORY_ADMIN };

    try {
      const userIds: string[] = [];
      this.futureMembers().forEach((user: User) => {
        if (user.userId) {
          userIds.push(user.userId);
        }
      });

      await this.categoryService.postCategoryAdministartorsAsync({
        id: this.categoryId(),
        requestBody: userIds,
      });
      res.result = ActionResult.SUCCEED;
      this.resetForm();
      this.futureMembers.set([]);
    } catch (error) {
      console.error(error);
      console.error('impossible to add the users as category admins');
      res.result = ActionResult.FAILED;
    }
    this.processing.set(false);
    this.modalHide.emit(res);
  }

  /**
   * Cancels the interaction: resets the form, clears the staged members and
   * emits {@link modalHide} with an {@link ActionResult.CANCELED} result.
   */
  public cancel() {
    this.resetForm();
    this.futureMembers.set([]);

    const result: ActionEmitterResult = {
      result: ActionResult.CANCELED,
      type: ActionType.INVITE_CATEGORY_ADMIN,
    };

    this.modalHide.emit(result);
  }

  /**
   * Triggers a directory search using the current value of the name field.
   * When the field is non-empty it clears the current selection and delegates
   * to {@link populateUsers}.
   *
   * @returns A promise that resolves once the search (if any) completes.
   */
  public async searchUsers() {
    if (this.addUserForm.controls.name.value !== '') {
      this.addUserForm.controls.possibleUsers.setValue('');
      await this.populateUsers(
        this.addUserForm.controls.name.value,
        this.addUserForm.controls.filter.value
      );
    }
  }

  /**
   * Queries the user directory and refreshes {@link availableUsers} with the
   * results, toggling {@link searchingUsers} around the call.
   *
   * @param query The search term to look users up by.
   * @param filter Whether the backend should apply its default user filter.
   * @returns A promise that resolves once the results have been loaded.
   */
  public async populateUsers(query: string, filter: boolean) {
    this.searchingUsers.set(true);
    const res = await this.userService.getUsersAsync({ query, filter });
    this.availableUsers.set([...res]);
    this.searchingUsers.set(false);
  }

  /**
   * Clears the search results and resets the name and selection form controls
   * to their empty state.
   */
  public resetForm(): void {
    this.availableUsers.set([]);
    this.addUserForm.controls.name.setValue('');
    this.addUserForm.controls.possibleUsers.setValue('');
  }

  /**
   * Determines whether the given user is already an administrator of the
   * category.
   *
   * @param user The user to check against {@link existingAdmins}.
   * @returns `true` if the user is already a category administrator,
   * otherwise `false`.
   */
  public isAlreadyAdmin(user: User): boolean {
    for (const member of this.existingAdmins()) {
      if (member && member.userId === user.userId) {
        return true;
      }
    }
    return false;
  }

  /**
   * Moves the users currently selected in the form (from
   * {@link availableUsers}) into {@link futureMembers}, avoiding duplicates.
   */
  public selectUsers(): void {
    const membersTmp: User[] = [];
    this.addUserForm.controls.possibleUsers.value.forEach((userId: string) => {
      const memberTmp = this.availableUsers().find(
        (user) => user.userId === userId
      );
      if (memberTmp) {
        membersTmp.push(memberTmp);
      }
    });

    const currentMembers = this.futureMembers();
    this.futureMembers.set(
      currentMembers.concat(
        membersTmp.filter((memberTmp) => {
          return !currentMembers.some((member) => {
            return member && memberTmp && member.userId === memberTmp.userId;
          });
        })
      )
    );
  }

  /**
   * Indicates whether at least one user is currently selected in the form.
   *
   * @returns `true` when the selection control holds a non-empty value,
   * otherwise `false`.
   */
  hasSelectedUser(): boolean {
    return this.addUserForm.controls.possibleUsers.value !== '';
  }

  /**
   * Removes the given user from the staged {@link futureMembers} list.
   *
   * @param m The user to remove from the future administrators list.
   */
  public removeFromFutureMember(m: User): void {
    this.futureMembers.set(
      this.futureMembers().filter((member) => member !== m)
    );
  }
}
