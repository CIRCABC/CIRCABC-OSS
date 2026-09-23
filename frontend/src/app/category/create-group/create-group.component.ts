import {
  ChangeDetectionStrategy,
  Component,
  inject,
  model,
  OnInit,
  output,
  signal,
} from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result/index';
import {
  CategoryService,
  InterestGroupPostModel,
  User,
  UserService,
} from 'app/core/generated/circabc';
import { fileNameValidator, titleValidator } from 'app/core/validation.service';
import { ControlMessageComponent } from 'app/shared/control-message/control-message.component';
import { DataCyDirective } from 'app/shared/directives/data-cy.directive';
import { MultilingualInputComponent } from 'app/shared/input/multilingual-input.component';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';

/**
 * Modal component that renders a multi-step wizard for creating a new interest
 * group within a category.
 *
 * The wizard is displayed inside a {@link ModalComponent} and guides the user
 * through three sequential steps, each backed by its own reactive form:
 * 1. Group details (name, title, description, contact).
 * 2. Group leaders (search for and select users to become leaders/members).
 * 3. Notification (optional welcome/notification text sent on creation).
 *
 * On completion it posts the new group to the backend via
 * {@link CategoryService} and emits the outcome through {@link modalClosed}.
 * It relies on {@link UserService} to search for candidate users and reads the
 * target category identifier from the active route.
 */
@Component({
  selector: 'cbc-create-group',
  templateUrl: './create-group.component.html',
  styleUrl: './create-group.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ModalComponent,
    ReactiveFormsModule,
    DataCyDirective,
    ControlMessageComponent,
    MultilingualInputComponent,
    SpinnerComponent,
    TranslocoModule,
  ],
})
export class CreateGroupComponent implements OnInit {
  /** Reactive forms builder used to construct the wizard step forms. */
  private readonly fb = inject(FormBuilder);
  /** Active route, used to resolve the target category id from route params. */
  private readonly route = inject(ActivatedRoute);
  /** Backend service used to search for candidate users. */
  private readonly userService = inject(UserService);
  /** Backend service used to create the interest group under the category. */
  private readonly categoryService = inject(CategoryService);

  /**
   * Two-way bindable model controlling the visibility of the wizard modal.
   * `true` shows the modal, `false` hides it.
   */
  showModal = model<boolean>(false);
  /**
   * Emits once the modal is closed, carrying the outcome of the operation
   * (success, failure, "already exists" or cancellation).
   */
  readonly modalClosed = output<ActionEmitterResult>();

  /** Whether the "group details" wizard step is currently visible. */
  public readonly showDetailsForm = signal(true);
  /** Whether the "group leaders" wizard step is currently visible. */
  public readonly showLeadersForm = signal(false);
  /** Whether the "notification" wizard step is currently visible. */
  public readonly showNotificationForm = signal(false);
  /** `true` while a user search request is in flight (drives the spinner). */
  public readonly searchingUsers = signal(false);
  /** `true` while the group creation request is being processed. */
  public readonly processing = signal(false);

  /** Reactive form holding the group details (name, title, description, contact). */
  public groupDetailsForm!: FormGroup;
  /** Reactive form holding the leader search term and selected candidate users. */
  public groupLeadersForm!: FormGroup;
  /** Reactive form holding the notification flag and notification text. */
  public groupNotificationForm!: FormGroup;

  /** Identifier of the category under which the new group will be created. */
  public categoryId!: string;

  /** Users returned by the latest search, available for selection as leaders. */
  public readonly availableUsers = signal<User[]>([]);
  /** Users selected to become the leaders/members of the new group. */
  public readonly futureMembers = signal<User[]>([]);

  /**
   * Lifecycle hook that resolves the target category id from the route and
   * initializes the three reactive forms used by the wizard steps.
   */
  ngOnInit() {
    this.route.params.subscribe((params) => {
      this.categoryId = params.id;
    });

    this.groupDetailsForm = this.fb.group(
      {
        name: ['', [Validators.required, fileNameValidator]],
        title: [{}, [Validators.required, titleValidator]],
        description: [{}, Validators.required],
        contact: [{}, Validators.required],
      },
      {
        updateOn: 'change',
      }
    );
    this.groupLeadersForm = this.fb.group(
      {
        search: [],
        possibleUsers: [],
      },
      {
        updateOn: 'change',
      }
    );
    this.groupNotificationForm = this.fb.group(
      {
        notify: [false],
        notificationText: [{}, Validators.required],
      },
      {
        updateOn: 'change',
      }
    );
  }

