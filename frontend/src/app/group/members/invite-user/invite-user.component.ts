import {
  ChangeDetectionStrategy,
  Component,
  effect,
  inject,
  input,
  model,
  OnDestroy,
  output,
  resource,
  signal,
} from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
} from '@angular/forms';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import {
  HistoryService,
  MembershipPostDefinition,
  MembersService,
  PagedUserProfile,
  ProfileService,
  User,
  UserProfile,
  UserService,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { getErrorTranslation } from 'app/core/util';
import { setupCalendarDateHandling } from 'app/core/util/date-calendar-util';
import { RestorableUserProfile } from 'app/group/members/invite-user/restorable-user-profile';
import { ConfirmDialogComponent } from 'app/shared/confirm-dialog/confirm-dialog.component';
import { ControlMessageComponent } from 'app/shared/control-message/control-message.component';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { environment } from 'environments/environment';
import { Subscription } from 'rxjs';

/**
 * Standalone Angular component (`cbc-invite-user`) that drives the wizard used
 * to invite one or more users into an interest group.
 *
 * The component renders a multi-step form that lets an administrator:
 * - search for existing (or, for external users, email-based) users,
 * - assign an access {@link Profile} to the selected users,
 * - optionally set a membership expiration date/time,
 * - handle users whose past membership can be restored (recoverable
 *   memberships) via a confirmation dialog,
 * - and finally submit the resulting memberships to the backend with optional
 *   user/admin notifications and a notification comment.
 *
 * It collaborates with the generated CIRCABC API services
 * ({@link UserService}, {@link MembersService}, {@link ProfileService},
 * {@link HistoryService}), the {@link LoginService} to determine the current
 * user context (e.g. external users, OSS release), the {@link UiMessageService}
 * for surfacing errors, and {@link MatDialog} for restorable-membership
 * confirmations.
 *
 * @remarks Uses reactive forms and Angular signal-based inputs/outputs.
 */
@Component({
  selector: 'cbc-invite-user',
  templateUrl: './invite-user.component.html',
  styleUrl: './invite-user.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ReactiveFormsModule,
    MatSlideToggleModule,
    SpinnerComponent,
    MatTooltipModule,
    MatDatepickerModule,
    MatInputModule,
    MatFormFieldModule,
    ControlMessageComponent,
    I18nPipe,
    TranslocoModule,
    MatDialogModule,
  ],
})
export class InviteUserComponent implements OnDestroy {
  private readonly fb = inject(FormBuilder);
  private readonly userService = inject(UserService);
  private readonly membersService = inject(MembersService);
  private readonly translateService = inject(TranslocoService);
  private readonly uiMessageService = inject(UiMessageService);
  private readonly profileService = inject(ProfileService);
  private readonly historyService = inject(HistoryService);
  private readonly dialog = inject(MatDialog);
  private readonly i18nPipe = inject(I18nPipe);
  private readonly loginService = inject(LoginService);

  /**
   * Two-way bindable model controlling whether the invitation wizard
   * (the second step of the flow) is currently displayed.
   */
  public readonly showWizard = model(false);
  /**
   * Whether the currently logged-in user belongs to the `external` domain,
   * which changes the search behavior to email-based lookups.
   */
  public isExternalUser = false;
  /**
   * Required input carrying the identifier of the group the users are being
   * invited into.
   */
  public readonly groupId = input.required<string>();
  /**
   * Emitted when the modal/wizard should be closed, carrying the outcome of
   * the operation (canceled, succeeded, etc.).
   */
  public readonly modalHide = output<ActionEmitterResult>();
  /**
   * Emitted when a previously removed member has been restored, allowing the
   * parent to refresh its state.
   */
  public readonly userRestored = output();

  /** Reactive form backing the invitation wizard. */
  public addUserForm!: FormGroup;

  /**
   * Loads the assignable access profiles for the group, excluding the `guest`
   * and `EVERYONE` profiles. Reruns whenever {@link groupId} changes. Errors
   * are logged and leave the list empty rather than propagating, since there
   * is no dedicated error UI for this section.
   */
  private readonly profilesResource = resource({
    params: () => this.groupId(),
    loader: async ({ params: groupId }) => {
      try {
        const profiles = await this.profileService.getProfilesAsync({
          id: groupId,
        });
        return profiles.filter(
          (prof) => !(prof.name === 'guest' || prof.name === 'EVERYONE')
        );
      } catch (error) {
        console.error(error);
        return [];
      }
    },
    defaultValue: [],
  });

