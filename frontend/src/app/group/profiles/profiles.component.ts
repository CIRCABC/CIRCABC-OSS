import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  OnInit,
} from '@angular/core';

import { ActivatedRoute, RouterLink } from '@angular/router';

import { TranslocoModule } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import {
  InterestGroup,
  InterestGroupService,
  Profile,
  ProfileService,
  UserService,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { ReponsiveSubMenuComponent } from 'app/shared/reponsive-sub-menu/reponsive-sub-menu.component';
import { CreateProfileComponent } from './create/create-profile.component';
import { DeleteProfileComponent } from './delete/delete-profile.component';
import { ImportProfileComponent } from './import/import-profile.component';

/**
 * Access profiles management view for an interest group.
 *
 * This standalone Angular component renders the list of {@link Profile}
 * (access profiles) belonging to the interest group identified by the `id`
 * route parameter. For each profile it shows the available actions and
 * orchestrates the create, edit, delete and import workflows through embedded
 * child components ({@link CreateProfileComponent},
 * {@link DeleteProfileComponent} and {@link ImportProfileComponent}), which are
 * displayed as modal dialogs.
 *
 * The component determines which actions are permitted per profile (editable,
 * deletable, exportable, etc.) based on the profile's name, permissions and
 * export/import state, and gates member-management UI on the current user's
 * permissions within the group.
 *
 * Key collaborators:
 * - {@link ProfileService} — fetches and persists profiles.
 * - {@link InterestGroupService} — loads the current interest group.
 * - {@link UserService} / {@link LoginService} — resolve the current user and
 *   their memberships.
 * - {@link PermissionEvaluatorService} — evaluates the current user's
 *   directory (members) administration permissions on the group.
 *
 * The view has no `input()` or `output()` members; its state is driven
 * entirely by the route and by callbacks from the child action components.
 */
@Component({
  selector: 'cbc-profiles',
  templateUrl: './profiles.component.html',
  styleUrl: './profiles.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    HorizontalLoaderComponent,
    ReponsiveSubMenuComponent,
    RouterLink,
    CreateProfileComponent,
    DeleteProfileComponent,
    ImportProfileComponent,
    I18nPipe,
    TranslocoModule,
  ],
})
export class ProfilesComponent implements OnInit {
  private readonly profileService = inject(ProfileService);
  private readonly route = inject(ActivatedRoute);
  private readonly groupService = inject(InterestGroupService);
  private readonly loginService = inject(LoginService);
  private readonly userService = inject(UserService);
  private readonly permEvalService = inject(PermissionEvaluatorService);
  private readonly changeDetectorRef = inject(ChangeDetectorRef);

  /** Access profiles of the current interest group, loaded from the backend. */
  public profiles: Profile[] = [];
  /** Whether the create/edit profile modal is currently displayed. */
  public showModal = false;
  /** Whether the import profile modal is currently displayed. */
  public showImportModal = false;
  /** Node identifier of the current interest group, taken from the `id` route parameter. */
  public nodeId!: string;
  /** Profile currently targeted by an edit or delete action, if any. */
  public selectedProfile: Profile | undefined;
  /** Whether the delete confirmation modal is currently displayed. */
  public showDeleteModal = false;
  /** Whether an asynchronous load of the group/profiles is in progress. */
  public loading = false;
  /** The interest group whose profiles are being managed. */
  public currentGroup!: InterestGroup;
  /** Whether the current user is already a member of the current group. */
  public alreadyMember = false;
  /** Whether the "add" dropdown (create/import choices) is expanded. */
  public showAddDropdown = false;
  /** Whether the profile export/import feature is enabled for this view. */
  public exportFeatureEnabled = false;

  /**
   * Angular lifecycle hook. Subscribes to route parameter changes and loads
   * the interest group and its profiles whenever the parameters change.
   */
  ngOnInit() {
    this.route.params.subscribe(
      async (params) => await this.loadProfiles(params)
    );
  }

  /**
   * Loads the current interest group, the current user's memberships and the
   * group's profiles for the given route parameters. Toggles {@link loading}
   * around the asynchronous work.
   *
   * @param params Route parameters; the `id` entry identifies the interest group.
   * @returns A promise that resolves once loading has completed.
   */
  private async loadProfiles(params: { [key: string]: string }) {
    this.loading = true;
    this.nodeId = params.id;
    if (this.nodeId) {
      this.currentGroup = await this.groupService.getInterestGroupAsync({
        id: this.nodeId,
      });

      await this.getCurrentUserMemberships();

      this.profiles = await this.profileService.getProfilesAsync({
        id: this.nodeId,
      });
    }
    this.loading = false;
    // OnPush: loading/currentGroup/alreadyMember/profiles are set from this
    // async route-driven load.
    this.changeDetectorRef.markForCheck();
  }

