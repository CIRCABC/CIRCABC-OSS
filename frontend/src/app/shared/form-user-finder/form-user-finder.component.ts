import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  OnInit,
  signal,
} from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ActivatedRoute } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import {
  InterestGroup,
  InterestGroupService,
  User,
  UserService,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';

/**
 * Reusable form control that lets the user search for and select a single
 * {@link User}, wiring the selection back into a parent reactive form.
 *
 * The component renders a search input together with a results list. As the
 * user types a query and triggers a search, matching users are fetched from
 * the backend via {@link UserService} and displayed for selection. The chosen
 * user is written into the parent form's `creatorUser` control (supplied via
 * the {@link searchAdvancedForm} input), and clearing that control resets the
 * finder. For users belonging to the `external` domain, the search query is
 * additionally validated as an email address before a lookup is performed.
 *
 * Key collaborators:
 * - {@link UserService} — performs the user search lookups.
 * - {@link InterestGroupService} — resolves the current interest group from
 *   the `id` route parameter.
 * - {@link LoginService} — determines whether the current user is external.
 * - {@link FormBuilder} — builds the internal search form.
 * - {@link ActivatedRoute} — provides the route parameters.
 */
@Component({
  selector: 'cbc-form-user-finder',
  templateUrl: './form-user-finder.component.html',
  styleUrl: './form-user-finder.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ReactiveFormsModule,
    SpinnerComponent,
    MatTooltipModule,
    TranslocoModule,
  ],
})
export class FormUserFinderComponent implements OnInit {
  /** Route used to read the interest group `id` parameter. */
  private readonly route = inject(ActivatedRoute);
  /** Service used to resolve the current interest group. */
  private readonly groupsService = inject(InterestGroupService);
  /** Factory used to build the internal search form. */
  private readonly fb = inject(FormBuilder);
  /** Service used to search for users matching a query. */
  private readonly userService = inject(UserService);
  /** Service exposing the currently authenticated user. */
  private readonly loginService = inject(LoginService);

  /**
   * Translation key for the label/placeholder shown on the search field.
   * Defaults to `'label.search.user'`.
   */
  public readonly label = input('label.search.user');

  /**
   * Required parent reactive form that owns the `creatorUser` control. The
   * selected user is written into that control, and the component reacts to
   * external changes of the form (e.g. resetting when `creatorUser` is
   * cleared).
   */
  public readonly searchAdvancedForm = input.required<FormGroup>();

  /** The user currently selected in the finder, or `undefined` if none. */
  public readonly userSelected = signal<User | undefined>(undefined);
  /** Internal reactive form holding the search query and candidate list. */
  public addUserForm!: FormGroup;
  /** Users returned by the most recent search that can be selected. */
  public readonly availableUsers = signal<User[]>([]);
  /** Whether a user search request is currently in progress. */
  public readonly searchingUsers = signal(false);
  /** Whether the currently authenticated user belongs to the external domain. */
  public isExternalUser = true;
  /** The interest group resolved from the current route, when available. */
  public currentGroup!: InterestGroup;
  /** Whether the finder is rendered in read-only mode. */
  public isReadOnly = false;
  /** Whether the last search returned no matching users. */
  public readonly noAvailableUsers = signal(false);
  /** Whether the last search failed due to insufficient access rights. */
  public readonly noAccess = signal(false);

  /**
   * Angular lifecycle hook. Builds the internal search form, subscribes to
   * changes of the parent form to keep the selected user in sync (resetting
   * when `creatorUser` is cleared), determines whether the current user is
   * external, and resolves the current interest group from the route `id`
   * parameter.
   */
  public ngOnInit() {
    this.buildForm();
    this.searchAdvancedForm().valueChanges.subscribe((dataForm) => {
      this.userSelected.set(dataForm.creatorUser);
      if (dataForm.creatorUser === undefined) {
        this.resetForm();
      }
    });
    this.isExternalUser =
      this.loginService.getUser().properties?.domain === 'external';

    this.route.params.subscribe(async (params) => {
      if (params.id)
        this.currentGroup = await this.groupsService.getInterestGroupAsync({
          id: params.id,
        });
    });
  }