  /**
   * Builds the {@link InterestGroupPostModel} from the wizard forms and creates
   * the interest group under the current category.
   *
   * On success it resets the wizard; on failure it reports the error type
   * (including a dedicated "already exists" outcome when the backend indicates
   * a name clash). The result is always emitted through {@link modalClosed} and
   * {@link processing} is cleared when finished. Errors from the backend call
   * are caught internally and reflected in the emitted result rather than being
   * re-thrown.
   *
   * @returns A promise that resolves once creation has completed and the
   * outcome has been emitted.
   */
  async createGroup() {
    this.processing.set(true);
    const result: ActionEmitterResult = {};
    result.type = ActionType.CREATE_INTEREST_GROUP;

    if (this.categoryId) {
      try {
        const newGroup: InterestGroupPostModel = {
          ...this.groupDetailsForm.value,
        };
        newGroup.leaders = [];
        newGroup.notify = this.groupNotificationForm.value.notify;
        newGroup.notifyText = this.groupNotificationForm.value.notificationText;

        for (const leader of this.futureMembers()) {
          if (leader.userId) {
            newGroup.leaders.push(leader.userId);
          }
        }
        const group = await this.categoryService.postInterestGroupAsync({
          id: this.categoryId,
          interestGroupPostModel: newGroup,
        });
        result.type = ActionType.CREATE_INTEREST_GROUP;
        result.result = ActionResult.SUCCEED;
        result.node = { id: group.id };

        this.reset();
      } catch (error) {
        if (error.error.message.includes(' already exists.')) {
          result.type = ActionType.CREATE_INTEREST_GROUP_EXISTS;
        }
        result.result = ActionResult.FAILED;
      }
    }
    this.modalClosed.emit(result);
    this.processing.set(false);
  }

  /** Resets the leaders search form, clearing the search term and selection. */
  resetSearchForm() {
    this.groupLeadersForm.reset();
  }

  /**
   * Cancels the wizard, resetting all forms and emitting a cancellation result
   * through {@link modalClosed}.
   */
  cancel() {
    const res: ActionEmitterResult = {};
    res.result = ActionResult.CANCELED;
    res.type = ActionType.CREATE_INTEREST_GROUP;

    this.reset();

    this.modalClosed.emit(res);
  }

  /** Switches the wizard to the "group details" step. */
  setDetailsForm() {
    this.showDetailsForm.set(true);
    this.showLeadersForm.set(false);
    this.showNotificationForm.set(false);
  }

  /** Switches the wizard to the "group leaders" step. */
  setLeadersForm() {
    this.showDetailsForm.set(false);
    this.showLeadersForm.set(true);
    this.showNotificationForm.set(false);
  }

  /** Switches the wizard to the "notification" step. */
  setNotificationForm() {
    this.showDetailsForm.set(false);
    this.showLeadersForm.set(false);
    this.showNotificationForm.set(true);
  }

  /**
   * Searches for users matching the current search term (unless it is empty)
   * and stores the matches in {@link availableUsers}. Toggles
   * {@link searchingUsers} around the asynchronous request.
   *
   * @returns A promise that resolves once the search has completed.
   */
  async searchUsers() {
    if (!this.searchExpressionEmpty()) {
      this.searchingUsers.set(true);
      this.availableUsers.set(
        await this.userService.getUsersAsync({
          query: this.groupLeadersForm.controls.search.value,
          filter: false,
        })
      );
      this.searchingUsers.set(false);
    }
  }

  /**
   * Indicates whether the leaders search term is empty.
   *
   * @returns `true` when the search control value is an empty string or `null`.
   */
  public searchExpressionEmpty(): boolean {
    return (
      this.groupLeadersForm.controls.search.value === '' ||
      this.groupLeadersForm.controls.search.value === null
    );
  }