  /**
   * Handles the result emitted after a create-profile action. On a successful
   * creation, reloads the profiles and closes the modal.
   *
   * @param result The action result emitted by the create profile component.
   * @returns A promise that resolves once any required reload has completed.
   */
  public async onProfileCreated(result: ActionEmitterResult) {
    if (
      result.type === ActionType.CREATE_PROFILE &&
      result.result === ActionResult.SUCCEED
    ) {
      await this.loadProfiles({ id: this.nodeId });
      this.showModal = false;
    }
  }

  /**
   * Handles the result emitted after an edit-profile action. On a successful
   * edit, reloads the profiles and closes the modal.
   *
   * @param result The action result emitted by the edit profile component.
   * @returns A promise that resolves once any required reload has completed.
   */
  public async onProfileEdited(result: ActionEmitterResult) {
    if (
      result.type === ActionType.EDIT_PROFILE &&
      result.result === ActionResult.SUCCEED
    ) {
      await this.loadProfiles({ id: this.nodeId });
      this.showModal = false;
    }
  }

  /**
   * Selects the given profile and opens the delete confirmation modal.
   *
   * @param profile The profile to be deleted.
   */
  public prepareDelete(profile: Profile) {
    this.selectedProfile = profile;
    this.showDeleteModal = true;
  }

  /**
   * Handles the result emitted after a delete-profile action. Clears the
   * selection and closes the delete modal on both cancellation and success,
   * and reloads the profiles when the deletion succeeded.
   *
   * @param result The action result emitted by the delete profile component.
   * @returns A promise that resolves once any required reload has completed.
   */
  public async onProfileDeleted(result: ActionEmitterResult) {
    if (
      result.type === ActionType.DELETE_PROFILE &&
      result.result === ActionResult.CANCELED
    ) {
      this.selectedProfile = undefined;
      this.showDeleteModal = false;
    } else if (
      result.type === ActionType.DELETE_PROFILE &&
      result.result === ActionResult.SUCCEED
    ) {
      this.selectedProfile = undefined;
      this.showDeleteModal = false;
      await this.loadProfiles({ id: this.nodeId });
    }
  }

  /**
   * Determines whether the given profile can be deleted. Built-in profiles
   * (`guest`, `EVERYONE`), non-editable profiles and exported profiles cannot
   * be deleted.
   *
   * @param profile The profile to evaluate.
   * @returns `true` if the profile may be deleted, otherwise `false`.
   */
  public isDeletable(profile: Profile): boolean {
    return (
      profile.name !== 'guest' &&
      profile.name !== 'EVERYONE' &&
      this.isEditable(profile) &&
      profile.exported !== true
    );
  }

  /**
   * Determines whether the given profile can be edited. Reserved leader
   * profiles (`leader`, `IGLeader`, `000`) and the full-administrator profile
   * (admin on all services) are considered non-editable.
   *
   * @param profile The profile to evaluate.
   * @returns `true` if the profile may be edited, otherwise `false`.
   */
  public isEditable(profile: Profile): boolean {
    if (profile.name && profile.permissions) {
      return !(
        profile.name.toLowerCase() === 'leader' ||
        profile.name === 'IGLeader' ||
        profile.name === '000' ||
        (profile.permissions.library === 'LibAdmin' &&
          profile.permissions.information === 'InfAdmin' &&
          profile.permissions.events === 'EveAdmin' &&
          profile.permissions.forums === 'NwsAdmin' &&
          profile.permissions.members === 'DirAdmin')
      );
    }

    return true;
  }

  /**
   * Selects the given profile and opens the create/edit modal in edit mode.
   *
   * @param profile The profile to edit.
   */
  public prepareEdit(profile: Profile) {
    this.selectedProfile = profile;
    this.showModal = true;
  }

  /**
   * Cancels any open modal: clears the current selection and hides both the
   * create/edit and delete modals.
   */
  public onModalCancel() {
    this.selectedProfile = undefined;
    this.showModal = false;
    this.showDeleteModal = false;
  }

  /**
   * Determines whether the given profile can be exported. Requires the export
   * feature to be enabled and the profile to be deletable and neither already
   * exported nor imported.
   *
   * @param profile The profile to evaluate.
   * @returns `true` if the profile may be exported, otherwise `false`.
   */
  public isExportable(profile: Profile): boolean {
    if (this.exportFeatureEnabled) {
      return (
        !(profile.exported || profile.imported) && this.isDeletable(profile)
      );
    }
    return false;
  }

