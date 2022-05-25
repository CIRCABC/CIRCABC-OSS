import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges,
} from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup } from '@angular/forms';

import { TranslateService } from '@ngx-translate/core';

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
  Profile,
  ProfileService,
  User,
  UserProfile,
  UserService,
} from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { getErrorTranslation } from 'app/core/util';
import { ValidationService } from 'app/core/validation.service';
import { RestorableUserProfile } from 'app/group/members/wizard/restorable-user-profile';
import { environment } from 'environments/environment';

@Component({
  selector: 'cbc-invite-user',
  templateUrl: './invite-user.component.html',
  styleUrls: ['./invite-user.component.scss'],
  preserveWhitespaces: true,
})
export class InviteUserComponent implements OnInit, OnChanges {
  @Input()
  public showWizard = false;
  @Input()
  public groupId!: string;
  @Output()
  public readonly modalHide = new EventEmitter();
  @Output()
  public readonly userRestored = new EventEmitter();

  public showAddWizardStep1 = false;
  public showAddWizardStep2 = false;
  public addUserForm!: FormGroup;
  public notificationForm!: FormGroup;
  public availableProfiles: Profile[] = [];
  public availableUsers: User[] = [];
  public futureMembers: UserProfile[] = [];
  public searchingUsers = false;
  public inviting = false;
  public existingMembers: UserProfile[] = [];
  public restorableUsers: RestorableUserProfile[] = [];
  public restoringId = '';
  public isOSS = false;

