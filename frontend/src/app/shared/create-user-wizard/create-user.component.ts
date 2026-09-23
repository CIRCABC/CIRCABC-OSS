import {
  ChangeDetectionStrategy,
  Component,
  computed,
  effect,
  inject,
  input,
  model,
  OnChanges,
  OnDestroy,
  output,
  resource,
  SimpleChanges,
  signal,
} from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

import { TranslocoModule, TranslocoService } from '@jsverse/transloco';

import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import {
  MembershipPostDefinition,
  MembersService,
  ProfileService,
  User,
  UserPostDefinition,
  UserProfile,
  UserService,
} from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { getErrorTranslation, getSuccessTranslation } from 'app/core/util';
import { setupCalendarDateHandling } from 'app/core/util/date-calendar-util';
import {
  emailValidator,
  nameValidator,
  passwordValidator,
  pastDateTimeValidator,
  phoneValidator,
  urlValidator,
  usernameValidator,
} from 'app/core/validation.service';
import { ControlMessageComponent } from 'app/shared/control-message/control-message.component';
import { DataCyDirective } from 'app/shared/directives/data-cy.directive';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { environment } from 'environments/environment';
import { Subscription } from 'rxjs';

/**
 * Standalone modal wizard component that lets an administrator create a brand
 * new CIRCABC user account and, optionally, immediately invite that user into
 * an interest group with a chosen access profile.
 *
 * The template renders two sequential steps:
 * - A "create" step backed by {@link addUserForm} that captures the account
 *   details (username, name, contact information, address and password).
 * - An "invite" step backed by {@link notificationForm} that assigns an access
 *   profile, configures user/admin notifications and an optional membership
 *   expiration date.
 *
 * When no group context is provided the wizard only creates the account and
 * closes; when a {@link groupId} is present it chains into the invite step so
 * the freshly created user can be added to that group.
 *
 * Key collaborators:
 * - {@link UserService} to persist the new user account.
 * - {@link MembersService} to add the user as a group member.
 * - {@link ProfileService} to load the group's assignable access profiles.
 * - {@link UiMessageService} and {@link TranslocoService} to surface localized
 *   success/error feedback.
 */
@Component({
  selector: 'cbc-create-user',
  templateUrl: './create-user.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ReactiveFormsModule,
    DataCyDirective,
    ControlMessageComponent,
    SpinnerComponent,
    MatDatepickerModule,
    MatInputModule,
    MatFormFieldModule,
    TranslocoModule,
    I18nPipe,
  ],
})
export class CreateUserComponent implements OnChanges, OnDestroy {
  /** Reactive forms builder used to construct the wizard's form groups. */
  private readonly fb = inject(FormBuilder);
  /** API client used to create the new user account. */
  private readonly userService = inject(UserService);
  /** API client used to add the created user as a group member. */
  private readonly membersService = inject(MembersService);
  /** Transloco service used to translate feedback messages. */
  private readonly translateService = inject(TranslocoService);
  /** Service used to push success/error notifications to the UI. */
  private readonly uiMessageService = inject(UiMessageService);
  /** API client used to fetch the assignable access profiles of a group. */
  private readonly profileService = inject(ProfileService);

  /**
   * Two-way bound model controlling the visibility of the wizard. Setting it to
   * `false` hides the wizard; changes are also mirrored into
   * {@link showCreateWizard} via {@link ngOnChanges}.
   */
  public readonly showWizard = model(false);
  /**
   * Optional identifier of the interest group the new user should be invited
   * into. When provided, the wizard loads the group's profiles and chains into
   * the invite step after account creation.
   */
  public readonly groupId = input<string>();
  /**
   * Emits when the wizard is dismissed or completes an action, carrying the
   * resulting {@link ActionEmitterResult} (e.g. cancellation or membership
   * addition outcome).
   */
  public readonly modalHide = output<ActionEmitterResult>();
  /** Emits when a previously deleted/archived user is restored. */
  public readonly userRestored = output();

  /** Whether the account-creation step of the wizard is currently shown. */
  public showCreateWizard = signal(false);
  /** Whether the group-invitation step of the wizard is currently shown. */
  public showInviteWizard = signal(false);

  /** Reactive form holding the new user's account and contact details. */
  public addUserForm!: FormGroup;
  /** Reactive form holding the invitation profile and notification options. */
  public notificationForm!: FormGroup;