  /**
   * Loads the current members of the group. Reruns whenever {@link groupId}
   * changes, or can be forced via {@link membersResource}`.reload()` (e.g.
   * after a search or a membership restore). Errors are logged and leave the
   * list empty rather than propagating.
   */
  private readonly membersResource = resource({
    params: () => this.groupId(),
    loader: async ({ params: groupId }) => {
      try {
        const result: PagedUserProfile =
          await this.membersService.getMembersAsync({
            id: groupId,
            profile: [],
            language: '',
            limit: -1,
            page: -1,
            order: '',
          });
        return result?.data ?? [];
      } catch (error) {
        console.error(error);
        return [];
      }
    },
    defaultValue: [],
  });

  /** Access profiles available for assignment within the group (excluding guest/EVERYONE). */
  public readonly availableProfiles = this.profilesResource.value;
  /** Users returned by the most recent search that can be selected. */
  public readonly availableUsers = signal<User[]>([]);
  /** Users staged for invitation but not yet submitted. */
  public readonly futureMembers = signal<UserProfile[]>([]);
  /** True while a user search request is in flight. */
  public readonly searchingUsers = signal(false);
  /** True while the membership submission request is in flight. */
  public readonly inviting = signal(false);
  /** Existing members of the group, used to avoid inviting duplicates. */
  public readonly existingMembers = this.membersResource.value;
  /** Selected users whose prior membership can be restored. */
  public restorableUsers: RestorableUserProfile[] = [];
  /** Identifier of the user currently being restored. */
  public restoringId = '';
  /** True when running against the open-source (`oss`) release. */
  public isOSS = false;
  /** True when the last search returned no matching users. */
  public readonly noResultFound = signal(false);
  /** Earliest selectable expiration date/time (end of the current day). */
  public minDate: Date = new Date(new Date().setHours(23, 59, 0, 0));

  /** Subscription managing calendar/date-picker handling for the expiration field. */
  private dateSubscription!: Subscription;

  constructor() {
    if (environment.circabcRelease === 'oss') {
      this.isOSS = true;
    } else {
      this.isExternalUser =
        this.loginService.getUser().properties?.domain === 'external';
    }
    this.buildForm();

    /**
     * Preselects the first available profile whenever the profiles resource
     * loads a new list, mirroring the previous `ngOnInit` behavior. Uses an
     * `effect` because it syncs a signal into the imperative reactive-forms
     * API rather than into another signal.
     */
    effect(() => {
      const profiles = this.availableProfiles();
      if (profiles.length > 0) {
        this.addUserForm.controls.selectedProfile.setValue(profiles[0].id);
      }
    });
  }

  /**
   * Builds the reactive form controlling the invitation wizard and wires up
   * the expiration date handling: enabling/disabling and resetting the
   * expiration date/time control based on the `expiration` toggle.
   */
  public buildForm(): void {
    this.addUserForm = this.fb.group(
      {
        name: [''],
        possibleUsers: [''],
        selectedProfile: [''],
        filter: [true],
        userNotifications: [false],
        adminNotifications: [false],
        expiration: [false],
        expirationDateTime: [this.minDate],
        comment: [''],
      },
      {
        updateOn: 'change',
      }
    );

    // this.addUserForm.controls.expirationDateTime.valueChanges.subscribe((change) => {
    //   if (change) {
    //     this.addUserForm.controls.expirationDateTime.setValue(
    //       new Date(data).setHours(23, 59, 0, 0)
    //     );
    //   }

    this.dateSubscription = setupCalendarDateHandling(
      this.addUserForm.controls.expirationDateTime
    );

    this.addUserForm.controls.expirationDateTime.disable();
    this.addUserForm.controls.expiration.valueChanges.subscribe((data) => {
      if (data === false) {
        this.addUserForm.controls.expirationDateTime.reset();
        this.addUserForm.controls.expirationDateTime.disable();
      } else {
        this.addUserForm.controls.expirationDateTime.setValue(this.minDate);
        this.addUserForm.controls.expirationDateTime.enable();
      }
    });
  }

  /**
   * Angular lifecycle hook. Cleans up the date-handling subscription to
   * prevent memory leaks.
   */
  ngOnDestroy() {
    if (this.dateSubscription) {
      this.dateSubscription.unsubscribe();
    }
  }

