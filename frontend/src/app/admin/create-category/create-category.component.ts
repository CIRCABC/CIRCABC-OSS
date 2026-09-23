import {
  ChangeDetectionStrategy,
  Component,
  inject,
  model,
  signal,
} from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { TranslocoModule } from '@jsverse/transloco';
import {
  CategoryService,
  HeaderService,
  User,
  UserService,
} from 'app/core/generated/circabc';
import { ControlMessageComponent } from 'app/shared/control-message/control-message.component';
import { DataCyDirective } from 'app/shared/directives/data-cy.directive';
import { MultilingualInputComponent } from 'app/shared/input/multilingual-input.component';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';

/**
 * Modal wizard component used by platform administrators to create a new
 * category.
 *
 * The component renders a multi-step modal dialog that lets the user:
 * 1. Enter the category details (name, optional multilingual title and the
 *    parent header the category belongs to).
 * 2. Search for existing users and designate one or more of them as the
 *    initial administrators of the new category.
 *
 * On completion it persists the category through {@link CategoryService} and
 * assigns the selected administrators. It collaborates with
 * {@link HeaderService} to list the available headers and with
 * {@link UserService} to search for candidate administrators.
 *
 * Selector: `cbc-create-category`.
 */
@Component({
  selector: 'cbc-create-category',
  templateUrl: './create-category.component.html',
  styleUrl: './create-category.component.scss',
  preserveWhitespaces: true,
  imports: [
    ModalComponent,
    DataCyDirective,
    ReactiveFormsModule,
    ControlMessageComponent,
    MultilingualInputComponent,
    SpinnerComponent,
    TranslocoModule,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CreateCategoryComponent {
  /** Reactive forms builder used to construct the wizard's form groups. */
  private readonly fb = inject(FormBuilder);
  /** API client used to retrieve the list of available headers. */
  private readonly headerService = inject(HeaderService);
  /** API client used to create the category and assign its administrators. */
  private readonly categoryService = inject(CategoryService);
  /** API client used to search for candidate administrator users. */
  private readonly userService = inject(UserService);

  /**
   * Two-way bindable model controlling the visibility of the modal dialog.
   * Set to `true` to open the wizard and `false` to close it.
   */
  public showModal = model<boolean>(false);

  /** Identifier of the currently displayed wizard step (e.g. `'details'`). */
  public step = signal<string>('details');

  /** Users that will be granted administrator rights on the new category. */
  public futureAdmins = signal<User[]>([]);

  /** Users returned by the latest search, selectable as administrators. */
  public availableUsers = signal<User[]>([]);

  /** Whether a user search request is currently in progress. */
  public searchingUsers = signal(false);

  /** Whether the category creation request is currently in progress. */
  public processing = signal(false);

  /** Reactive resource providing the full list of available headers. */
  readonly headersResource = rxResource({
    stream: () => this.headerService.getHeaders(),
  });

  /** Reactive form backing the category details step (name, title, header). */
  public categoryForm: FormGroup = this.fb.group({
    name: ['', Validators.required],
    title: [''],
    header: ['', Validators.required],
  });

  /** Reactive form backing the user search / administrator selection step. */
  public addUserForm: FormGroup = this.fb.group({
    name: [''],
    possibleUsers: [''],
  });

  /**
   * Resets both forms and clears the selected/available users, then closes
   * the modal. Used to abort the wizard or after a successful creation.
   */
  public cancel(): void {
    this.addUserForm.reset();
    this.categoryForm.reset({
      name: '',
      title: '',
      header: '',
    });
    this.availableUsers.set([]);
    this.futureAdmins.set([]);
    this.showModal.set(false);
  }

  /**
   * Convenience accessor for the category `name` form control.
   *
   * @returns The `name` control of the category form.
   */
  get nameControl(): AbstractControl {
    return this.categoryForm.controls.name;
  }

  /**
   * Convenience accessor for the category `title` form control.
   *
   * @returns The `title` control of the category form.
   */
  get titleControl(): AbstractControl {
    return this.categoryForm.controls.title;
  }

  /**
   * Convenience accessor for the category `header` form control.
   *
   * @returns The `header` control of the category form.
   */
  get headerControl(): AbstractControl {
    return this.categoryForm.controls.header;
  }

  /**
   * Triggers a user search based on the current value of the search field.
   * Clears any previous selection and repopulates {@link availableUsers}
   * when the search term is not empty.
   *
   * @returns A promise that resolves once the search has completed.
   */
  public async searchUsers(): Promise<void> {
    if (this.addUserForm.controls.name.value !== '') {
      this.addUserForm.controls.possibleUsers.setValue('');
      await this.populateUsers(this.addUserForm.controls.name.value);
    }
  }

  /**
   * Queries the {@link UserService} for users matching the given term and
   * replaces {@link availableUsers} with the results, toggling
   * {@link searchingUsers} around the request.
   *
   * @param query The search term to match users against.
   * @returns A promise that resolves once the users have been loaded.
   */
  public async populateUsers(query: string): Promise<void> {
    this.searchingUsers.set(true);
    const res = await this.userService.getUsersAsync({ query, filter: false });
    this.availableUsers.set(res);
    this.searchingUsers.set(false);
  }

  /**
   * Clears the current search results and resets the search and selection
   * controls of the user form.
   */
  public resetForm(): void {
    this.availableUsers.set([]);
    this.addUserForm.controls.name.setValue('');
    this.addUserForm.controls.possibleUsers.setValue('');
  }

  /**
   * Adds the users currently selected in the search results to
   * {@link futureAdmins}, skipping any that are already present to avoid
   * duplicates.
   */
  public selectUsers(): void {
    const selectedIds: string[] = this.addUserForm.controls.possibleUsers.value;
    const membersTmp = this.availableUsers().filter((user) =>
      selectedIds.includes(user.userId ?? '')
    );

    const currentAdmins = this.futureAdmins();
    const newAdmins = membersTmp.filter(
      (candidate) =>
        !currentAdmins.some((admin) => admin.userId === candidate.userId)
    );

    this.futureAdmins.set([...currentAdmins, ...newAdmins]);
  }

  /**
   * Indicates whether at least one user is currently selected in the search
   * results.
   *
   * @returns `true` if a user is selected, otherwise `false`.
   */
  hasSelectedUser(): boolean {
    return this.addUserForm.controls.possibleUsers.value !== '';
  }

  /**
   * Removes the given user from the list of future administrators.
   *
   * @param m The user to remove from {@link futureAdmins}.
   */
  public removeFromFutureAdmin(m: User): void {
    this.futureAdmins.update((admins) => admins.filter((admin) => admin !== m));
  }

  /**
   * Determines whether the wizard has enough valid input to create a
   * category, i.e. the details form is valid and at least one future
   * administrator has been selected.
   *
   * @returns `true` when the category can be created, otherwise `false`.
   */
  public isWizardOk(): boolean {
    return this.categoryForm.valid && this.futureAdmins().length > 0;
  }

  /**
   * Creates the category from the current form values and assigns the
   * selected future administrators to it. Toggles {@link processing} around
   * the request and, on success, resets and closes the wizard via
   * {@link cancel}. Errors are caught and logged rather than propagated.
   *
   * @returns A promise that resolves once the creation flow has completed.
   */
  public async createCategory(): Promise<void> {
    this.processing.set(true);
    try {
      const category = this.categoryForm.value;
      const newCategory = await this.categoryService.postCategoryAsync({
        id: this.categoryForm.value.header,
        category,
      });
      if (newCategory?.id) {
        const userIds: string[] = this.futureAdmins()
          .map((user) => user.userId)
          .filter((id): id is string => !!id);

        await this.categoryService.postCategoryAdministartorsAsync({
          id: newCategory.id,
          requestBody: userIds,
        });
        this.cancel();
      }
    } catch (error) {
      console.error(error);
      console.error('Error during the creation of the category');
    }
    this.processing.set(false);
  }
}