  /**
   * Initializes {@link addUserForm} with the search query (`name`) and the
   * currently highlighted candidate (`possibleUsers`) controls.
   */
  public buildForm(): void {
    this.addUserForm = this.fb.group(
      {
        name: [''],
        possibleUsers: [''],
      },
      {
        updateOn: 'change',
      }
    );
  }

  /**
   * Triggers a user search based on the current query. For external users the
   * query must be a valid email address; otherwise the search is skipped.
   * Empty queries are ignored.
   *
   * @returns A promise that resolves once the search (if any) has completed.
   */
  public async searchUsers() {
    let isValid = true;
    if (this.isExternalUser && !this.isValidEmail()) {
      isValid = false;
    }
    if (isValid) {
      if (this.addUserForm.controls.name.value !== '') {
        await this.populateUsers(this.addUserForm.controls.name.value);
      }
    }
  }

  /**
   * Fetches users matching the given query and populates
   * {@link availableUsers}. Manages the {@link searchingUsers},
   * {@link noAvailableUsers} and {@link noAccess} flags accordingly. Access
   * errors are caught and surfaced via {@link noAccess} rather than being
   * rethrown.
   *
   * @param query The search string to look users up by.
   * @returns A promise that resolves once the results have been populated.
   */
  public async populateUsers(query: string) {
    this.searchingUsers.set(true);
    this.availableUsers.set([]);
    this.noAvailableUsers.set(false);
    try {
      const res = await this.userService.getUsersAsync({ query });
      this.availableUsers.set([...res]);
    } catch (error) {
      console.error(error);
      this.noAccess.set(true);
    }
    if (this.availableUsers().length === 0) {
      this.noAvailableUsers.set(true);
    }
    this.searchingUsers.set(false);
  }

  /**
   * Clears the search results and resets the internal form controls and the
   * {@link noAvailableUsers} / {@link noAccess} flags to their initial state.
   */
  public resetForm(): void {
    this.availableUsers.set([]);
    this.addUserForm.controls.name.setValue('');
    this.addUserForm.controls.possibleUsers.setValue('');
    this.noAvailableUsers.set(false);
    this.noAccess.set(false);
  }

  /**
   * Selects the given user, clears the candidate control and writes the
   * selection into the parent form's `creatorUser` control.
   *
   * @param user The user chosen from the search results.
   * @returns A promise that resolves once the selection has been applied.
   */
  public async selectUser(user: User) {
    this.userSelected.set(user);
    this.addUserForm.controls.possibleUsers.setValue('');
    this.searchAdvancedForm().controls.creatorUser.setValue(user);
  }

  /**
   * Clears the current selection, nulls out the parent form's `creatorUser`
   * control and resets the finder to its initial state.
   */
  public removeUser(): void {
    this.userSelected.set(undefined);
    this.searchAdvancedForm().controls.creatorUser.setValue(null);
    this.resetForm();
  }

  /**
   * Indicates whether the search query is currently empty.
   *
   * @returns `true` if the form exists and its `name` value is empty or
   * `null`; otherwise `false`.
   */
  public isSearchEmpty() {
    if (this.addUserForm) {
      return (
        this.addUserForm.value.name === '' ||
        this.addUserForm.value.name === null
      );
    }

    return false;
  }

  /**
   * Validates the current search query as an email address.
   *
   * @returns `true` if the form exists and its `name` value matches a basic
   * email pattern; otherwise `false`.
   */
  public isValidEmail() {
    if (this.addUserForm) {
      const emailRegex: RegExp = /^[^\s@]+@[^\s@]+\.[^\s@]+$/; // NOSONAR - no backtracking, literal anchors separate segments
      const email = this.addUserForm.value.name;
      return emailRegex.test(email);
    }
    return false;
  }
}