  /**
   * Determines whether the given user is already a member of the group.
   *
   * @param user The user to check against the existing members.
   * @returns `true` if the user is already a member, otherwise `false`.
   */
  public isAlreadyMember(user: User): boolean {
    for (const member of this.existingMembers()) {
      if (member.user && member.user.userId === user.userId) {
        return true;
      }
    }
    return false;
  }

  /**
   * Runs a user search based on the current `name` form value. For external
   * users the value must be a valid email address. On success the results are
   * populated and the existing members are refreshed.
   *
   * @returns A promise that resolves once the search (if performed) completes.
   */
  public async searchUsers() {
    let isValid = true;
    if (this.isExternalUser && !this.isValidEmail()) {
      isValid = false;
    }
    if (isValid) {
      if (this.addUserForm.controls.name.value !== '') {
        this.addUserForm.controls.possibleUsers.setValue('');
        await this.populateUsers(
          this.addUserForm.controls.name.value,
          this.addUserForm.controls.filter.value
        );
      }
      this.membersResource.reload();
    }
  }

  /**
   * Queries the backend for users matching the supplied search string and
   * populates {@link availableUsers}. Sets {@link noResultFound} when no users
   * are returned or when the backend reports an invalid email address.
   *
   * @param query The search term (name or email) to look up.
   * @param filter Whether to apply server-side filtering of the results.
   * @returns A promise that resolves once the search completes.
   */
  public async populateUsers(query: string, filter: boolean) {
    this.searchingUsers.set(true);

    try {
      const res = await this.userService.getUsersAsync({ query, filter });
      const users: User[] = [];
      this.noResultFound.set(false);
      for (const user of res) {
        users.push(user);
      }
      this.availableUsers.set(users);
      if (users.length === 0) {
        this.noResultFound.set(true);
      }
    } catch (error) {
      if (error.error.message.includes('Please enter a valid email address')) {
        this.noResultFound.set(true);
      }
    }
    this.searchingUsers.set(false);
  }

  /**
   * Resets the search-related form controls and state back to their defaults
   * and reloads the available profiles and existing members.
   */
  public resetForm(): void {
    this.availableUsers.set([]);
    this.addUserForm.controls.name.setValue('');
    this.addUserForm.controls.possibleUsers.setValue('');
    this.addUserForm.controls.expiration.setValue(false);
    this.addUserForm.controls.expirationDateTime.setValue('');
    this.addUserForm.controls.filter.setValue(true);
    this.restorableUsers = [];
    this.noResultFound.set(false);
    this.profilesResource.reload();
    this.membersResource.reload();
  }

  /**
   * Handles wizard navigation/cancellation.
   *
   * @param backTo When `'close'`, closes the wizard, clears state and emits a
   *   {@link ActionResult.CANCELED} result via {@link modalHide}. When
   *   `'step1'`, re-displays the wizard.
   */
  public cancelWizard(backTo: string) {
    if (backTo === 'close') {
      this.showWizard.set(false);
      this.availableUsers.set([]);
      this.futureMembers.set([]);
      this.resetForm();
      this.modalHide.emit({ result: ActionResult.CANCELED });
    } else if (backTo === 'step1') {
      this.showWizard.set(true);
    }
  }

  /**
   * Confirms the currently selected users with the chosen profile. Users with
   * a recoverable prior membership are collected into {@link restorableUsers}
   * for confirmation, while the rest are added to {@link futureMembers}
   * (deduplicated). Finally triggers any required restore confirmation dialogs.
   *
   * @returns A promise that resolves once the selection has been processed.
   */
  public async selectUsers() {
    const profileTmp = this.availableProfiles().find((profile) => {
      return profile.id === this.addUserForm.controls.selectedProfile.value;
    });

    const membersTmp: UserProfile[] = [];
    for (const userId of this.addUserForm.controls.possibleUsers.value) {
      const memberTmp: UserProfile = { user: undefined, profile: profileTmp };
      memberTmp.user = this.availableUsers().find((user) => {
        return user.userId === userId;
      });

      const restoreOption = await this.getRestorableOption(
        userId,
        this.groupId()
      );

      if (restoreOption.recoverable && memberTmp.user) {
        const userExists = this.restorableUsers.some((user) => {
          return user.userId === userId;
        });
        if (!userExists) {
          this.restorableUsers.push({
            userId: userId,
            recoveryOption: restoreOption,
            user: memberTmp.user,
          });
        }
      } else {
        membersTmp.push(memberTmp);
      }
    }

    const currentMembers = this.futureMembers();
    this.futureMembers.set(
      currentMembers.concat(
        membersTmp.filter((memberTmp) => {
          return !currentMembers.some((member) => {
            if (member.user && memberTmp.user) {
              return member.user.userId === memberTmp.user.userId;
            }
            return true;
          });
        })
      )
    );
    this.addUserForm.controls.possibleUsers.setValue('');

    this.dialogRequestProfile();
  }