  /**
   * Clears the current search results and resets the search and selection
   * controls of the leaders form.
   */
  public resetForm(): void {
    this.availableUsers.set([]);
    this.groupLeadersForm.controls.search.setValue('');
    this.groupLeadersForm.controls.possibleUsers.setValue('');
  }

  /**
   * Resets the wizard to its initial state: returns to the details step, resets
   * all three forms and clears the available users and selected future members.
   */
  private reset() {
    this.setDetailsForm();

    if (this.groupDetailsForm !== undefined) {
      this.groupDetailsForm.reset({
        name: '',
        title: '',
        description: '',
        contact: '',
      });
      this.groupDetailsForm.controls.title.markAsPristine();
    }
    if (this.groupLeadersForm !== undefined) {
      this.groupLeadersForm.reset({
        search: '',
        possibleUsers: '',
      });
    }
    if (this.groupNotificationForm !== undefined) {
      this.groupNotificationForm.reset({
        notify: '',
      });
    }

    this.availableUsers.set([]);
    this.futureMembers.set([]);
  }

  /**
   * Adds the users currently selected in the leaders form to
   * {@link futureMembers}, resolving them from {@link availableUsers} and
   * avoiding duplicates.
   */
  public selectUsers(): void {
    const membersTmp: User[] = [];
    if (
      this.groupLeadersForm.controls.possibleUsers.value !== null &&
      this.groupLeadersForm.controls.possibleUsers.value !== undefined &&
      this.groupLeadersForm.controls.possibleUsers.value !== ''
    ) {
      this.groupLeadersForm.controls.possibleUsers.value.forEach(
        (userid: string) => {
          const memberTmp = this.availableUsers().find(
            (user) => user.userId === userid
          );
          if (memberTmp) {
            membersTmp.push(memberTmp);
          }
        }
      );

      this.futureMembers.set(
        this.futureMembers().concat(
          membersTmp.filter((memberTmp) => {
            return !this.futureMembers().some((member) => {
              return member && memberTmp && member.userId === memberTmp.userId;
            });
          })
        )
      );
    }
  }

  /**
   * Removes a user from the list of selected future members.
   *
   * @param m The user to remove from {@link futureMembers}.
   */
  public removeFromFutureMember(m: User): void {
    this.futureMembers.set(
      this.futureMembers().filter((member) => member !== m)
    );
  }

  /**
   * Accessor for the group name form control.
   *
   * @returns The `name` control of the group details form.
   */
  get nameControl(): AbstractControl {
    return this.groupDetailsForm.controls.name;
  }

  /**
   * Accessor for the group title form control.
   *
   * @returns The `title` control of the group details form.
   */
  get titleControl(): AbstractControl {
    return this.groupDetailsForm.controls.title;
  }

  /**
   * Determines whether the wizard is in a valid state for its current step.
   *
   * The details and leaders steps are always considered valid (they only gate
   * navigation); the final step requires at least one selected future member
   * and a valid details form.
   *
   * @returns `true` when the current step allows proceeding.
   */
  public areFormsValid(): boolean {
    if (this.showDetailsForm() || this.showLeadersForm()) {
      return true;
    }
    return this.futureMembers().length > 0 && this.groupDetailsForm.valid;
  }

  /**
   * Provides the i18n label key for the modal's confirm button, depending on
   * the current wizard step.
   *
   * @returns `'label.next'` for the details/leaders steps, otherwise
   * `'label.create'` for the final step.
   */
  public getOkLabel() {
    if (this.showDetailsForm() || this.showLeadersForm()) {
      return 'label.next';
    }
    return 'label.create';
  }

  /**
   * Handles the modal's confirm action: advances to the next wizard step, or
   * triggers group creation when on the final step.
   *
   * @returns A promise that resolves once the step transition or creation has
   * completed.
   */
  public async okAction() {
    if (this.showDetailsForm()) {
      this.setLeadersForm();
    } else if (this.showLeadersForm()) {
      this.setNotificationForm();
    } else {
      await this.createGroup();
    }
  }
}