  /**
   * Loads the assignable access profiles for the current {@link groupId}. Idle
   * (loader not called) while no group id is set, mirroring the previous
   * `if (this.groupId())` guard. Errors are swallowed and logged since the
   * template has no dedicated error UI for profile loading.
   */
  private readonly profilesResource = resource({
    params: () => this.groupId() || undefined,
    loader: async ({ params: groupId }) => {
      try {
        return await this.profileService.getProfilesAsync({ id: groupId });
      } catch (error) {
        console.error(error);
        return [];
      }
    },
  });
  /**
   * Access profiles that can be assigned to the user for the current group,
   * excluding the `guest` and `EVERYONE` system profiles.
   */
  public availableProfiles = computed(() =>
    (this.profilesResource.value() ?? []).filter(
      (prof) => prof.name !== 'guest' && prof.name !== 'EVERYONE'
    )
  );
  /** True while the group-membership request is in flight. */
  public inviting = signal(false);
  /** True while the user-creation request is in flight. */
  public creating = signal(false);
  /** True when running against the open-source (OSS) CIRCABC release. */
  public isOSS = false;
  /** Subscription for the expiration date calendar handling, cleaned up on destroy. */
  private dateSubscription!: Subscription;

  constructor() {
    if (environment.circabcRelease === 'oss') {
      this.isOSS = true;
    }
    this.buildForm();

    // Preselects the first available profile once profiles have loaded.
    // Syncs the resource's loaded value into the (non-signal) reactive form
    // control, so an `effect` is the appropriate tool here.
    effect(() => {
      const profiles = this.availableProfiles();
      if (profiles.length > 0) {
        this.notificationForm.controls.selectedProfile.setValue(profiles[0].id);
      }
    });
  }

  /**
   * Builds the two reactive form groups used by the wizard:
   * {@link addUserForm} (account details with validators) and
   * {@link notificationForm} (profile selection, notification toggles and an
   * optional expiration date). Also wires the expiration toggle so enabling it
   * pre-fills a date one day ahead and activates calendar date handling, while
   * disabling it resets and disables the date control.
   *
   * @returns Nothing; the form groups are assigned to the component fields.
   */
  public buildForm(): void {
    this.addUserForm = this.fb.group(
      {
        username: ['', [Validators.required, usernameValidator]],
        firstname: ['', [Validators.required, nameValidator]],
        lastname: ['', [Validators.required, nameValidator]],
        email: ['', [Validators.required, emailValidator]],
        phone: ['', [Validators.required, phoneValidator]],
        title: [''],
        companyId: [''],
        fax: ['', [phoneValidator]],
        urlAddress: ['', [urlValidator]],
        postalAddress: ['', [Validators.required]],
        description: [''],
        password: ['', [Validators.required, passwordValidator]],
        passwordVerify: [
          '',
          [
            Validators.required,
            (control: AbstractControl) => this.validateVerifyPassword(control),
          ],
        ],
      },
      {
        updateOn: 'change',
      }
    );

    this.notificationForm = this.fb.group(
      {
        selectedProfile: [''],
        userNotifications: [false],
        adminNotifications: [false],
        expiration: [false],
        expirationDateTime: ['', pastDateTimeValidator],
      },
      {
        updateOn: 'change',
      }
    );
    this.notificationForm.controls.expirationDateTime.disable();
    this.notificationForm.controls.expiration.valueChanges.subscribe((data) => {
      if (data === false) {
        this.notificationForm.controls.expirationDateTime.reset();
        this.notificationForm.controls.expirationDateTime.disable();
      } else {
        const result: Date = new Date();
        result.setDate(result.getDate() + 1);
        this.notificationForm.controls.expirationDateTime.setValue(result);
        this.notificationForm.controls.expirationDateTime.enable();

        this.dateSubscription = setupCalendarDateHandling(
          this.notificationForm.controls.expirationDateTime
        );
      }
    });
  }

  /**
   * Angular lifecycle hook. Unsubscribes from the expiration date calendar
   * subscription to avoid memory leaks.
   */
  ngOnDestroy() {
    if (this.dateSubscription) {
      this.dateSubscription.unsubscribe();
    }
  }