  /**
   * For each restorable user, either restores their membership directly (when
   * their recoverable profile matches the currently selected profile) or opens
   * a confirmation dialog letting the administrator choose between the
   * recoverable profile and the newly selected profile.
   */
  dialogRequestProfile() {
    for (const restorableUser of this.restorableUsers) {
      if (
        restorableUser.recoveryOption.profile?.id === this.selectedProfile?.id
      ) {
        this.restoreMember(restorableUser);
      } else {
        const dialogRef = this.dialog.open(ConfirmDialogComponent, {
          ariaLabel: 'Dialog',
          data: {
            title: 'label.restorable.memberships.explanation',
            message: `${restorableUser.user.firstname} ${restorableUser.user.lastname}`,
            message2: restorableUser.user.email,
            labelOK: this.i18nPipe.transform(
              restorableUser.recoveryOption.profile?.title
            ),
            labelCancel: this.i18nPipe.transform(this.selectedProfile?.title),
            layoutStyle: 'inviteUsers',
          },
        });

        dialogRef.afterClosed().subscribe((result) => {
          if (result) {
            this.restoreMember(restorableUser);
          } else {
            this.restoreMemberNewProfile(restorableUser);
          }
        });
      }
    }
  }

  /**
   * Retrieves the recoverable-membership option for a user within a group.
   *
   * @param userId The identifier of the user to check.
   * @param groupId The identifier of the group.
   * @returns A promise resolving to the recovery option; returns a non-recoverable
   *   option (`{ recoverable: false, profile: undefined }`) if the lookup fails.
   */
  private async getRestorableOption(userId: string, groupId: string) {
    try {
      return await this.historyService.isUserRecoverableAsync({
        userId,
        groupId,
      });
    } catch {
      return { recoverable: false, profile: undefined };
    }
  }

  /**
   * Removes a staged member from {@link futureMembers}.
   *
   * @param m The user/profile entry to remove.
   */
  public removeFromFutureMember(m: UserProfile): void {
    this.futureMembers.set(
      this.futureMembers().filter((member) => member !== m)
    );
  }

  /**
   * The profile currently selected in the form, resolved from
   * {@link availableProfiles}.
   *
   * @returns The matching {@link Profile}, or `undefined` if none is selected.
   */
  get selectedProfile() {
    return this.availableProfiles().find(
      (profile) =>
        this.addUserForm.controls.selectedProfile.value === profile.id
    );
  }

  /**
   * Submits the staged memberships ({@link futureMembers}) to the backend,
   * including notification preferences, an optional comment and an optional
   * expiration date/time. On success clears the form/state and emits a
   * {@link ActionResult.SUCCEED} result via {@link modalHide}; on failure a
   * translated error message is shown through the {@link UiMessageService}.
   *
   * @returns A promise that resolves once the submission attempt completes.
   */
  public async submitMembers() {
    this.inviting.set(true);
    const postData: MembershipPostDefinition = {
      adminNotifications: this.addUserForm.controls.adminNotifications.value,
      userNotifications: this.addUserForm.controls.userNotifications.value,
      memberships: this.futureMembers(),
      notifyText:
        this.addUserForm.controls.userNotifications.value === true
          ? this.addUserForm.controls.comment.value
          : '',
    };

    const groupId = this.groupId();
    if (groupId !== undefined) {
      const result: ActionEmitterResult = {};
      result.type = ActionType.ADD_MEMBERSHIPS;

      try {
        if (this.addUserForm.value.expiration === true) {
          const expirationDateTime: string =
            this.addUserForm.value.expirationDateTime.toISOString();
          await this.membersService.postMemberAsync({
            id: groupId,
            membershipPostDefinition: postData,
            expirationDate: expirationDateTime,
          });
        } else {
          await this.membersService.postMemberAsync({
            id: groupId,
            membershipPostDefinition: postData,
          });
        }

        this.addUserForm.reset();
        this.showWizard.set(false);
        this.availableUsers.set([]);
        this.futureMembers.set([]);
        this.showWizard.set(false);
        this.resetForm();
        result.result = ActionResult.SUCCEED;
        this.modalHide.emit(result);
      } catch (error) {
        console.error(error);
        const res = this.translateService.translate(
          getErrorTranslation(ActionType.ADD_MEMBERSHIPS)
        );
        this.uiMessageService.addErrorMessage(res, false);
      }
    }
    this.inviting.set(false);
  }

