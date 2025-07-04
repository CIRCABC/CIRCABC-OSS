import {
  Component,
  Input,
  OnInit,
  output,
  input,
  OnDestroy,
} from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
} from '@angular/forms';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { ConfirmDialogComponent } from 'app/shared/confirm-dialog/confirm-dialog.component';

import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatTooltipModule } from '@angular/material/tooltip';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import {
  HistoryService,
  MembersService,
  MembershipPostDefinition,
  PagedUserProfile,
  Profile,
  ProfileService,
  User,
  UserProfile,
  UserService,
} from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { getErrorTranslation } from 'app/core/util';
import { RestorableUserProfile } from 'app/group/members/invite-user/restorable-user-profile';
import { ControlMessageComponent } from 'app/shared/control-message/control-message.component';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { environment } from 'environments/environment';
import { DatePicker } from 'primeng/datepicker';
import { firstValueFrom, Subscription } from 'rxjs';
import { LoginService } from 'app/core/login.service';
import { setupCalendarDateHandling } from 'app/core/util/date-calendar-util';

@Component({
  selector: 'cbc-invite-user',
  templateUrl: './invite-user.component.html',
  styleUrl: './invite-user.component.scss',
  preserveWhitespaces: true,
  imports: [
    ReactiveFormsModule,
    MatSlideToggleModule,
    SpinnerComponent,
    MatTooltipModule,
    DatePicker,
    ControlMessageComponent,
    I18nPipe,
    TranslocoModule,
    MatDialogModule,
  ],
})
export class InviteUserComponent implements OnInit, OnDestroy {
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  public showWizard = false;
  public isExternalUser = false;
  public readonly groupId = input.required<string>();
  public readonly modalHide = output<ActionEmitterResult>();
  public readonly userRestored = output();

  public addUserForm!: FormGroup;
  public availableProfiles: Profile[] = [];
  public availableUsers: User[] = [];
  public futureMembers: UserProfile[] = [];
  public searchingUsers = false;
  public inviting = false;
  public existingMembers: UserProfile[] = [];
  public restorableUsers: RestorableUserProfile[] = [];
  public restoringId = '';
  public isOSS = false;
  public noResultFound = false;
  public minDate: Date = new Date(new Date().setHours(23, 59, 0, 0));

