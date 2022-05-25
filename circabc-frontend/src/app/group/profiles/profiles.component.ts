import { Component, OnInit } from '@angular/core';

import { ActivatedRoute } from '@angular/router';

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
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-profiles',
  templateUrl: './profiles.component.html',
  styleUrls: ['./profiles.component.scss'],
  preserveWhitespaces: true,
})
export class ProfilesComponent implements OnInit {
  public profiles: Profile[] = [];
  public showModal = false;
  public showImportModal = false;
  public nodeId!: string;
  public selectedProfile: Profile | undefined;
  public showDeleteModal = false;
  public loading = false;
  public currentGroup!: InterestGroup;
  public alreadyMember = false;
  public showAddDropdown = false;
  public exportFeatureEnabled = false;

  constructor(
    private profileService: ProfileService,
    private route: ActivatedRoute,
    private groupService: InterestGroupService,
    private loginService: LoginService,
    private userService: UserService,
    private permEvalService: PermissionEvaluatorService
  ) {}

  ngOnInit() {
    this.route.params.subscribe(
      async (params) => await this.loadProfiles(params)
    );
  }

  private async loadProfiles(params: { [key: string]: string }) {
    this.loading = true;
    this.nodeId = params.id;
    if (this.nodeId) {
      this.currentGroup = await firstValueFrom(
        this.groupService.getInterestGroup(this.nodeId)
      );

      await this.getCurrentUserMemberships();

      this.profiles = await firstValueFrom(
        this.profileService.getProfiles(this.nodeId)
      );
    }
    this.loading = false;
  }

  public async onProfileCreated(result: ActionEmitterResult) {
    if (
      result.type === ActionType.CREATE_PROFILE &&
      result.result === ActionResult.SUCCEED
    ) {
      await this.loadProfiles({ id: this.nodeId });
      this.showModal = false;
    }
  }

  public async onProfileEdited(result: ActionEmitterResult) {
    if (
      result.type === ActionType.EDIT_PROFILE &&
      result.result === ActionResult.SUCCEED
    ) {
      await this.loadProfiles({ id: this.nodeId });
      this.showModal = false;
    }
  }

  public prepareDelete(profile: Profile) {
    this.selectedProfile = profile;
    this.showDeleteModal = true;
  }

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

  public isDeletable(profile: Profile): boolean {
    return (
      profile.name !== 'guest' &&
      profile.name !== 'EVERYONE' &&
      this.isEditable(profile) &&
      profile.exported !== true
    );
  }

  public isEditable(profile: Profile): boolean {
    if (profile.name && profile.permissions) {
      return (
        !(
          profile.name.toLowerCase() === 'leader' ||
          profile.name === 'IGLeader' ||
          profile.name === '000'
        ) &&
        !(
          profile.permissions.library === 'LibAdmin' &&
          profile.permissions.information === 'InfAdmin' &&
          profile.permissions.events === 'EveAdmin' &&
          profile.permissions.forums === 'NwsAdmin' &&
          profile.permissions.members === 'DirAdmin'
        )
      );
    }

    return true;
  }

  public prepareEdit(profile: Profile) {
    this.selectedProfile = profile;
    this.showModal = true;
  }

  public onModalCancel() {
    this.selectedProfile = undefined;
    this.showModal = false;
    this.showDeleteModal = false;
  }

  public isExportable(profile: Profile): boolean {
    if (this.exportFeatureEnabled) {
      return (
        !profile.exported && !profile.imported && this.isDeletable(profile)
      );
    } else {
      return false;
    }
  }

  public isUnexportable(profile: Profile): boolean {
    if (profile.exported === true) {
      if (profile.exportedRefs) {
        return profile.exportedRefs.length === 0;
      } else {
        return true;
      }
    } else {
      return false;
    }
  }

  public async export(profile: Profile) {
    profile.exported = true;
    if (profile.id) {
      await firstValueFrom(this.profileService.putProfile(profile.id, profile));
    }
  }

  public async unexport(profile: Profile) {
    profile.exported = false;
    if (profile.id) {
      await firstValueFrom(this.profileService.putProfile(profile.id, profile));
    }
  }

  public async onProfileImport(result: ActionEmitterResult) {
    this.showImportModal = false;
    if (
      result.type === ActionType.IMPORT_PROFILE &&
      result.result === ActionResult.SUCCEED
    ) {
      await this.loadProfiles({ id: this.nodeId });
    }
  }

  public isImported(profile: Profile): boolean {
    return profile.imported === true;
  }

  public unimport(profile: Profile): boolean {
    return profile.imported === true;
  }

  isMember() {
    return this.alreadyMember;
  }

  private async getCurrentUserMemberships() {
    if (!this.loginService.isGuest()) {
      const userId =
        this.loginService.getUser().userId !== undefined
          ? this.loginService.getUser().userId
          : 'guest';
      if (userId !== undefined) {
        const currentUserMemberships = await firstValueFrom(
          this.userService.getUserMembership(userId)
        );
        for (const profile of currentUserMemberships) {
          if (
            profile &&
            profile.interestGroup &&
            profile.interestGroup.id === this.currentGroup.id
          ) {
            this.alreadyMember = true;
          }
        }
      }
    }
  }

  public isDirAdmin(): boolean {
    return this.permEvalService.isDirAdmin(this.currentGroup);
  }

  public isDirManageMembers(): boolean {
    return this.permEvalService.isDirManageMembers(this.currentGroup);
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  public toggleAddDropdown(event: any) {
    // only trigger display or hide when clicking the dropdown-trigger html element
    if (event.target.classList.contains('dropdown-trigger')) {
      this.showAddDropdown = !this.showAddDropdown;
    }
  }

  public showAddProfile() {
    this.selectedProfile = undefined;
    this.showModal = true;
    this.showAddDropdown = false;
  }

  public showImportProfile() {
    this.showImportModal = true;
    this.showAddDropdown = false;
  }
}
