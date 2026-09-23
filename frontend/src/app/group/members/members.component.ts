import { DatePipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  linkedSignal,
  resource,
  signal,
} from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import {
  BASE_PATH,
  InterestGroupService,
  MembersService,
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
import { ChangeProfilesMultipleComponent } from './change-profiles-multiple/change-profiles-multiple.component';
import { ChangeUserProfileComponent } from './change-user-profile/change-user-profile.component';
import { EditExpirationComponent } from './edit-expiration/edit-expiration.component';
import { InviteUserComponent } from './invite-user/invite-user.component';
import { MembersDropdownComponent } from './members-dropdown.component';
import { UninviteUserComponent } from './uninvite/uninvite-user.component';
import { UninviteMultipleComponent } from './uninvite-multiple/uninvite-multiple.component';
/**
 * Standalone Angular component (`cbc-members`) that renders the member
 * management view for an interest group.
 *
 * It displays a paginated, sortable and searchable list of the group's
 * members together with their assigned access profile, and orchestrates the
 * various member-related workflows through embedded dialog/wizard child
 * components:
 * - inviting existing users ({@link InviteUserComponent}) or creating and
 *   inviting new users ({@link CreateUserComponent}),
 * - un-inviting a single member ({@link UninviteUserComponent}) or several at
 *   once ({@link UninviteMultipleComponent}),
 * - changing the access profile of one ({@link ChangeUserProfileComponent}) or
 *   many ({@link ChangeProfilesMultipleComponent}) members,
 * - editing membership expiration dates ({@link EditExpirationComponent}),
 * - exporting the member list (CSV / XML / Excel).
 *
 * Data is fetched from the CIRCABC backend through {@link InterestGroupService},
 * {@link MembersService}, {@link ProfileService} and {@link UserService}.
 * Permission-gated actions rely on {@link PermissionEvaluatorService}, and user
 * feedback is surfaced via {@link UiMessageService} with translated messages
 * from {@link TranslocoService}.
 *
 * The current group is resolved from the `id` route parameter.
 */
@Component({
  selector: 'cbc-members',
  templateUrl: './members.component.html',
  styleUrl: './members.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
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
export class MembersComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly groupService = inject(InterestGroupService);
  private readonly membersService = inject(MembersService);
  private readonly profileService = inject(ProfileService);
  private readonly translateService = inject(TranslocoService);
  private readonly uiMessageService = inject(UiMessageService);
  private readonly permEvalService = inject(PermissionEvaluatorService);
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly saveAsService = inject(SaveAsService);
  private readonly loginService = inject(LoginService);
  private readonly userService = inject(UserService);
  private readonly i18nPipe = inject(I18nPipe);

  /** Member currently targeted by the single-member profile change dialog. */
  public selectChangeMember: UserProfile = { profile: {} } as UserProfile;
  /** Identifier of the interest group (resolved from the `id` route param). */
  private readonly nodeIdSignal = signal<string | undefined>(undefined);
  /** Whether the advanced search box is expanded in the UI. */
  public showSearchBox = false;
  /** Whether the "invite existing user" wizard is visible. */
  public showWizard = false;
  /** Whether the "create and invite new user" wizard is visible. */
  public showUserCreateWizard = false;
  /** Whether the single-member profile change dialog is visible. */
  public showChangeDialog = false;
  /** Whether the single-member un-invite confirmation dialog is visible. */
  public showUninviteDialog = false;
  /** Whether the multi-member un-invite confirmation dialog is visible. */
  public showUninviteMultipleDialog = false;
  /** Whether the multi-member profile change dialog is visible. */
  public showMultipleChangeDialog = false;
  /** Whether the membership expiration edit dialog is visible. */
  public showExpirationDialog = false;
  /** User selected for a single-member action (e.g. un-invite). */
  public selectedUser: User | undefined;
  /** Members currently selected via checkboxes for bulk actions. */
  public selectedUsers: SelectableUserProfile[] = [];
  /** Members targeted by the expiration edit dialog. */
  public selectedExpiredUsers: SelectableUserProfile[] = [];
  /** Paging/sorting signal driving the member listing requests. */
  private readonly listingOptionsSignal = signal<ListingOptions>({
    page: 1,
    limit: 10,
    sort: 'lastName_ASC',
  });
  /** Reactive form holding the member search criteria. */
  public searchForm: FormGroup = this.fb.group(
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
  /** Whether the connected user is already a member of the current group. */
  public alreadyMember = computed(() => {
    const currentGroup = this.currentGroup();
    const memberships = this.membershipsResource.hasValue()
      ? this.membershipsResource.value()
      : undefined;
    if (!(currentGroup && memberships)) {
      return false;
    }
    return memberships.some(
      (profile) =>
        profile?.interestGroup && profile.interestGroup.id === currentGroup.id
    );
  });
  // properties for the exporter (format to export the file and the file id)
  /** Supported export formats offered in the export form. */
  public exportFormats = [
    { code: 'csv', name: 'CSV' },
    { code: 'xml', name: 'XML' },
    { code: 'xls', name: 'Excel' },
  ];
  /** Reactive form holding the selected export format. */
  public exportForm: FormGroup = this.fb.group(
    {
      export: [this.exportFormats[0]],
    },
    {
      updateOn: 'change',
    }
  );
  /** Whether all eligible members on the current page are selected. */
  public allSelected = false;
  /** Whether an export operation is currently running. */
  public exporting = false;
  /** Whether the application runs in the open-source (OSS) release. */
  public isOSS = environment.circabcRelease === 'oss';
  /** First-name search filter currently applied to the listing. */
  private readonly firstNameFilter = signal('');
  /** Last-name search filter currently applied to the listing. */
  private readonly lastNameFilter = signal('');
  /** Email search filter currently applied to the listing. */
  private readonly emailFilter = signal('');
  /** Profile-id search filter currently applied to the listing. */
  private readonly searchProfileFilter = signal('');

  /** Backend API base path injected from {@link BASE_PATH}. */
  private readonly basePath!: string;

  /** The interest group whose members are being managed. */
  private readonly groupResource = resource({
    params: () => this.nodeIdSignal() || undefined,
    loader: async ({ params: id }) => {
      try {
        return await this.groupService.getInterestGroupAsync({ id });
      } catch (e) {
        console.error(e);
        return undefined;
      }
    },
  });
  /** The interest group whose members are being managed. */
  public readonly currentGroup = computed(() =>
    this.groupResource.hasValue() ? this.groupResource.value() : undefined
  );

  /** Access profiles fetched from the backend for the current group. */
  private readonly profilesResource = resource({
    params: () => this.nodeIdSignal() || undefined,
    loader: async ({ params: id }) => {
      try {
        return await this.profileService.getProfilesAsync({ id });
      } catch (e) {
        console.error(e);
        return [];
      }
    },
  });
  /** Access profiles available in the group (excluding EVERYONE/guest). */
  public readonly availableProfiles = computed(() =>
    (this.profilesResource.value() ?? []).filter(
      (profile) => profile.name !== 'EVERYONE' && profile.name !== 'guest'
    )
  );

  /** Paginated/filtered members matching the active listing options. */
  private readonly membersResource = resource({
    params: () => {
      const id = this.nodeIdSignal();
      if (id === undefined) {
        return undefined;
      }
      const listingOptions = this.listingOptionsSignal();
      return {
        id,
        profile: this.searchProfileFilter(),
        limit: listingOptions.limit,
        page: listingOptions.page,
        order: listingOptions.sort,
        firstName: this.firstNameFilter(),
        lastName: this.lastNameFilter(),
        email: this.emailFilter(),
      };
    },
    loader: async ({ params }) => {
      try {
        return await this.membersService.getMembersFilterAsync({
          ...params,
          language: '',
        });
      } catch (e) {
        console.error(e);
        return { data: [], total: 0 };
      }
    },
  });
  /** Members displayed on the current page, augmented with a `selected` flag. */
  public readonly members = linkedSignal<SelectableUserProfile[]>(
    () => (this.membersResource.value()?.data as SelectableUserProfile[]) ?? []
  );
  /** Total number of members matching the current filter (for the pager). */
  public readonly totalItems = computed(
    () => this.membersResource.value()?.total ?? 10
  );

  /** Group memberships of the connected user, used to derive membership state. */
  private readonly membershipsResource = resource({
    params: () => {
      if (this.loginService.isGuest()) {
        return undefined;
      }
      const userId = this.loginService.getUser().userId;
      return userId ?? 'guest';
    },
    loader: async ({ params: userId }) => {
      try {
        return await this.userService.getUserMembershipAsync({ userId });
      } catch (e) {
        console.error(e);
        return [];
      }
    },
  });

  /** Whether an asynchronous listing operation is in progress. */
  public readonly loading = computed(
    () =>
      this.groupResource.isLoading() ||
      this.membersResource.isLoading() ||
      this.profilesResource.isLoading()
  );

  /** Paging/sorting options driving the member listing requests (read model). */
  public get listingOptions(): ListingOptions {
    return this.listingOptionsSignal();
  }

  /**
   * Reads the injected {@link BASE_PATH} token and stores it in
   * {@link basePath} for later construction of the export URL, and
   * subscribes to route parameter changes to drive the group/members
   * resources.
   */
  constructor() {
    const basePath = inject(BASE_PATH);

    if (basePath) {
      this.basePath = basePath;
    }

    this.route.params.subscribe((params) => {
      this.nodeIdSignal.set(params.id);
    });
  }

  /**
   * Identifier of the interest group (resolved from the `id` route param).
   *
   * @returns The current group id, or `undefined` before it is resolved.
   */
  public get nodeId(): string {
    return this.nodeIdSignal() as string;
  }

  /**
   * Reads the current search-form values (first name, last name, email and
   * profile), applies them as the active listing filters. The
   * {@link membersResource} reactively reloads {@link members} and
   * {@link totalItems}.
   */
  public searchUsers() {
    this.firstNameFilter.set(this.searchForm.controls.firstName.value);
    this.lastNameFilter.set(this.searchForm.controls.lastName.value);
    this.emailFilter.set(this.searchForm.controls.email.value);

    if (
      this.searchForm.controls.searchProfile.value !== 'all' &&
      this.searchForm.controls.searchProfile.value !== ''
    ) {
      this.searchProfileFilter.set(this.searchForm.value.searchProfile);
    } else {
      this.searchProfileFilter.set('');
    }
  }

  /**
   * Navigates the member listing to the given page.
   *
   * @param page The 1-based page number to display.
   */
  public goToPage(page: number) {
    this.listingOptionsSignal.update((options) => ({ ...options, page }));
    this.selectedUsers = [];
    this.selectedUser = undefined;
  }

  /**
   * Toggles/updates the sort order for the given column and reloads the
   * listing.
   *
   * @param sort The sort key/column to apply (e.g. `lastName`).
   */
  public changeSort(sort: string) {
    this.listingOptionsSignal.update((options) => ({
      ...options,
      sort: changeSort(options.sort, sort),
    }));
    this.selectedUsers = [];
    this.selectedUser = undefined;
  }

  /**
   * Changes the number of members displayed per page, resets to the first
   * page and reloads the listing.
   *
   * @param limit The new page size.
   */
  public changeLimit(limit: number) {
    this.listingOptionsSignal.update((options) => ({
      ...options,
      limit,
      page: 1,
    }));
    this.selectedUsers = [];
    this.selectedUser = undefined;
  }

  /**
   * Callback invoked when the invite-existing-user wizard closes. On a
   * successful add-memberships result it shows a success message and reloads
   * the listing; on failure it shows an error message. Always hides the
   * wizard.
   *
   * @param result The outcome emitted by the invite wizard.
   */
  public inviteWizardClosed(result: ActionEmitterResult) {
    if (result !== undefined) {
      if (
        result.result === ActionResult.SUCCEED &&
        result.type === ActionType.ADD_MEMBERSHIPS
      ) {
        const res = this.translateService.translate(
          getSuccessTranslation(ActionType.ADD_MEMBERSHIPS)
        );
        this.uiMessageService.addSuccessMessage(res, true);
        this.membersResource.reload();
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

  /**
   * Callback invoked when the create-and-invite-user wizard closes. Reloads
   * the listing on a successful add-memberships result and hides the wizard.
   *
   * @param result The outcome emitted by the create-user wizard.
   */
  public createUserWizardClosed(result: ActionEmitterResult) {
    if (result !== undefined) {
      if (
        result.result === ActionResult.SUCCEED &&
        result.type === ActionType.ADD_MEMBERSHIPS
      ) {
        this.membersResource.reload();
      }
    }

    this.showUserCreateWizard = false;
  }

  /**
   * Indicates whether the connected user is a directory administrator of the
   * current group.
   *
   * @returns `true` if a current group exists and the user is a directory
   * admin, otherwise `false`.
   */
  public isDirAdmin(): boolean {
    const currentGroup = this.currentGroup();
    return (
      currentGroup !== undefined &&
      this.permEvalService.isDirAdmin(currentGroup)
    );
  }

  /**
   * Indicates whether the connected user is allowed to manage the members of
   * the current group.
   *
   * @returns `true` if a current group exists and the user can manage its
   * members, otherwise `false`.
   */
  public isDirManageMembers(): boolean {
    const currentGroup = this.currentGroup();
    return (
      currentGroup !== undefined &&
      this.permEvalService.isDirManageMembers(currentGroup)
    );
  }

  /**
   * Opens the single-member un-invite confirmation dialog for the given user.
   *
   * @param user The user to un-invite; when `undefined` the call is ignored.
   */
  public uninviteUser(user: User | undefined): void {
    if (user === undefined) {
      return;
    }
    this.showUninviteDialog = true;
    this.selectedUser = user;
  }

  /**
   * Callback invoked when either the single or multiple un-invite dialog
   * closes. Displays the appropriate success/error message for the removal,
   * clears selections on bulk success, reloads the listing and hides both
   * un-invite dialogs.
   *
   * @param result The outcome emitted by the un-invite dialog.
   */
  public uninviteWizardClosed(result: ActionEmitterResult) {
    if (result !== undefined) {
      if (
        result.result === ActionResult.SUCCEED &&
        result.type === ActionType.REMOVE_MEMBERSHIP
      ) {
        const res = this.translateService.translate(
          getSuccessTranslation(ActionType.REMOVE_MEMBERSHIP)
        );
        this.uiMessageService.addSuccessMessage(res, true);
        this.membersResource.reload();
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
        this.membersResource.reload();
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

  /**
   * Navigates to the sibling "profiles" route relative to the current route.
   */
  public goToProfiles() {
    this.router.navigate(['../profiles'], { relativeTo: this.route });
  }

  /**
   * Triggers a download of the member list in the format selected in the
   * export form, using the URL built by {@link createUrl}. Toggles
   * {@link exporting} around the operation.
   */
  public export() {
    this.exporting = true;
    const exportCode: string = this.exportForm.value.export.code;
    const url = this.createUrl(exportCode);
    const name = `Members.${exportCode}`;
    this.saveAsService.saveUrlAs(url, name);
    this.exporting = false;
  }

  /**
   * Builds the backend export URL for the current group, embedding the active
   * search filters, sort order and requested format.
   *
   * @param exportCode The export format code (e.g. `csv`, `xml`, `xls`).
   * @returns The fully qualified members export URL.
   */
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

  /**
   * Toggles the selection state of the given member and keeps
   * {@link selectedUsers} in sync by adding or removing it.
   *
   * @param selectedUser The member whose selection should be toggled.
   */
  public toggleSelectedUser(selectedUser: SelectableUserProfile) {
    this.members().forEach((member) => {
      if (member.user?.userId && selectedUser.user?.userId) {
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

  /**
   * Rebuilds {@link selectedUsers} from the currently displayed members that
   * are flagged as selected.
   */
  private prepareMultiple() {
    this.selectedUsers = [];

    this.members().forEach((member) => {
      if (member.user?.userId) {
        if (member.selected) {
          this.selectedUsers.push(member);
        }
      }
    });
  }

  /**
   * Collects the selected members and opens the multi-member un-invite dialog.
   */
  public prepareMultipleDeletion() {
    this.prepareMultiple();
    this.showUninviteMultipleDialog = true;
  }

  /**
   * Collects the selected members and opens the multi-member profile change
   * dialog.
   */
  public prepareMultipleChangeProfile() {
    this.prepareMultiple();
    this.showMultipleChangeDialog = true;
  }

  /**
   * Opens the single-member profile change dialog for the given member.
   *
   * @param member The member whose profile should be changed.
   */
  public prepareChangeProfile(member: UserProfile) {
    this.selectChangeMember = member;
    this.showChangeDialog = true;
  }

  /**
   * Callback invoked when the single-member profile change dialog closes.
   * Handles the cancelled, succeeded and failed outcomes by showing the
   * relevant message and reloading the listing on success.
   *
   * @param res The outcome emitted by the change-profile dialog.
   */
  public changeModalClosed(res: ActionEmitterResult) {
    if (res.result === ActionResult.CANCELED) {
      this.showChangeDialog = false;
    } else if (res.result === ActionResult.SUCCEED) {
      this.showChangeDialog = false;
      const txt = this.translateService.translate(
        getSuccessTranslation(ActionType.CHANGE_PROFILE)
      );
      this.uiMessageService.addSuccessMessage(txt, true);
      this.membersResource.reload();
    } else if (res.result === ActionResult.FAILED) {
      const txt = this.translateService.translate(
        getErrorTranslation(ActionType.CHANGE_PROFILE)
      );
      this.uiMessageService.addErrorMessage(txt, false);
    }
  }

  /**
   * Callback invoked when the multi-member profile change dialog closes.
   * Clears the selection and reloads the listing on success, or shows an
   * error message on failure. Always hides the dialog.
   *
   * @param res The outcome emitted by the multiple change-profile dialog.
   */
  public multipleChangeModalClosed(res: ActionEmitterResult) {
    this.showMultipleChangeDialog = false;
    if (res.result === ActionResult.SUCCEED) {
      this.showMultipleChangeDialog = false;
      this.selectedUsers = [];
      this.allSelected = false;
      this.membersResource.reload();
    } else if (res.result === ActionResult.FAILED) {
      const txt = this.translateService.translate(
        getErrorTranslation(ActionType.CHANGE_PROFILES)
      );
      this.uiMessageService.addErrorMessage(txt, false);
    }
  }

  /**
   * Indicates whether the connected user is already a member of the group.
   *
   * @returns The current value of {@link alreadyMember}.
   */
  isMember() {
    return this.alreadyMember();
  }

  /**
   * Determines whether the given user id corresponds to the connected user.
   *
   * @param userId The user id to compare against the connected user.
   * @returns `true` if it matches the connected (non-guest) user, otherwise
   * `false`.
   */
  isConnectedUser(userId: string | undefined) {
    if (userId === undefined) {
      return false;
    }
    if (!this.loginService.isGuest()) {
      return this.loginService.getCurrentUsername() === userId;
    }

    return false;
  }

  /**
   * Toggles bulk selection of all eligible members on the current page. When
   * enabling, every member except the connected user is selected; when
   * disabling, the selection is cleared.
   */
  selectAll() {
    if (this.allSelected) {
      this.allSelected = false;
      this.selectedUsers = [];
      this.members().forEach((member) => {
        member.selected = false;
      });
    } else {
      this.allSelected = true;
      const currentUserName = this.loginService.getCurrentUsername();
      this.members().forEach((member) => {
        if (member.user?.userId && currentUserName !== member.user.userId) {
          member.selected = true;
          this.selectedUsers.push(member);
        }
      });
    }
  }

  /**
   * Clears the search form and the active search filters, then reruns the
   * search to display the unfiltered member list.
   */
  public resetSearch() {
    this.searchForm.patchValue({
      searchProfile: 'all',
      firstName: '',
      lastName: '',
      email: '',
    });
    this.firstNameFilter.set('');
    this.lastNameFilter.set('');
    this.emailFilter.set('');
    this.searchProfileFilter.set('');
    this.searchUsers();
  }

  /**
   * Resolves the display label for a profile, preferring its localized title
   * (via {@link I18nPipe}) and falling back to its name.
   *
   * @param profile The profile to derive a label for.
   * @returns The localized title, the profile name, or an empty string when
   * the profile is `undefined`.
   */
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

  /**
   * Reloads the current member listing page (e.g. after an external change).
   */
  public refreshUsers() {
    this.membersResource.reload();
  }

  /**
   * Opens the expiration edit dialog for a single member.
   *
   * @param member The member whose membership expiration should be edited.
   */
  public prepareChangeExpiration(member: SelectableUserProfile) {
    this.selectedExpiredUsers = [member];
    this.selectedExpiredUsers = [...this.selectedExpiredUsers];
    this.showExpirationDialog = true;
  }

  /**
   * Opens the expiration edit dialog for all currently selected members, when
   * at least one member is selected.
   */
  public prepareMultipleSetExpiration() {
    if (this.selectedUsers.length > 0) {
      this.selectedExpiredUsers = this.selectedUsers;
      this.selectedExpiredUsers = [...this.selectedExpiredUsers];
      this.showExpirationDialog = true;
    }
  }

  /**
   * Callback invoked when the expiration edit dialog closes. Shows a
   * success/error message, clears the selection on success and refreshes the
   * listing in both cases. Always hides the dialog.
   *
   * @param res The outcome emitted by the expiration edit dialog.
   */
  public expirationModalClosed(res: ActionEmitterResult) {
    this.showExpirationDialog = false;
    if (res.result === ActionResult.SUCCEED) {
      this.selectedUsers = [];
      this.allSelected = false;
      const txt = this.translateService.translate(
        getSuccessTranslation(ActionType.EDIT_EXPIRATION)
      );
      this.uiMessageService.addSuccessMessage(txt, true);
      this.refreshUsers();
    } else if (res.result === ActionResult.FAILED) {
      const txt = this.translateService.translate(
        getErrorTranslation(ActionType.EDIT_EXPIRATION)
      );
      this.uiMessageService.addErrorMessage(txt, false);
      this.refreshUsers();
    }
  }

  /**
   * `trackBy` function for the members list, providing a stable identity per
   * member based on user id and profile id.
   *
   * @param _index The index of the item in the list (unused).
   * @param item The member being tracked.
   * @returns A unique tracking key of the form `<userId>-<profileId>`.
   */
  public trackMember(_index: number, item: SelectableUserProfile) {
    return `${item.user?.userId}-${item.profile?.id}`;
  }

  /**
   * Keyboard handler that triggers a member search when the Enter key is
   * pressed in the search inputs.
   *
   * @param event The keyboard event to inspect.
   */
  public filterEnterEvent(event: KeyboardEvent) {
    if (event.key === 'Enter') {
      this.searchUsers();
    }
  }
}