  private dateSubscription!: Subscription;

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private membersService: MembersService,
    private translateService: TranslocoService,
    private uiMessageService: UiMessageService,
    private profileService: ProfileService,
    private historyService: HistoryService,
    private dialog: MatDialog,
    private i18nPipe: I18nPipe,
    private loginService: LoginService
  ) {}

  public async ngOnInit() {
    if (environment.circabcRelease === 'oss') {
      this.isOSS = true;
    } else {
      this.isExternalUser =
        this.loginService.getUser().properties?.domain === 'external';
    }
    this.buildForm();
    await this.initProfiles();
    await this.initMembers();
  }

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

  ngOnDestroy() {
    if (this.dateSubscription) {
      this.dateSubscription.unsubscribe();
    }
  }

  public async initMembers() {
    this.existingMembers = [];
    try {
      const result: PagedUserProfile = await firstValueFrom(
        this.membersService.getMembers(this.groupId(), [], '', -1, -1, '')
      );
      if (result?.data) {
        this.existingMembers = result.data;
      }
    } catch (error) {
      console.error(error);
    }
  }

  public async initProfiles() {
    const profiles = await firstValueFrom(
      this.profileService.getProfiles(this.groupId())
    );

    this.availableProfiles = [];

    for (const prof of profiles) {
      if (!(prof.name === 'guest' || prof.name === 'EVERYONE')) {
        this.availableProfiles.push(prof);
      }
    }
    if (this.availableProfiles.length > 0) {
      this.addUserForm.controls.selectedProfile.setValue(
        this.availableProfiles[0].id
      );
    }
  }

  public isAlreadyMember(user: User): boolean {
    for (const member of this.existingMembers) {
      if (member.user && member.user.userId === user.userId) {
        return true;
      }
    }
    return false;
  }

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
      this.initMembers();
    }
  }

  public async populateUsers(query: string, filter: boolean) {
    this.searchingUsers = true;

    try {
      const res = await firstValueFrom(
        this.userService.getUsers(query, filter)
      );
      this.availableUsers = [];
      this.noResultFound = false;
      for (const user of res) {
        this.availableUsers.push(user);
      }
      if (this.availableUsers.length === 0) {
        this.noResultFound = true;
      }
    } catch (error) {
      if (error.error.message.includes('Please enter a valid email address')) {
        this.noResultFound = true;
      }
    }
    this.searchingUsers = false;
  }

  public resetForm(): void {
    this.availableUsers = [];
    this.addUserForm.controls.name.setValue('');
    this.addUserForm.controls.possibleUsers.setValue('');
    this.addUserForm.controls.expiration.setValue(false);
    this.addUserForm.controls.expirationDateTime.setValue('');
    this.addUserForm.controls.filter.setValue(true);
    this.existingMembers = [];
    this.restorableUsers = [];
    this.noResultFound = false;
    this.initProfiles();
  }

  public cancelWizard(backTo: string) {
    if (backTo === 'close') {
      this.showWizard = false;
      this.availableUsers = [];
      this.futureMembers = [];
      this.resetForm();
      this.modalHide.emit({ result: ActionResult.CANCELED });
    } else if (backTo === 'step1') {
      this.showWizard = true;
    }
  }

  public async selectUsers() {
    const profileTmp = this.availableProfiles.find((profile) => {
      return profile.id === this.addUserForm.controls.selectedProfile.value;
    });

    const membersTmp: UserProfile[] = [];
    for (const userId of this.addUserForm.controls.possibleUsers.value) {
      const memberTmp: UserProfile = { user: undefined, profile: profileTmp };
      memberTmp.user = this.availableUsers.find((user) => {
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

    this.futureMembers = this.futureMembers.concat(
      membersTmp.filter((memberTmp) => {
        return (
          this.futureMembers.find((member) => {
            if (member.user && memberTmp.user) {
              return member.user.userId === memberTmp.user.userId;
            }
            return true;
          }) === undefined
        );
      })
    );
    this.addUserForm.controls.possibleUsers.setValue('');

    this.dialogRequestProfile();
  }

  dialogRequestProfile() {
    for (const restorableUser of this.restorableUsers) {
      if (
        restorableUser.recoveryOption.profile?.id !== this.selectedProfile?.id
      ) {
        const dialogRef = this.dialog.open(ConfirmDialogComponent, {
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
      } else {
        this.restoreMember(restorableUser);
      }
    }
  }

  private async getRestorableOption(userId: string, groupId: string) {
    try {
      return await firstValueFrom(
        this.historyService.isUserRecoverable(userId, groupId)
      );
    } catch {
      return { recoverable: false, profile: undefined };
    }
  }

  public removeFromFutureMember(m: UserProfile): void {
    const index: number = this.futureMembers.indexOf(m, 0);
    this.futureMembers.splice(index, 1);
  }

  get selectedProfile() {
    return this.availableProfiles.find(
      (profile) =>
        this.addUserForm.controls.selectedProfile.value === profile.id
    );
  }

  public async submitMembers() {
    this.inviting = true;
    const postData: MembershipPostDefinition = {
      adminNotifications: this.addUserForm.controls.adminNotifications.value,
      userNotifications: this.addUserForm.controls.userNotifications.value,
      memberships: this.futureMembers,
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

        this.addUserForm.reset();
        this.showWizard = false;
        this.availableUsers = [];
        this.futureMembers = [];
        this.showWizard = false;
        this.resetForm();
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

  hasSelectedUserAndProfile(): boolean {
    return (
      this.addUserForm.controls.selectedProfile.value !== '' &&
      this.addUserForm.controls.possibleUsers.value !== ''
    );
  }

  public hasUsersSelected(): boolean {
    return this.futureMembers && this.futureMembers.length > 0;
  }

  public isSearchEmpty() {
    if (this.addUserForm) {
      return (
        this.addUserForm.value.name === '' ||
        this.addUserForm.value.name === null
      );
    }

    return false;
  }
  public isValidEmail() {
    if (this.addUserForm) {
      const emailRegex: RegExp = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      const email = this.addUserForm.value.name;
      return emailRegex.test(email);
    }
    return false;
  }

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
      this.futureMembers.push(memberTmp);
      this.addUserForm.controls.selectedProfile.setValue(profileTemp);
    });
  }

  public restoreMemberNewProfile(user: RestorableUserProfile) {
    this.cleanRestoreMember(user).then(() => {
      this.addUserForm.controls.possibleUsers.setValue([user.userId]);
      const memberTmp: UserProfile = {
        user: user.user,
        profile: this.availableProfiles.find(
          (prof) => this.addUserForm.controls.selectedProfile.value === prof.id
        ),
      };
      this.futureMembers.push(memberTmp);
    });
  }

  public async cleanRestoreMember(user: RestorableUserProfile) {
    if (user.recoveryOption?.profile?.id) {
      try {
        await firstValueFrom(
          this.historyService.cleanUserMembershipLogs(
            this.groupId(),
            user.userId
          )
        );
        const i = this.restorableUsers.findIndex((value) => {
          return value.userId === this.restoringId;
        });
        this.restorableUsers.splice(i, 1);
        await this.initMembers();
      } catch (error) {
        console.error(error);
      }
    }
  }

  get expirationDateTimeControl(): AbstractControl {
    return this.addUserForm.controls.expirationDateTime;
  }
}
