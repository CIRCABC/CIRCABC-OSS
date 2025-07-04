import {
  Component,
  Input,
  OnChanges,
  OnInit,
  SimpleChanges,
  output,
  input,
  OnDestroy,
} from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';

import { TranslocoModule, TranslocoService } from '@jsverse/transloco';

import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import {
  MembersService,
  MembershipPostDefinition,
  Profile,
  ProfileService,
  User,
  UserPostDefinition,
  UserProfile,
  UserService,
} from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { getErrorTranslation, getSuccessTranslation } from 'app/core/util';
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
import { DatePicker } from 'primeng/datepicker';
import { setupCalendarDateHandling } from 'app/core/util/date-calendar-util';
import { firstValueFrom, Subscription } from 'rxjs';

@Component({
  selector: 'cbc-create-user',
  templateUrl: './create-user.component.html',
  preserveWhitespaces: true,
  imports: [
    ReactiveFormsModule,
    DataCyDirective,
    ControlMessageComponent,
    SpinnerComponent,
    DatePicker,
    TranslocoModule,
    I18nPipe,
  ],
})
export class CreateUserComponent implements OnInit, OnChanges, OnDestroy {
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  public showWizard = false;
  public readonly groupId = input<string>();
  public readonly modalHide = output<ActionEmitterResult>();
  public readonly userRestored = output();

  public showCreateWizard = false;
  public showInviteWizard = false;

  public addUserForm!: FormGroup;
  public notificationForm!: FormGroup;
  public availableProfiles: Profile[] = [];
  public inviting = false;
  public creating = false;
  public isOSS = false;
  private dateSubscription!: Subscription;

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private membersService: MembersService,
    private translateService: TranslocoService,
    private uiMessageService: UiMessageService,
    private profileService: ProfileService
  ) {}

  public async ngOnInit() {
    if (environment.circabcRelease === 'oss') {
      this.isOSS = true;
    }
    this.buildForm();
    if (this.groupId()) {
      await this.initProfiles();
    }
  }

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

  ngOnDestroy() {
    if (this.dateSubscription) {
      this.dateSubscription.unsubscribe();
    }
  }

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

  public async initProfiles() {
    const groupId = this.groupId();
    if (groupId === undefined) {
      return;
    }
    const profiles = await firstValueFrom(
      this.profileService.getProfiles(groupId)
    );

    this.availableProfiles = [];

    for (const prof of profiles) {
      if (!(prof.name === 'guest' || prof.name === 'EVERYONE')) {
        this.availableProfiles.push(prof);
      }
    }
    if (this.availableProfiles.length > 0) {
      this.notificationForm.controls.selectedProfile.setValue(
        this.availableProfiles[0].id
      );
    }
  }

  async ngOnChanges(changes: SimpleChanges) {
    if (changes.showWizard) {
      this.showCreateWizard = changes.showWizard.currentValue;
    }
  }

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

  public cancelWizard() {
    this.showWizard = false;
    this.showCreateWizard = false;
    this.showInviteWizard = false;
    this.resetForm();
    this.modalHide.emit({ result: ActionResult.CANCELED });
  }

  public launchInviteWizard() {
    this.showCreateWizard = false;
    this.showInviteWizard = true;
  }

  public async submitNewUser() {
    if (!this.addUserForm.valid) {
      return;
    }

    this.creating = true;
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
      await firstValueFrom(this.userService.postUser(postData));

      if (groupId !== undefined) {
        this.launchInviteWizard();
      } else {
        this.cancelWizard();
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

    this.creating = false;
  }

  public async submitMembers() {
    if (!this.hasSelectedProfile()) {
      return;
    }

    this.inviting = true;

    const user: User = {
      userId: this.addUserForm.controls.username.value,
      firstname: this.addUserForm.controls.firstname.value,
      lastname: this.addUserForm.controls.lastname.value,
      email: this.addUserForm.controls.email.value,
    };

    const selectedProfile = this.availableProfiles.find((profile) => {
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
          await firstValueFrom(
            this.membersService.postMember(
              groupId,
              postData,
              expirationDateTime
            )
          );
        } else {
          await firstValueFrom(
            this.membersService.postMember(groupId, postData)
          );
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
      } catch (_error) {
        const res = this.translateService.translate(
          getErrorTranslation(ActionType.ADD_MEMBERSHIPS)
        );
        this.uiMessageService.addErrorMessage(res, false);
      }
    }
    this.inviting = false;
  }

  public hasSelectedProfile(): boolean {
    return this.notificationForm.controls.selectedProfile.value !== '';
  }

  get expirationDateTimeControl(): AbstractControl {
    return this.notificationForm.controls.expirationDateTime;
  }

  get usernameControl(): AbstractControl {
    return this.addUserForm.controls.username;
  }

  get firstnameControl(): AbstractControl {
    return this.addUserForm.controls.firstname;
  }

  get lastnameControl(): AbstractControl {
    return this.addUserForm.controls.lastname;
  }

  get emailControl(): AbstractControl {
    return this.addUserForm.controls.email;
  }

  get phoneControl(): AbstractControl {
    return this.addUserForm.controls.phone;
  }

  get faxControl(): AbstractControl {
    return this.addUserForm.controls.fax;
  }

  get urlAddressControl(): AbstractControl {
    return this.addUserForm.controls.urlAddress;
  }

  get postalAddressControl(): AbstractControl {
    return this.addUserForm.controls.postalAddress;
  }

  get passwordControl(): AbstractControl {
    return this.addUserForm.controls.password;
  }

  get passwordVerifyControl(): AbstractControl {
    return this.addUserForm.controls.passwordVerify;
  }
}