  /**
   * Indicates whether both a profile and at least one user have been selected
   * in the form.
   *
   * @returns `true` when a profile and possible users are selected.
   */
  hasSelectedUserAndProfile(): boolean {
    return (
      this.addUserForm.controls.selectedProfile.value !== '' &&
      this.addUserForm.controls.possibleUsers.value !== ''
    );
  }

  /**
   * Indicates whether any users have been staged for invitation.
   *
   * @returns `true` when {@link futureMembers} is non-empty.
   */
  public hasUsersSelected(): boolean {
    return this.futureMembers().length > 0;
  }

  /**
   * Indicates whether the search field is empty.
   *
   * @returns `true` when the `name` control is empty/null (or the form is not
   *   yet built).
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
   * Validates whether the current `name` form value is a well-formed email
   * address.
   *
   * @returns `true` when the value matches the email pattern, otherwise `false`.
   */
  public isValidEmail() {
    if (this.addUserForm) {
      const emailRegex: RegExp = /^[^\s@]+@[^\s@]+\.[^\s@]+$/; // NOSONAR - no backtracking, literal anchors separate segments
      const email = this.addUserForm.value.name;
      return emailRegex.test(email);
    }
    return false;
  }

  /**
   * Restores a user's previous membership using their recoverable profile.
   * After cleaning the historical membership logs, the user is added to
   * {@link futureMembers} with the recovered profile while preserving the
   * currently selected profile in the form.
   *
   * @param user The restorable user/profile entry to restore.
   * @returns A promise that resolves once the restore flow is initiated.
   */
  public async restoreMember(user: RestorableUserProfile) {
    this.cleanRestoreMember(user).then(() => {
      this.addUserForm.controls.possibleUsers.setValue([user.userId]);
      const profileTemp = this.addUserForm.controls.selectedProfile.value;
      this.addUserForm.controls.selectedProfile.setValue(
        user.recoveryOption.profile?.id
      );
      const memberTmp: UserProfile = {
        user: user.user,
        profile: user.recoveryOption.profile,
      };
      this.futureMembers.set([...this.futureMembers(), memberTmp]);
      this.addUserForm.controls.selectedProfile.setValue(profileTemp);
    });
  }

  /**
   * Restores a user after cleaning their membership logs, but assigns the
   * profile currently selected in the form rather than their recoverable one,
   * then adds them to {@link futureMembers}.
   *
   * @param user The restorable user/profile entry to restore.
   */
  public restoreMemberNewProfile(user: RestorableUserProfile) {
    this.cleanRestoreMember(user).then(() => {
      this.addUserForm.controls.possibleUsers.setValue([user.userId]);
      const memberTmp: UserProfile = {
        user: user.user,
        profile: this.availableProfiles().find(
          (prof) => this.addUserForm.controls.selectedProfile.value === prof.id
        ),
      };
      this.futureMembers.set([...this.futureMembers(), memberTmp]);
    });
  }

  /**
   * Clears the historical membership logs for a restorable user so their
   * membership can be recreated, removes them from {@link restorableUsers} and
   * refreshes the existing members. Errors are logged rather than propagated.
   *
   * @param user The restorable user/profile entry whose logs should be cleaned.
   * @returns A promise that resolves once the cleanup completes.
   */
  public async cleanRestoreMember(user: RestorableUserProfile) {
    if (user.recoveryOption?.profile?.id) {
      try {
        await this.historyService.cleanUserMembershipLogsAsync({
          groupId: this.groupId(),
          userId: user.userId,
        });
        const i = this.restorableUsers.findIndex((value) => {
          return value.userId === this.restoringId;
        });
        this.restorableUsers.splice(i, 1);
        this.membersResource.reload();
      } catch (error) {
        console.error(error);
      }
    }
  }

  /**
   * Convenience accessor for the `expirationDateTime` form control.
   *
   * @returns The {@link AbstractControl} backing the expiration date/time field.
   */
  get expirationDateTimeControl(): AbstractControl {
    return this.addUserForm.controls.expirationDateTime;
  }
}