  /**
   * Cross-field validator ensuring the password confirmation matches the
   * chosen password.
   *
   * @param control The password-verify control being validated.
   * @returns `null` when the confirmation is valid (or the form is not yet
   * built), otherwise an error object `{ invalidPasswordConfirm: true }`.
   */
  private validateVerifyPassword(
    control: AbstractControl
  ): { [key: string]: boolean } | null {
    if (this.addUserForm === undefined) {
      return null;
    }
    if (control.value === null) {
      return { invalidPasswordConfirm: true };
    }
    if (
      control.value === null ||
      this.addUserForm.controls.password.value === null
    ) {
      return { invalidPasswordConfirm: true };
    }
    if (control.value !== this.addUserForm.controls.password.value) {
      return { invalidPasswordConfirm: true };
    }
    return null;
  }

  /**
   * Angular lifecycle hook reacting to input changes. Mirrors the
   * {@link showWizard} value into {@link showCreateWizard} so the create step
   * is displayed when the wizard is opened.
   *
   * @param changes The set of changed input properties.
   * @returns A promise (async hook) that resolves once the change is applied.
   */
  ngOnChanges(changes: SimpleChanges) {
    void this.handleChanges(changes);
  }

  private async handleChanges(changes: SimpleChanges) {
    if (changes.showWizard) {
      this.showCreateWizard.set(changes.showWizard.currentValue);
    }
  }

  /**
   * Clears every field of both {@link addUserForm} and the relevant
   * {@link notificationForm} controls back to their default empty state.
   */
  public resetForm(): void {
    this.addUserForm.controls.username.setValue('');
    this.addUserForm.controls.firstname.setValue('');
    this.addUserForm.controls.lastname.setValue('');
    this.addUserForm.controls.email.setValue('');
    this.addUserForm.controls.phone.setValue('');
    this.addUserForm.controls.title.setValue('');
    this.addUserForm.controls.companyId.setValue('');
    this.addUserForm.controls.fax.setValue('');
    this.addUserForm.controls.urlAddress.setValue('');
    this.addUserForm.controls.postalAddress.setValue('');
    this.addUserForm.controls.description.setValue('');
    this.addUserForm.controls.password.setValue('');
    this.addUserForm.controls.passwordVerify.setValue('');

    this.notificationForm.controls.expiration.setValue(false);
    this.notificationForm.controls.expirationDateTime.setValue('');
  }

  /**
   * Cancels the wizard: hides all steps, resets the forms and emits a
   * {@link ActionResult.CANCELED} result through {@link modalHide}.
   */
  public cancelWizard() {
    this.showWizard.set(false);
    this.showCreateWizard.set(false);
    this.showInviteWizard.set(false);
    this.resetForm();
    this.modalHide.emit({ result: ActionResult.CANCELED });
  }

  /**
   * Transitions the wizard from the account-creation step to the
   * group-invitation step.
   */
  public launchInviteWizard() {
    this.showCreateWizard.set(false);
    this.showInviteWizard.set(true);
  }

  /**
   * Submits the account-creation form to create a new user. On success, either
   * closes the wizard (no group context) or advances to the invite step (group
   * context), showing a localized success message. On failure, surfaces a
   * localized error message; HTTP 409 conflicts use the server-provided
   * message. Does nothing when {@link addUserForm} is invalid.
   *
   * @returns A promise that resolves once the creation attempt completes and
   * the {@link creating} flag is cleared.
   */
  public async submitNewUser() {
    if (!this.addUserForm.valid) {
      return;
    }

    this.creating.set(true);
    const postData: UserPostDefinition = {
      userId: this.addUserForm.controls.username.value,
      firstname: this.addUserForm.controls.firstname.value,
      lastname: this.addUserForm.controls.lastname.value,
      email: this.addUserForm.controls.email.value,
      phone: this.addUserForm.controls.phone.value,
      title: this.addUserForm.controls.title.value,
      companyId: this.addUserForm.controls.companyId.value,
      fax: this.addUserForm.controls.fax.value,
      urlAddress: this.addUserForm.controls.urlAddress.value,
      postalAddress: this.addUserForm.controls.postalAddress.value,
      description: this.addUserForm.controls.description.value,
      password: this.addUserForm.controls.password.value,
    };

    const groupId = this.groupId();
    if (groupId !== undefined) {
      postData.currentIgId = groupId;
    }

    try {
      await this.userService.postUserAsync({ userPostDefinition: postData });

      if (groupId === undefined) {
        this.cancelWizard();
      } else {
        this.launchInviteWizard();
      }

      const res = this.translateService.translate(
        getSuccessTranslation(ActionType.CREATE_USER)
      );
      this.uiMessageService.addSuccessMessage(res, true);
    } catch (error) {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      let messageKey: any;
      if (error.status === 409) {
        messageKey = error.error.message;
      } else {
        messageKey = error.message;
      }
      const res = this.translateService.translate(messageKey);
      this.uiMessageService.addErrorMessage(res, false);
    }

    this.creating.set(false);
  }

