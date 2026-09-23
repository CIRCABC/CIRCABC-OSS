import { HttpErrorResponse } from '@angular/common/http';
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
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import {
  BASE_PATH,
  BulkInviteData,
  IGData,
  InterestGroupProfile,
  InterestGroupService,
  NameValue,
  Profile,
  ProfileService,
  UserService,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { SaveAsService } from 'app/core/save-as.service';
import { SelectableBulkImportUserData } from 'app/core/ui-model';
import { truncate } from 'app/core/util';
import { ListingOptions } from 'app/group/listing-options/listing-options';
import { InlineDeleteComponent } from 'app/shared/delete/inline-delete.component';
import { ItemMultiselectorComponent } from 'app/shared/item-multiselector/item-multiselector.component';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { NumberBadgeComponent } from 'app/shared/number-badge/number-badge.component';
import { PagerComponent } from 'app/shared/pager/pager.component';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { ReponsiveSubMenuComponent } from 'app/shared/reponsive-sub-menu/reponsive-sub-menu.component';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';

/**
 * Standalone Angular component (`cbc-bulk-invite`) that drives the bulk
 * invitation workflow for an interest group's members area.
 *
 * The component renders a form that lets an administrator build a list of
 * candidate users to invite into the current interest group. Candidates can
 * be gathered from two sources:
 * - existing members of other interest groups (selected by category and
 *   group), and/or
 * - an uploaded spreadsheet file (an `.xls` template that is parsed by the
 *   backend).
 *
 * The collected candidates are shown in a paginated, selectable table where
 * each candidate can be assigned an access profile. The administrator can
 * then trigger the invitation, optionally creating new profiles and/or
 * notifying the invited users.
 *
 * Key collaborators:
 * - {@link UserService} — fetches invite categories/groups/members, parses
 *   the uploaded file and performs the actual bulk invitation.
 * - {@link ProfileService} — loads the profiles available in the group.
 * - {@link InterestGroupService} — loads the current interest group.
 * - {@link PermissionEvaluatorService} — evaluates the current user's admin
 *   rights on the group.
 * - {@link LoginService} — resolves the current user and guest state.
 * - {@link SaveAsService} — downloads the import template file.
 * - {@link UiMessageService} / {@link TranslocoService} — surface parsing
 *   errors as localized UI messages.
 */
@Component({
  selector: 'cbc-bulk-invite',
  templateUrl: './bulk-invite.component.html',
  styleUrl: './bulk-invite.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    HorizontalLoaderComponent,
    ReponsiveSubMenuComponent,
    RouterLink,
    ReactiveFormsModule,
    ItemMultiselectorComponent,
    PagerComponent,
    NumberBadgeComponent,
    InlineDeleteComponent,
    SpinnerComponent,
    TranslocoModule,
  ],
})
export class BulkInviteComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly userService = inject(UserService);
  private readonly profileService = inject(ProfileService);
  private readonly interestGroupService = inject(InterestGroupService);
  private readonly permEvalService = inject(PermissionEvaluatorService);
  private readonly loginService = inject(LoginService);
  private readonly formBuilder = inject(FormBuilder);
  private readonly saveAsService = inject(SaveAsService);
  private readonly translateService = inject(TranslocoService);
  private readonly uiMessageService = inject(UiMessageService);
  private readonly router = inject(Router);
  private readonly i18nPipe = inject(I18nPipe);

  /** Identifier of the interest group users are being invited into, driven by the route param. */
  public readonly igId = signal('');

  /** Reactive form holding the `createNewProfiles` and `notifyUsers` flags. */
  public bulkInviteForm!: FormGroup;
  /**
   * True while an invitation request is being submitted to the backend.
   * Signal-backed so writes from the async `invite()` continuation mark the
   * OnPush view dirty.
   */
  public readonly processing = signal(false);

  /** The currently logged-in user. */
  private readonly user = this.loginService.getUser();

  /**
   * Loads the current group's bulk-invite setup: the invite categories the
   * current user may pull candidates from, the target interest group, the
   * current user's memberships (to detect {@link alreadyMember}) and the
   * group's profiles. Idle while {@link igId} is empty.
   */
  private readonly initResource = resource({
    params: () => this.igId() || undefined,
    loader: async ({ params: igId }) => {
      const [availableCategories, currentGroup, profiles] = await Promise.all([
        this.userService.getBulkInviteCategoriesAsync({
          username: this.user.userId as string,
        }),
        this.interestGroupService.getInterestGroupAsync({ id: igId }),
        this.profileService.getProfilesAsync({ id: igId }),
      ]);

      let alreadyMember = false;
      let currentUserMemberships: InterestGroupProfile[] = [];
      if (!this.loginService.isGuest()) {
        const userId = this.user.userId ?? 'guest';
        currentUserMemberships = await this.userService.getUserMembershipAsync({
          userId,
        });
        alreadyMember = currentUserMemberships.some(
          (profile) =>
            profile?.interestGroup &&
            profile.interestGroup.id === currentGroup.id
        );
      }

      const availableProfiles = profiles.filter(
        (profile: Profile) =>
          profile.name !== undefined &&
          profile.name !== 'guest' &&
          profile.name !== 'EVERYONE' &&
          !profile.name.includes(':')
      );
      moveAccessToTop(availableProfiles);

      return {
        availableCategories,
        currentGroup,
        currentUserMemberships,
        alreadyMember,
        profiles,
        availableProfiles,
      };
    },
  });

  /** Categories the current user may pull invite candidates from. */
  public readonly availableCategories = computed(
    () => this.initResource.value()?.availableCategories ?? []
  );
  /** The interest group the users are being invited into. */
  public readonly currentGroup = computed(
    () => this.initResource.value()?.currentGroup
  );
  /** The current user's memberships across all interest groups. */
  public readonly currentUserMemberships = computed(
    () => this.initResource.value()?.currentUserMemberships ?? []
  );
  /** True if the current user is already a member of {@link currentGroup}. */
  public readonly alreadyMember = computed(
    () => this.initResource.value()?.alreadyMember ?? false
  );
  /** All profiles defined in the current interest group. */
  public readonly profiles = computed(
    () => this.initResource.value()?.profiles ?? []
  );
  /**
   * Assignable profiles (excludes guest/EVERYONE and system profiles), with
   * the `Access` profile moved to the top. Locally writable so it can be
   * reordered again once members load from the file digest.
   */
  public readonly availableProfiles = linkedSignal(
    () => this.initResource.value()?.availableProfiles ?? []
  );

  /** Category currently selected as the source of candidate groups. */
  public readonly selectedCategory = linkedSignal(
    () => this.availableCategories()[0]
  );
  /** Source interest groups whose members should be gathered as candidates. */
  public readonly selectedInterestGroups = signal<NameValue[]>([]);

  /**
   * Loads the interest groups belonging to {@link selectedCategory} (excluding
   * the current group), mapped as name/value pairs with a truncated
   * category-prefixed label. Idle while there is no selected category.
   */
  private readonly igsResource = resource({
    params: () => {
      const category = this.selectedCategory();
      const igId = this.igId();
      return category && igId ? { category, igId } : undefined;
    },
    loader: async ({ params: { category, igId } }) => {
      const igs = await this.userService.getBulkInviteIGsAsync({
        categoryId: category.id as string,
        igId,
      });
      return igs.map((igData: IGData) => ({
        name: `${truncate(category.name, 15)} / ${igData.igName}`,
        value: igData.igId,
      }));
    },
  });

  /** Interest groups (as name/value pairs) available under the selected category. */
  public readonly availableInterestGroups = computed(
    () => this.igsResource.value() ?? []
  );

  /** Spreadsheet file uploaded to import candidate users, if any. */
  public readonly fileToUpload = signal<File | undefined>(undefined);

  /** Current pagination/sort state for the candidate members table. */
  public readonly listingOptions = signal<ListingOptions>({
    page: 1,
    limit: 10,
    sort: '',
  });

  /**
   * Loads and combines the full candidate list from the selected source
   * groups and the uploaded file. Local mutations (delete, select, profile
   * assignment) are applied by writing to {@link membersResource}'s `value`
   * directly rather than reloading.
   */
  private readonly membersResource = resource({
    params: () => ({
      igId: this.igId(),
      selectedInterestGroups: this.selectedInterestGroups(),
      fileToUpload: this.fileToUpload(),
    }),
    loader: async ({
      params: { igId, selectedInterestGroups, fileToUpload },
    }) => {
      let allMembers: SelectableBulkImportUserData[] =
        await this.loadMembersFromGroups(igId, selectedInterestGroups);

      allMembers = await this.loadMembersFromFile(
        igId,
        fileToUpload,
        allMembers
      );

      this.listingOptions.update((options) => ({ ...options, page: 1 }));
      return allMembers;
    },
  });

  /** Full, unpaginated list of gathered candidate members. */
  public readonly allMembers = computed(
    () => this.membersResource.value() ?? []
  );
  /** Slice of {@link allMembers} currently displayed on the active page. */
  public readonly members = computed(() => {
    const { page, limit } = this.listingOptions();
    const allMembers = this.allMembers();
    const effectiveLimit = limit === -1 ? allMembers.length : limit;
    const startItem = (page - 1) * effectiveLimit;
    const endItem = Math.min(startItem + effectiveLimit, allMembers.length);
    return allMembers.slice(startItem, endItem);
  });
  /** Total number of candidate members (used to drive the pager). */
  public readonly totalItems = computed(() => {
    const allMembers = this.allMembers();
    return allMembers.length > 0
      ? allMembers.length
      : this.listingOptions().limit;
  });

  /** Whether the "select all" toggle is currently active. */
  public allSelected = false;
  /** Candidate members currently selected via checkboxes. */
  public selectedMembers: SelectableBulkImportUserData[] = [];

  /** True while asynchronous data loading is in progress. */
  public readonly loading = computed(
    () =>
      this.initResource.isLoading() ||
      this.igsResource.isLoading() ||
      this.membersResource.isLoading()
  );

  /** API base path, injected from {@link BASE_PATH}, used to build download URLs. */
  private readonly basePath!: string;

  /**
   * Resolves the API {@link BASE_PATH} token and stores it in
   * {@link basePath} when available, builds the reactive form and subscribes
   * to route params so the component reloads whenever the `igId` route param
   * changes.
   */
  constructor() {
    const basePath = inject(BASE_PATH);

    if (basePath) {
      this.basePath = basePath;
    }

    this.buildForm();
    this.route.params.subscribe((params) => {
      this.igId.set(params.igId);
    });
  }

  /** Initializes {@link bulkInviteForm} with its default control values. */
  public buildForm(): void {
    this.bulkInviteForm = this.formBuilder.group({
      createNewProfiles: [false],
      notifyUsers: [false],
    });
  }

  /**
   * Loads candidate members from the selected source interest groups, or
   * returns an empty list when no source group is selected.
   */
  private async loadMembersFromGroups(
    igId: string,
    selectedInterestGroups: NameValue[]
  ): Promise<SelectableBulkImportUserData[]> {
    if (selectedInterestGroups?.length) {
      return await this.userService.getBulkInviteMembersAsync({
        igIds: selectedInterestGroups.map((item) => item.value as string),
        destinationIGId: igId,
      });
    }
    return [];
  }

  /**
   * Sends the uploaded file to the backend for parsing and appends the
   * returned candidates to the given list, resolving each candidate's
   * profile. On failure it surfaces a localized error and clears the file.
   */
  private async loadMembersFromFile(
    igId: string,
    fileToUpload: File | undefined,
    allMembers: SelectableBulkImportUserData[]
  ): Promise<SelectableBulkImportUserData[]> {
    if (!fileToUpload) return allMembers;

    try {
      const membersFromFile =
        await this.userService.bulkInviteUsersDigestFileAsync({
          igId,
          fileData: fileToUpload,
        });

      const result = [...allMembers];
      for (const member of membersFromFile) {
        this.extractProfile(member);
        result.push(member);
      }
      return result;
    } catch (error) {
      this.handleFileError(error as HttpErrorResponse);
      this.fileToUpload.set(undefined);
      return allMembers;
    }
  }

  /**
   * Inspects a file-upload error and shows an appropriate localized UI
   * message. Recognizes structured backend errors marked with the `ERR::`
   * prefix and falls back to a generic message otherwise.
   *
   * @param error The HTTP error returned by the file digest endpoint.
   */
  private handleFileError(error: HttpErrorResponse) {
    const errorMessage = error.error?.message ?? '';
    const errPos = errorMessage.indexOf('ERR::');

    if (errPos === -1) {
      this.showGenericFileError();
      return;
    }

    const errorDetails = errorMessage.substring(errPos + 5);
    const result = this.parseErrorDetails(errorDetails);
    this.uiMessageService.addInfoMessage(result);
  }

  /**
   * Translates a structured file-error detail string into a localized,
   * human-readable message. Handles the `NumCol<9:`, `NoColumn:` and
   * `NullColumn:` variants, falling back to a generic message.
   *
   * @param errorDetails The error detail payload following the `ERR::` marker.
   * @returns The localized error message to display.
   */
  private parseErrorDetails(errorDetails: string): string {
    if (errorDetails.startsWith('NumCol<9:')) {
      return this.translateService.translate(
        'label.error.invalid.bulk.file.columns',
        {
          details: errorDetails.substring(9),
        }
      );
    }

    if (errorDetails.startsWith('NoColumn:')) {
      return this.parseNoColumnError(errorDetails);
    }

    if (errorDetails.startsWith('NullColumn:')) {
      return this.translateService.translate(
        'label.error.invalid.bulk.file.nullcolumn',
        {
          details: errorDetails.substring(11),
        }
      );
    }

    return this.translateService.translate('label.error.invalid.bulk.file');
  }

  /**
   * Parses a `NoColumn:` error detail (`name|pos|found`) and returns the
   * corresponding localized message, or a generic one when the pattern does
   * not match.
   *
   * @param errorDetails The `NoColumn:`-prefixed error detail string.
   * @returns The localized error message to display.
   */
  private parseNoColumnError(errorDetails: string): string {
    const match = /NoColumn:(.*)\|(\d+)\|(.*)/.exec(errorDetails);

    if (match) {
      return this.translateService.translate(
        'label.error.invalid.bulk.file.nocolumn',
        {
          name: match[1],
          pos: match[2],
          found: match[3],
        }
      );
    }

    return this.translateService.translate('label.error.invalid.bulk.file');
  }

  /** Displays the generic "invalid bulk file" localized UI message. */
  private showGenericFileError() {
    const result = this.translateService.translate(
      'label.error.invalid.bulk.file'
    );
    this.uiMessageService.addInfoMessage(result);
  }

  /**
   * @returns True if the current user is already a member of the target
   * group.
   */
  public isMember(): boolean {
    return this.alreadyMember();
  }

  /**
   * @returns True if the current user has directory-admin rights on
   * {@link currentGroup}.
   */
  public isDirAdmin(): boolean {
    const currentGroup = this.currentGroup();
    return (
      currentGroup !== undefined &&
      this.permEvalService.isDirAdmin(currentGroup)
    );
  }

  /**
   * @returns True if the current user is allowed to manage members of
   * {@link currentGroup}.
   */
  public isDirManageMembers(): boolean {
    const currentGroup = this.currentGroup();
    return (
      currentGroup !== undefined &&
      this.permEvalService.isDirManageMembers(currentGroup)
    );
  }

  /**
   * Sets {@link selectedCategory} to the category matching the given id,
   * which reactively reloads its interest groups via {@link igsResource}.
   *
   * @param value The id of the category to select.
   */
  public setCategory(value: string) {
    const category = this.availableCategories().find((c) => c.id === value);
    if (category) {
      this.selectedCategory.set(category);
    }
  }

  /**
   * Assigns a profile to the displayed candidate whose username matches.
   *
   * @param profileId The profile identifier to assign.
   * @param username The username of the target candidate.
   */
  public setProfile(profileId: string, username: string) {
    for (const member of this.members()) {
      if (member.username === username) {
        member.profileId = profileId;
        break;
      }
    }
  }

  /**
   * Assigns the given profile to every currently selected candidate.
   *
   * @param profileId The profile identifier to assign to all selected members.
   */
  public setSelectedProfiles(profileId: string) {
    for (const member of this.selectedMembers) {
      member.profileId = profileId;
    }
  }

  /**
   * Assigns a profile to a single candidate.
   *
   * @param member The candidate to update.
   * @param profileId The profile identifier to assign.
   */
  public setSelectedProfile(
    member: SelectableBulkImportUserData,
    profileId: string
  ) {
    member.profileId = profileId;
  }

  /**
   * Returns a truncated, localized profile title suitable for display.
   *
   * @param title The i18n title map for the profile, or undefined.
   * @returns The localized title truncated to 12 characters, or an empty
   * string when no title is provided.
   */
  public truncateProfileTitle(
    title: { [key: string]: string } | undefined
  ): string {
    if (title === undefined) {
      return '';
    }
    return truncate(this.i18nPipe.transform(title), 12);
  }

  /**
   * Removes a single candidate from the full candidate list, adjusting
   * pagination as needed.
   *
   * @param member The candidate to remove.
   */
  public deleteMember(member: SelectableBulkImportUserData) {
    this.membersResource.value.update((allMembers) =>
      (allMembers ?? []).filter((m) => m !== member)
    );
    this.removeFromSelected(member);
  }

  /**
   * Removes all currently selected candidates from the full candidate list,
   * then clears the selection state.
   */
  public deleteSelectedMembers() {
    const selected = new Set(this.selectedMembers);
    this.membersResource.value.update((allMembers) =>
      (allMembers ?? []).filter((m) => !selected.has(m))
    );
    this.selectedMembers = [];
    this.allSelected = false;
  }

  /** Clears the uploaded file, which reactively reloads the candidate list without it. */
  public deleteFile() {
    this.fileToUpload.set(undefined);
  }

  /**
   * @returns True if at least one candidate has an `ok` status and can
   * therefore be invited.
   */
  public canInvite() {
    return this.allMembers().some((member) => member.status === 'ok');
  }

  /**
   * Toggles the selected state of the candidate matching the given user and
   * keeps {@link selectedMembers} and {@link allSelected} in sync.
   *
   * @param selectedUser The candidate whose selection should be toggled.
   */
  public toggleSelectedUser(selectedUser: SelectableBulkImportUserData) {
    const member = this.members().find(
      (m) => m.username === selectedUser.username
    );

    if (member) {
      member.selected = !member.selected;
      this.updateSelectedMembers(member);
    }

    if (this.selectedMembers.length === 0) {
      this.allSelected = false;
    }
  }

  /**
   * Removes a member from {@link selectedMembers}, resetting
   * {@link allSelected} when the selection becomes empty.
   *
   * @param member The candidate to remove from the selection.
   */
  private removeFromSelected(member: SelectableBulkImportUserData) {
    const idx = this.selectedMembers.indexOf(member);
    if (idx > -1) {
      this.selectedMembers.splice(idx, 1);
      if (this.selectedMembers.length === 0) {
        this.allSelected = false;
      }
    }
  }

  /**
   * Adds or removes a member from {@link selectedMembers} based on its
   * `selected` flag, resetting {@link allSelected} when the selection becomes
   * empty.
   *
   * @param member The candidate whose selection changed.
   */
  private updateSelectedMembers(member: SelectableBulkImportUserData) {
    if (member.selected) {
      this.selectedMembers.push(member);
    } else {
      this.removeFromSelected(member);
    }
  }

  /**
   * Toggles selection of all candidates: selects every candidate with a
   * username when none are selected, or clears the selection otherwise.
   */
  public selectAll() {
    const allMembers = this.allMembers();
    if (allMembers === undefined || allMembers.length === 0) {
      return;
    }
    if (this.allSelected) {
      this.allSelected = false;
      this.selectedMembers = [];
      this.members().forEach((member) => {
        member.selected = false;
      });
    } else {
      this.selectedMembers = [];
      this.allSelected = true;
      this.members().forEach((member) => {
        if (member.username) {
          member.selected = true;
          this.selectedMembers.push(member);
        }
      });
    }
  }

  /** Placeholder hook for saving work in progress; currently a no-op. */
  public saveWork() {
    //
  }

  /**
   * Submits the bulk invitation. Builds the list of invitable candidates
   * (those with an `ok` status, defaulting missing profiles to the first
   * available profile), maps the group profiles, and calls the backend with
   * the `createNewProfiles` and `notifyUsers` form options. Navigates away on
   * success and always clears {@link processing} when done.
   */
  public async invite() {
    this.processing.set(true);

    try {
      const igProfiles: NameValue[] = this.profiles().map(
        (profile: Profile) => {
          return {
            name:
              profile.title === undefined
                ? profile.name
                : this.i18nPipe.transform(profile.title),
            value: profile.name,
          };
        }
      );

      const okMembers = this.allMembers().filter(
        (value) => value.status === 'ok'
      );
      const members = okMembers.map((value) => {
        value.profileId ??= this.availableProfiles()[0].name;
        return value;
      });

      const bulkInviteData: BulkInviteData = {
        bulkImportUserData: members,
        igProfiles: igProfiles,
      };

      await this.userService.bulkInviteUsersAsync({
        igId: this.igId(),
        bulkInviteData: bulkInviteData,
        createNewProfiles: this.bulkInviteForm.controls.createNewProfiles.value,
        notifyUsers: this.bulkInviteForm.controls.notifyUsers.value,
      });

      this.close();
    } finally {
      this.processing.set(false);
    }
  }

  /** Resets the component state and navigates back to the members listing. */
  public close() {
    this.reset();
    this.router.navigate(['../..'], { relativeTo: this.route });
  }

  /** Clears all selection, candidate lists and the uploaded file. */
  private reset() {
    this.selectedInterestGroups.set([]);
    this.selectedMembers = [];
    this.membersResource.value.set([]);
    this.fileToUpload.set(undefined);
  }

  /**
   * Triggers a download of the bulk-invite import template spreadsheet.
   *
   * @returns Always `false` (to suppress default anchor navigation).
   */
  public getImportUsersFileTemplate() {
    const url = `${this.basePath}/users/bulkinvite/template`;
    const name = 'template.xls';
    this.saveAsService.saveUrlAs(url, name);
    return false;
  }

  /**
   * Handles the file input change event: stores the chosen file, which
   * reactively reloads the candidate list.
   *
   * @param event The DOM change event from the file input element.
   */
  public fileChangeEvent(event: Event) {
    const input = event.target as HTMLInputElement;
    const filesList = input.files as FileList;

    this.fileToUpload.set(filesList[0]);
  }

  // pagination

  /**
   * Changes the page size and returns to the first page.
   *
   * @param limit The new number of items per page.
   */
  public changeLimit(limit: number) {
    this.listingOptions.set({ ...this.listingOptions(), limit, page: 1 });
  }

  /**
   * Navigates to the given page number.
   *
   * @param page The 1-based page number to display.
   */
  public goToPage(page: number) {
    this.listingOptions.set({ ...this.listingOptions(), page });
  }

  /**
   * Ensures a candidate's assigned profile is valid; if the candidate's
   * profile is not among {@link availableProfiles}, it is reset to the first
   * available profile.
   *
   * @param member The candidate whose profile should be validated.
   */
  private extractProfile(member: SelectableBulkImportUserData) {
    const profileUser = member.profileId;
    const availableProfiles = this.availableProfiles();
    const found = availableProfiles.some((profile) => {
      return profile.name === profileUser;
    });

    if (!found) {
      member.profileId = availableProfiles[0].name;
    }
  }
  /**
   * `trackBy` function for the candidate members list, keying rows by the
   * combination of username and assigned profile.
   *
   * @param _index The row index (unused).
   * @param item The candidate for the row.
   * @returns A stable tracking identifier for the row.
   */
  public trackMember(_index: number, item: SelectableBulkImportUserData) {
    return `${item.username}-${item.profileId}`;
  }
}

/**
 * Reorders the given profiles array in place so that the `Access` profile,
 * if present, appears first in the list.
 */
function moveAccessToTop(profiles: Profile[]): void {
  let tempProfile!: Profile;
  for (let idx = 0; idx < profiles.length; idx++) {
    const profile: Profile = profiles[idx];
    if (profile.name === 'Access') {
      tempProfile = profile;
      profiles.splice(idx, 1);
      break;
    }
  }
  if (tempProfile !== undefined) {
    profiles.unshift(tempProfile);
  }
}