  /**
   * Determines whether the given profile can be un-exported. A profile can be
   * un-exported only if it is currently exported and has no referencing
   * exports.
   *
   * @param profile The profile to evaluate.
   * @returns `true` if the profile may be un-exported, otherwise `false`.
   */
  public isUnexportable(profile: Profile): boolean {
    if (profile.exported === true) {
      if (profile.exportedRefs) {
        return profile.exportedRefs.length === 0;
      }
      return true;
    }
    return false;
  }

  /**
   * Marks the given profile as exported and persists the change through the
   * profile service.
   *
   * @param profile The profile to export.
   * @returns A promise that resolves once the update has been persisted.
   */
  public async export(profile: Profile) {
    profile.exported = true;
    if (profile.id) {
      await this.profileService.putProfileAsync({ id: profile.id, profile });
    }
  }

  /**
   * Clears the exported flag on the given profile and persists the change
   * through the profile service.
   *
   * @param profile The profile to un-export.
   * @returns A promise that resolves once the update has been persisted.
   */
  public async unexport(profile: Profile) {
    profile.exported = false;
    if (profile.id) {
      await this.profileService.putProfileAsync({ id: profile.id, profile });
    }
  }

  /**
   * Handles the result emitted after an import-profile action. Closes the
   * import modal and reloads the profiles when the import succeeded.
   *
   * @param result The action result emitted by the import profile component.
   * @returns A promise that resolves once any required reload has completed.
   */
  public async onProfileImport(result: ActionEmitterResult) {
    this.showImportModal = false;
    if (
      result.type === ActionType.IMPORT_PROFILE &&
      result.result === ActionResult.SUCCEED
    ) {
      await this.loadProfiles({ id: this.nodeId });
    }
  }

  /**
   * Indicates whether the given profile was imported from another group.
   *
   * @param profile The profile to evaluate.
   * @returns `true` if the profile is imported, otherwise `false`.
   */
  public isImported(profile: Profile): boolean {
    return profile.imported === true;
  }

  /**
   * Indicates whether the given profile is eligible to be un-imported
   * (currently returns the profile's imported state).
   *
   * @param profile The profile to evaluate.
   * @returns `true` if the profile is imported, otherwise `false`.
   */
  public unimport(profile: Profile): boolean {
    return profile.imported === true;
  }

  /**
   * Indicates whether the current user is already a member of the current
   * interest group.
   *
   * @returns `true` if the current user is a member, otherwise `false`.
   */
  isMember() {
    return this.alreadyMember;
  }

  /**
   * Resolves the current user's memberships and sets {@link alreadyMember} to
   * `true` when one of them belongs to the current interest group. Does
   * nothing for guest users.
   *
   * @returns A promise that resolves once the memberships have been evaluated.
   */
  private async getCurrentUserMemberships() {
    if (this.loginService.isGuest()) {
      return;
    }
    const userId =
      this.loginService.getUser().userId === undefined
        ? 'guest'
        : this.loginService.getUser().userId;
    if (userId !== undefined) {
      const currentUserMemberships =
        await this.userService.getUserMembershipAsync({ userId });
      for (const profile of currentUserMemberships) {
        if (
          profile?.interestGroup &&
          profile.interestGroup.id === this.currentGroup.id
        ) {
          this.alreadyMember = true;
        }
      }
    }
  }

  /**
   * Indicates whether the current user has directory (members) administration
   * rights on the current group.
   *
   * @returns `true` if the user is a directory administrator, otherwise `false`.
   */
  public isDirAdmin(): boolean {
    return this.permEvalService.isDirAdmin(this.currentGroup);
  }

  /**
   * Indicates whether the current user is allowed to manage members of the
   * current group.
   *
   * @returns `true` if the user may manage members, otherwise `false`.
   */
  public isDirManageMembers(): boolean {
    return this.permEvalService.isDirManageMembers(this.currentGroup);
  }

  /**
   * Toggles the "add" dropdown, but only when the click originated from the
   * dropdown trigger element (identified by the `dropdown-trigger` CSS class).
   *
   * @param event The DOM click event that triggered the toggle.
   */
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  public toggleAddDropdown(event: any) {
    // only trigger display or hide when clicking the dropdown-trigger html element
    if (event.target.classList.contains('dropdown-trigger')) {
      this.showAddDropdown = !this.showAddDropdown;
    }
  }

  /**
   * Opens the create profile modal (clearing any current selection) and
   * collapses the "add" dropdown.
   */
  public showAddProfile() {
    this.selectedProfile = undefined;
    this.showModal = true;
    this.showAddDropdown = false;
  }

  /**
   * Opens the import profile modal and collapses the "add" dropdown.
   */
  public showImportProfile() {
    this.showImportModal = true;
    this.showAddDropdown = false;
  }
}