  /**
   * Adds the newly created user as a member of the current group using the
   * selected profile and notification settings, optionally with a membership
   * expiration date. On success emits an {@link ActionType.ADD_MEMBERSHIPS}
   * result via {@link modalHide} and closes the wizard; on failure shows a
   * localized error. Does nothing when no profile is selected or no group id is
   * set.
   *
   * @returns A promise that resolves once the membership attempt completes and
   * the {@link inviting} flag is cleared.
   */
  public async submitMembers() {
    if (!this.hasSelectedProfile()) {
      return;
    }

    this.inviting.set(true);

    const user: User = {
      userId: this.addUserForm.controls.username.value,
      firstname: this.addUserForm.controls.firstname.value,
      lastname: this.addUserForm.controls.lastname.value,
      email: this.addUserForm.controls.email.value,
    };

    const selectedProfile = this.availableProfiles().find((profile) => {
      return (
        profile.id === this.notificationForm.controls.selectedProfile.value
      );
    });

    const userProfile: UserProfile = { user: user, profile: selectedProfile };

    const postData: MembershipPostDefinition = {
      adminNotifications:
        this.notificationForm.controls.adminNotifications.value,
      userNotifications: this.notificationForm.controls.userNotifications.value,
      memberships: [userProfile],
    };

    const groupId = this.groupId();
    if (groupId !== undefined) {
      try {
        if (this.notificationForm.value.expiration === true) {
          const expirationDateTime: string =
            this.notificationForm.value.expirationDateTime.toISOString();
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

        const res = this.translateService.translate(
          getSuccessTranslation(ActionType.ADD_MEMBERSHIPS)
        );
        this.uiMessageService.addSuccessMessage(res, true);

        this.cancelWizard();

        const result: ActionEmitterResult = {};
        result.type = ActionType.ADD_MEMBERSHIPS;
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
   * Indicates whether an access profile has been selected in the invite step.
   *
   * @returns `true` when a non-empty profile is selected, otherwise `false`.
   */
  public hasSelectedProfile(): boolean {
    return this.notificationForm.controls.selectedProfile.value !== '';
  }

  /** Accessor for the membership expiration date/time form control. */
  get expirationDateTimeControl(): AbstractControl {
    return this.notificationForm.controls.expirationDateTime;
  }

  /** Accessor for the username form control. */
  get usernameControl(): AbstractControl {
    return this.addUserForm.controls.username;
  }

  /** Accessor for the first name form control. */
  get firstnameControl(): AbstractControl {
    return this.addUserForm.controls.firstname;
  }

  /** Accessor for the last name form control. */
  get lastnameControl(): AbstractControl {
    return this.addUserForm.controls.lastname;
  }

  /** Accessor for the email form control. */
  get emailControl(): AbstractControl {
    return this.addUserForm.controls.email;
  }

  /** Accessor for the phone form control. */
  get phoneControl(): AbstractControl {
    return this.addUserForm.controls.phone;
  }

  /** Accessor for the fax form control. */
  get faxControl(): AbstractControl {
    return this.addUserForm.controls.fax;
  }

  /** Accessor for the URL address form control. */
  get urlAddressControl(): AbstractControl {
    return this.addUserForm.controls.urlAddress;
  }

  /** Accessor for the postal address form control. */
  get postalAddressControl(): AbstractControl {
    return this.addUserForm.controls.postalAddress;
  }

  /** Accessor for the password form control. */
  get passwordControl(): AbstractControl {
    return this.addUserForm.controls.password;
  }

  /** Accessor for the password confirmation form control. */
  get passwordVerifyControl(): AbstractControl {
    return this.addUserForm.controls.passwordVerify;
  }
}
