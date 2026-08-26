import { Component, Inject, OnInit, Optional } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { TranslocoModule, TranslocoService } from '@jsverse/transloco';

import { DatePipe } from '@angular/common';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import {
  BASE_PATH,
  InterestGroup,
  InterestGroupProfile,
  InterestGroupService,
  MembersService,
  PagedUserProfile,
  Profile,
  ProfileService,
  User,
  UserProfile,
  UserService,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { SaveAsService } from 'app/core/save-as.service';
import { SelectableUserProfile } from 'app/core/ui-model/index';
import {
  changeSort,
  getErrorTranslation,
  getSuccessTranslation,
} from 'app/core/util';
import { ListingOptions } from 'app/group/listing-options/listing-options';
import { CreateUserComponent } from 'app/shared/create-user-wizard/create-user.component';
import { HintComponent } from 'app/shared/hint/hint.component';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { NumberBadgeComponent } from 'app/shared/number-badge/number-badge.component';
import { PagerComponent } from 'app/shared/pager/pager.component';
import { DownloadPipe } from 'app/shared/pipes/download.pipe';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { SecurePipe } from 'app/shared/pipes/secure.pipe';
import { SetTitlePipe } from 'app/shared/pipes/set-title.pipe';
import { ReponsiveSubMenuComponent } from 'app/shared/reponsive-sub-menu/reponsive-sub-menu.component';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { environment } from 'environments/environment';
import { firstValueFrom } from 'rxjs';
import { ChangeProfilesMultipleComponent } from './change-profiles-multiple/change-profiles-multiple.component';
import { ChangeUserProfileComponent } from './change-user-profile/change-user-profile.component';
import { EditExpirationComponent } from './edit-expiration/edit-expiration.component';
import { InviteUserComponent } from './invite-user/invite-user.component';
import { MembersDropdownComponent } from './members-dropdown.component';
import { UninviteMultipleComponent } from './uninvite-multiple/uninvite-multiple.component';
import { UninviteUserComponent } from './uninvite/uninvite-user.component';
@Component({
  selector: 'cbc-members',
  templateUrl: './members.component.html',
  styleUrl: './members.component.scss',
  preserveWhitespaces: true,
  imports: [
    HorizontalLoaderComponent,
    ReponsiveSubMenuComponent,
    RouterLink,
    MembersDropdownComponent,
    PagerComponent,
    HintComponent,
    ReactiveFormsModule,
    NumberBadgeComponent,
    SpinnerComponent,
    UninviteUserComponent,
    UninviteMultipleComponent,
    ChangeProfilesMultipleComponent,
    ChangeUserProfileComponent,
    InviteUserComponent,
    CreateUserComponent,
    EditExpirationComponent,
    DatePipe,
    DownloadPipe,
    I18nPipe,
    SecurePipe,
    SetTitlePipe,
    TranslocoModule,
  ],
})
export class MembersComponent implements OnInit {
  public selectChangeMember!: UserProfile;
  public members!: SelectableUserProfile[];
  public nodeId!: string;
  public showSearchBox = false;
  public showWizard = false;
  public showUserCreateWizard = false;
  public showChangeDialog = false;
  public showUninviteDialog = false;
  public showUninviteMultipleDialog = false;
  public showMultipleChangeDialog = false;
  public showExpirationDialog = false;
  public selectedUser: User | undefined;
  public selectedUsers: SelectableUserProfile[] = [];
  public selectedExpiredUsers: SelectableUserProfile[] = [];
  public currentGroup?: InterestGroup;
  public listingOptions: ListingOptions = {
    page: 1,
    limit: 10,
    sort: 'lastName_ASC',
  };
  public totalItems = 10;
  // eslint-disable-next-line no-magic-numbers
  public availableProfiles: Profile[] = [];
  public searchForm!: FormGroup;
  public loading = false;
  public alreadyMember = false;
  public exportForm!: FormGroup;
  public currentUserMemberships!: InterestGroupProfile[];
  public allSelected = false;
  public exporting = false;
  public isOSS = false;
  private firstName = '';
  private lastName = '';
  private email = '';
  private searchProfile = '';

  // properties for the exporter (format to export the file and the file id)
  public exportFormats = [
    { code: 'csv', name: 'CSV' },
    { code: 'xml', name: 'XML' },
    { code: 'xls', name: 'Excel' },
  ];

  private basePath!: string;

  constructor(
    private route: ActivatedRoute,
    private groupService: InterestGroupService,
    private membersService: MembersService,
    private profileService: ProfileService,
    private translateService: TranslocoService,
    private uiMessageService: UiMessageService,
    private permEvalService: PermissionEvaluatorService,
    private fb: FormBuilder,
    private router: Router,
    private saveAsService: SaveAsService,
    private loginService: LoginService,
    private userService: UserService,
    private i18nPipe: I18nPipe,
    @Optional()
    @Inject(BASE_PATH)
    basePath: string
  ) {
    if (basePath) {
      this.basePath = basePath;
    }
  }

  public async ngOnInit() {
    if (environment.circabcRelease === 'oss') {
      this.isOSS = true;
    }
    this.searchForm = this.fb.group(
      {
        firstName: [''],
        lastName: [''],
        email: [''],
        searchProfile: ['all'],
      },
      {
        updateOn: 'change',
      }
    );

    this.exportForm = this.fb.group(
      {
        export: [this.exportFormats[0]],
      },
      {
        updateOn: 'change',
      }
    );

    this.route.params.subscribe(async (params) => {
      await this.loadMembers(params);
      await this.getCurrentUserMemberships();
    });
  }

  private async getCurrentUserMemberships() {
    if (!this.loginService.isGuest()) {
      const userId =
        this.loginService.getUser().userId !== undefined
          ? this.loginService.getUser().userId
          : 'guest';
      if (userId !== undefined) {
        this.currentUserMemberships = await firstValueFrom(
          this.userService.getUserMembership(userId)
        );
        for (const profile of this.currentUserMemberships) {
          if (
            profile?.interestGroup &&
            this.currentGroup &&
            profile.interestGroup.id === this.currentGroup.id
          ) {
            this.alreadyMember = true;
          }
        }
      }
    }
  }

  private async loadMembers(params: { [key: string]: string }) {
    this.loading = true;
    this.nodeId = params.id;
    if (this.nodeId !== undefined) {
      this.currentGroup = await firstValueFrom(
        this.groupService.getInterestGroup(this.nodeId)
      );
      await this.changePage(this.listingOptions);
      this.availableProfiles = await firstValueFrom(
        this.profileService.getProfiles(this.nodeId)
      );
      this.cleanAvailableProfiles();
    }
    this.loading = false;
  }

  cleanAvailableProfiles() {
    this.availableProfiles = this.availableProfiles.filter((profile) => {
      return profile.name !== 'EVERYONE' && profile.name !== 'guest';
    });
  }

  public async searchUsers() {
    if (this.searchForm.controls.firstName.value !== '') {
      this.firstName = this.searchForm.controls.firstName.value;
    } else {
      this.firstName = '';
    }

    if (this.searchForm.controls.lastName.value !== '') {
      this.lastName = this.searchForm.controls.lastName.value;
    } else {
      this.lastName = '';
    }

    if (this.searchForm.controls.email.value !== '') {
      this.email = this.searchForm.controls.email.value;
    } else {
      this.email = '';
    }

    if (
      this.searchForm.controls.searchProfile.value !== 'all' &&
      this.searchForm.controls.searchProfile.value !== ''
    ) {
      this.searchProfile = this.searchForm.value.searchProfile;
    } else {
      this.searchProfile = '';
    }

    if (this.nodeId !== undefined) {
      const result: PagedUserProfile = await firstValueFrom(
        this.membersService.getMembersFilter(
          this.nodeId,
          this.searchProfile,
          '',
          this.listingOptions.limit,
          this.listingOptions.page,
          this.listingOptions.sort,
          this.firstName,
          this.lastName,
          this.email
        )
      );
      this.members = result.data as UserProfile[];
      this.totalItems = result.total as number;
    }
  }

  public async goToPage(page: number) {
    this.listingOptions.page = page;
    await this.changePage(this.listingOptions);
  }

  public async changePage(listingOptions: ListingOptions) {
    this.loading = true;
    this.listingOptions = listingOptions;
    if (this.nodeId !== undefined) {
      const result: PagedUserProfile = await firstValueFrom(
        this.membersService.getMembersFilter(
          this.nodeId,
          this.searchProfile,
          '',
          this.listingOptions.limit,
          this.listingOptions.page,
          this.listingOptions.sort,
          this.firstName,
          this.lastName,
          this.email
        )
      );
      this.members = result.data as UserProfile[];
      this.selectedUsers = [];
      this.selectedUser = undefined;
      this.totalItems = result.total as number;
    }
    this.loading = false;
  }

  public async changeSort(sort: string) {
    this.listingOptions.sort = changeSort(this.listingOptions.sort, sort);
    await this.changePage(this.listingOptions);
  }

  public async changeLimit(limit: number) {
    this.listingOptions.limit = limit;
    this.listingOptions.page = 1;
    await this.changePage(this.listingOptions);
  }

  public async inviteWizardClosed(result: ActionEmitterResult) {
    if (result !== undefined) {
      if (
        result.result === ActionResult.SUCCEED &&
        result.type === ActionType.ADD_MEMBERSHIPS
      ) {
        const res = this.translateService.translate(
          getSuccessTranslation(ActionType.ADD_MEMBERSHIPS)
        );
        this.uiMessageService.addSuccessMessage(res, true);
        await this.changePage(this.listingOptions);
      } else if (
        result.result === ActionResult.FAILED &&
        result.type === ActionType.ADD_MEMBERSHIPS
      ) {
        const res = this.translateService.translate(
          getErrorTranslation(ActionType.ADD_MEMBERSHIPS)
        );
        this.uiMessageService.addErrorMessage(res, true);
      }
    }

    this.showWizard = false;
  }

  public async createUserWizardClosed(result: ActionEmitterResult) {
    if (result !== undefined) {
      if (
        result.result === ActionResult.SUCCEED &&
        result.type === ActionType.ADD_MEMBERSHIPS
      ) {
        await this.changePage(this.listingOptions);
      }
    }

    this.showUserCreateWizard = false;
  }

  public isDirAdmin(): boolean {
    return (
      this.currentGroup !== undefined &&
      this.permEvalService.isDirAdmin(this.currentGroup)
    );
  }

  public isDirManageMembers(): boolean {
    return (
      this.currentGroup !== undefined &&
      this.permEvalService.isDirManageMembers(this.currentGroup)
    );
  }

  public uninviteUser(user: User | undefined): void {
    if (user === undefined) {
      return;
    }
    this.showUninviteDialog = true;
    this.selectedUser = user;
  }

  public async uninviteWizardClosed(result: ActionEmitterResult) {
    if (result !== undefined) {
      if (
        result.result === ActionResult.SUCCEED &&
        result.type === ActionType.REMOVE_MEMBERSHIP
      ) {
        const res = this.translateService.translate(
          getSuccessTranslation(ActionType.REMOVE_MEMBERSHIP)
        );
        this.uiMessageService.addSuccessMessage(res, true);
        await this.changePage(this.listingOptions);
      } else if (
        result.result === ActionResult.FAILED &&
        result.type === ActionType.REMOVE_MEMBERSHIP
      ) {
        const res = this.translateService.translate(
          getErrorTranslation(ActionType.REMOVE_MEMBERSHIP)
        );
        this.uiMessageService.addErrorMessage(res, true);
      } else if (
        result.result === ActionResult.SUCCEED &&
        result.type === ActionType.REMOVE_MEMBERSHIPS
      ) {
        const res = this.translateService.translate(
          getSuccessTranslation(ActionType.REMOVE_MEMBERSHIPS)
        );
        this.uiMessageService.addSuccessMessage(res, true);
        this.selectedUsers = [];
        this.allSelected = false;
        await this.changePage(this.listingOptions);
      } else if (
        result.result === ActionResult.FAILED &&
        result.type === ActionType.REMOVE_MEMBERSHIPS
      ) {
        const res = this.translateService.translate(
          getErrorTranslation(ActionType.REMOVE_MEMBERSHIPS)
        );
        this.uiMessageService.addErrorMessage(res, true);
      }
    }

    this.showUninviteDialog = false;
    this.showUninviteMultipleDialog = false;
    this.selectedUser = undefined;
  }

  public goToProfiles() {
    // eslint-disable-next-line @typescript-eslint/no-floating-promises
    this.router.navigate(['../profiles'], { relativeTo: this.route });
  }

  public export() {
    this.exporting = true;
    const exportCode: string = this.exportForm.value.export.code;
    const url = this.createUrl(exportCode);
    const name = `Members.${exportCode}`;
    this.saveAsService.saveUrlAs(url, name);
    this.exporting = false;
  }

  private createUrl(exportCode: string) {
    return `${this.basePath}/groups/${this.nodeId}/members/export?profile=${
      this.searchForm.value.searchProfile === 'all'
        ? ''
        : this.searchForm.value.searchProfile
    }&language=&limit=-1&page=1&order=${this.listingOptions.sort}&firstName=${
      this.searchForm.value.firstName
    }&lastName=${this.searchForm.controls.lastName.value}&email=${
      this.searchForm.value.email
    }&format=${exportCode}`;
  }

  public toggleSelectedUser(selectedUser: SelectableUserProfile) {
    this.members.forEach((member) => {
      if (
        member.user?.userId &&
        selectedUser.user &&
        selectedUser.user.userId
      ) {
        if (member.user.userId === selectedUser.user.userId) {
          member.selected = !member.selected;

          if (member.selected) {
            this.selectedUsers.push(member);
          } else {
            const idx = this.selectedUsers.indexOf(member);
            if (idx > -1) {
              this.selectedUsers.splice(idx, 1);
            }
          }
        }
      }
    });
  }

  private prepareMultiple() {
    this.selectedUsers = [];

    this.members.forEach((member) => {
      if (member.user?.userId) {
        if (member.selected) {
          this.selectedUsers.push(member);
        }
      }
    });
  }

  public prepareMultipleDeletion() {
    this.prepareMultiple();
    this.showUninviteMultipleDialog = true;
  }

  public prepareMultipleChangeProfile() {
    this.prepareMultiple();
    this.showMultipleChangeDialog = true;
  }

  public prepareChangeProfile(member: UserProfile) {
    this.selectChangeMember = member;
    this.showChangeDialog = true;
  }

  public async changeModalClosed(res: ActionEmitterResult) {
    if (res.result === ActionResult.CANCELED) {
      this.showChangeDialog = false;
    } else if (res.result === ActionResult.SUCCEED) {
      this.showChangeDialog = false;
      const txt = this.translateService.translate(
        getSuccessTranslation(ActionType.CHANGE_PROFILE)
      );
      this.uiMessageService.addSuccessMessage(txt, true);
      await this.changePage(this.listingOptions);
    } else if (res.result === ActionResult.FAILED) {
      const txt = this.translateService.translate(
        getErrorTranslation(ActionType.CHANGE_PROFILE)
      );
      this.uiMessageService.addErrorMessage(txt, false);
    }
  }

  public async multipleChangeModalClosed(res: ActionEmitterResult) {
    if (res.result === ActionResult.CANCELED) {
      this.showMultipleChangeDialog = false;
    } else if (res.result === ActionResult.SUCCEED) {
      this.showMultipleChangeDialog = false;
      this.selectedUsers = [];
      this.allSelected = false;
      await this.changePage(this.listingOptions);
    } else if (res.result === ActionResult.FAILED) {
      const txt = this.translateService.translate(
        getErrorTranslation(ActionType.CHANGE_PROFILES)
      );
      this.uiMessageService.addErrorMessage(txt, false);
    }
  }

  isMember() {
    return this.alreadyMember;
  }

  isConnectedUser(userId: string | undefined) {
    if (userId === undefined) {
      return false;
    }
    if (!this.loginService.isGuest()) {
      return this.loginService.getCurrentUsername() === userId;
    }

    return false;
  }

  selectAll() {
    if (this.allSelected) {
      this.allSelected = false;
      this.selectedUsers = [];
      this.members.forEach((member) => {
        member.selected = false;
      });
    } else {
      this.allSelected = true;
      const currentUserName = this.loginService.getCurrentUsername();
      this.members.forEach((member) => {
        if (member.user?.userId && currentUserName !== member.user.userId) {
          member.selected = true;
          this.selectedUsers.push(member);
        }
      });
    }
  }

  public async resetSearch() {
    this.searchForm.patchValue({
      searchProfile: 'all',
      firstName: '',
      lastName: '',
      email: '',
    });
    this.firstName = '';
    this.lastName = '';
    this.email = '';
    this.searchProfile = '';
    await this.searchUsers();
  }

  public getProfileNameOrTitle(
    profile: Profile | undefined
  ): string | undefined {
    if (profile === undefined) {
      return '';
    }
    let result = profile.name;

    if (profile.title) {
      const title = this.i18nPipe.transform(profile.title);
      if (title !== '' && title !== undefined) {
        result = title;
      }
    }

    return result;
  }

  public async refreshUsers() {
    await this.changePage(this.listingOptions);
  }

  public prepareChangeExpiration(member: SelectableUserProfile) {
    this.selectedExpiredUsers = [member];
    this.selectedExpiredUsers = [...this.selectedExpiredUsers];
    this.showExpirationDialog = true;
  }

  public prepareMultipleSetExpiration() {
    if (this.selectedUsers.length > 0) {
      this.selectedExpiredUsers = this.selectedUsers;
      this.selectedExpiredUsers = [...this.selectedExpiredUsers];
      this.showExpirationDialog = true;
    }
  }

  public async expirationModalClosed(res: ActionEmitterResult) {
    if (res.result === ActionResult.CANCELED) {
      this.showExpirationDialog = false;
    } else if (res.result === ActionResult.SUCCEED) {
      this.showExpirationDialog = false;
      this.selectedUsers = [];
      this.allSelected = false;
      const txt = this.translateService.translate(
        getSuccessTranslation(ActionType.EDIT_EXPIRATION)
      );
      this.uiMessageService.addSuccessMessage(txt, true);
      this.refreshUsers();
    } else if (res.result === ActionResult.FAILED) {
      this.showExpirationDialog = false;
      const txt = this.translateService.translate(
        getErrorTranslation(ActionType.EDIT_EXPIRATION)
      );
      this.uiMessageService.addErrorMessage(txt, false);
      this.refreshUsers();
    }
  }

  public trackMember(_index: number, item: SelectableUserProfile) {
    return `${item.user?.userId}-${item.profile?.id}`;
  }

  public filterEnterEvent(event: KeyboardEvent) {
    if (event.key === 'Enter') {
      this.searchUsers();
    }
  }
}