  public tomorrow = new Date().setTime(
    new Date().getTime() + 24 * 60 * 60 * 1000
  );

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private membersService: MembersService,
    private translateService: TranslateService,
    private uiMessageService: UiMessageService,
    private profileService: ProfileService,
    private historyService: HistoryService
  ) {}

  public async ngOnInit() {
    if (environment.circabcRelease === 'oss') {
      this.isOSS = true;
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
      },
      {
        updateOn: 'change',
      }
    );

    this.notificationForm = this.fb.group(
      {
        userNotifications: [false],
        adminNotifications: [false],
        expiration: [false],
        expirationDateTime: ['', ValidationService.pastDateTimeValidator],
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
      }
    });
  }

  public async initMembers() {
    this.existingMembers = [];
    try {
      const result: PagedUserProfile = await this.membersService
        .getMembers(this.groupId, [], '', -1, -1, '')
        .toPromise();
      if (result && result.data) {
        this.existingMembers = result.data;
      }
    } catch (error) {
      console.error(error);
    }
  }

  public async initProfiles() {
    const profiles = await this.profileService
      .getProfiles(this.groupId)
      .toPromise();

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

  async ngOnChanges(changes: SimpleChanges) {
    if (changes.showWizard) {
      this.showAddWizardStep1 = changes.showWizard.currentValue;
      await this.initMembers();
    }
  }

  public async searchUsers() {
    if (this.addUserForm.controls.name.value !== '') {
      this.addUserForm.controls.possibleUsers.setValue('');
      await this.populateUsers(
        this.addUserForm.controls.name.value,
        this.addUserForm.controls.filter.value
      );
    }
  }

  public async populateUsers(query: string, filter: boolean) {
    this.searchingUsers = true;
    const res = await this.userService.getUsers(query, filter).toPromise();
    this.availableUsers = [];
    for (const user of res) {
      this.availableUsers.push(user);
    }
    this.searchingUsers = false;
  }

  public resetForm(): void {
    this.availableUsers = [];
    this.addUserForm.controls.name.setValue('');
    this.addUserForm.controls.possibleUsers.setValue('');
    this.notificationForm.controls.expiration.setValue(false);
    this.notificationForm.controls.expirationDateTime.setValue('');
  }

  public cancelWizard(backTo: string) {
    if (backTo === 'close') {
      this.showWizard = false;
      this.showAddWizardStep1 = false;
      this.showAddWizardStep2 = false;
      this.availableUsers = [];
      this.futureMembers = [];
      this.resetForm();
      this.modalHide.emit();
    } else if (backTo === 'step1') {
      this.showWizard = true;
      this.showAddWizardStep1 = true;
      this.showAddWizardStep2 = false;
    }
  }

  public async selectUsers() {
    const profileTmp = this.availableProfiles.find((profile) => {
      return profile.id === this.addUserForm.controls.selectedProfile.value;
    });

    const membersTmp: UserProfile[] = [];
    for (const userid of this.addUserForm.controls.possibleUsers.value) {
      const memberTmp: UserProfile = { user: undefined, profile: profileTmp };
      memberTmp.user = this.availableUsers.find((user) => {
        return user.userId === userid;
      });

      const restoreOption = await this.getRestorableOption(
        userid,
        this.groupId
      );
      if (restoreOption.recoverable && memberTmp.user) {
        this.restorableUsers.push({
          userId: userid,
          recoveryOption: restoreOption,
          user: memberTmp.user,
        });
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
            } else {
              return true;
            }
          }) === undefined
        );
      })
    );
    this.addUserForm.controls.possibleUsers.setValue('');
  }

  private async getRestorableOption(userid: string, groupId: string) {
    try {
      return await this.historyService
        .isUserRecoverable(userid, groupId)
        .toPromise();
    } catch (error) {
      return { recoverable: false, profile: undefined };
    }
  }

  public removeFromFutureMember(m: UserProfile): void {
    const index: number = this.futureMembers.indexOf(m, 0);
    this.futureMembers.splice(index, 1);
  }

  public launchAddWizardStep2() {
    this.showAddWizardStep1 = false;
    this.showAddWizardStep2 = true;
  }

  public async submitMembers() {
    this.inviting = true;
    const postData: MembershipPostDefinition = {
      adminNotifications: this.notificationForm.controls.adminNotifications
        .value,
      userNotifications: this.notificationForm.controls.userNotifications.value,
      memberships: this.futureMembers,
    };

    if (this.groupId !== undefined) {
      const result: ActionEmitterResult = {};
      result.type = ActionType.ADD_MEMBERSHIPS;

      try {
        if (this.notificationForm.value.expiration === true) {
          const expirationDateTime: string = this.notificationForm.value.expirationDateTime.toISOString();
          await this.membersService
            .postMember(this.groupId, postData, expirationDateTime)
            .toPromise();
        } else {
          await this.membersService
            .postMember(this.groupId, postData)
            .toPromise();
        }

        this.addUserForm.reset();
        this.notificationForm.reset();
        this.showWizard = false;
        this.availableUsers = [];
        this.futureMembers = [];
        this.showWizard = false;
        this.showAddWizardStep1 = false;
        this.showAddWizardStep2 = false;
        this.resetForm();
        result.result = ActionResult.SUCCEED;
        this.modalHide.emit(result);
      } catch (error) {
        const res = await this.translateService
          .get(getErrorTranslation(ActionType.ADD_MEMBERSHIPS))
          .toPromise();
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

  public async restoreMember(user: RestorableUserProfile) {
    if (
      user.recoveryOption &&
      user.recoveryOption.profile &&
      user.recoveryOption.profile.id
    ) {
      this.restoringId = user.userId;
      try {
        await this.historyService
          .recoverUserMembership(
            user.userId,
            this.groupId,
            user.recoveryOption.profile.id
          )
          .toPromise();
        const i = this.restorableUsers.findIndex((value) => {
          return value.userId === this.restoringId;
        });
        this.restorableUsers.splice(i, 1);
        await this.initMembers();
        this.userRestored.emit();
      } catch (error) {
        console.error(error);
      }
      this.restoringId = '';
    }
  }

  public async cleanRestoreMember(user: RestorableUserProfile) {
    if (
      user.recoveryOption &&
      user.recoveryOption.profile &&
      user.recoveryOption.profile.id
    ) {
      try {
        await this.historyService
          .cleanUserMembershipLogs(this.groupId, user.userId)
          .toPromise();
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
    return this.notificationForm.controls.expirationDateTime;
  }
  get minDate(): Date {
    const result: Date = new Date();
    result.setDate(result.getDate());
    return new Date();
  }
}
