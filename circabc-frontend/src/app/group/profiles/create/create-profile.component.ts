import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges,
} from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';

import { agendaPermissionKeys } from 'app/core/evaluator/agenda-permissions';
import { directoryPermissionKeys } from 'app/core/evaluator/directory-permissions';
import { informationPermissionKeys } from 'app/core/evaluator/information-permissions';
import { libraryPermissionKeys } from 'app/core/evaluator/library-permissions';
import { newsGroupPermissionKeys } from 'app/core/evaluator/newsgroups-permissions';

import { Profile, ProfileService } from 'app/core/generated/circabc';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-create-profile',
  templateUrl: './create-profile.component.html',
  styleUrls: ['./create-profile.component.scss'],
  preserveWhitespaces: true,
})
export class CreateProfileComponent implements OnInit, OnChanges {
  @Input()
  groupId!: string;
  @Input()
  showDialog = false;
  @Input()
  profileToEdit: Profile | undefined;
  @Output()
  readonly profileCreated = new EventEmitter<ActionEmitterResult>();
  @Output()
  readonly profileUpdated = new EventEmitter<ActionEmitterResult>();
  @Output()
  readonly canceled = new EventEmitter();

  public createProfileForm!: FormGroup;
  public processing = false;

  constructor(
    private fb: FormBuilder,
    private profileService: ProfileService
  ) {}

  ngOnInit() {
    this.createProfileForm = this.fb.group(
      {
        title: ['', Validators.required],
        information: [0],
        library: [0],
        members: [0],
        events: [0],
        newsgroups: [0],
      },
      {
        updateOn: 'change',
      }
    );
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes.profileToEdit && this.createProfileForm) {
      if (changes.profileToEdit.currentValue !== undefined) {
        const infIndex = this.getInfIndex(
          changes.profileToEdit.currentValue.permissions.information
        );
        const libIndex = this.getLibIndex(
          changes.profileToEdit.currentValue.permissions.library
        );
        const memIndex = this.getMemIndex(
          changes.profileToEdit.currentValue.permissions.members
        );
        const evtIndex = this.getEvtIndex(
          changes.profileToEdit.currentValue.permissions.events
        );
        const nwsIndex = this.getNwsIndex(
          changes.profileToEdit.currentValue.permissions.newsgroups
        );
        this.createProfileForm = this.fb.group(
          {
            title: [changes.profileToEdit.currentValue.title],
            information: [infIndex],
            library: [libIndex],
            members: [memIndex],
            events: [evtIndex],
            newsgroups: [nwsIndex],
          },
          {
            updateOn: 'change',
          }
        );
      }
    } else if (this.createProfileForm) {
      this.initProfileFormValues();
    }
  }

  public initProfileFormValues() {
    this.createProfileForm.controls.title.patchValue('');
    this.createProfileForm.controls.information.patchValue(0);
    this.createProfileForm.controls.library.patchValue(0);
    this.createProfileForm.controls.members.patchValue(0);
    this.createProfileForm.controls.events.patchValue(0);
    this.createProfileForm.controls.newsgroups.patchValue(0);
  }

  public cancelWizard() {
    this.showDialog = false;
    this.profileToEdit = undefined;
    this.createProfileForm.reset();
    this.canceled.emit();
  }

  public isEditable(): boolean {
    if (
      this.profileToEdit &&
      this.profileToEdit.name &&
      this.profileToEdit.permissions
    ) {
      return (
        !(
          this.profileToEdit.name.toLowerCase() === 'leader' ||
          this.profileToEdit.name === 'IGLeader' ||
          this.profileToEdit.name === '000'
        ) &&
        !(
          this.profileToEdit.permissions.library === 'LibAdmin' &&
          this.profileToEdit.permissions.information === 'InfAdmin' &&
          this.profileToEdit.permissions.events === 'EveAdmin' &&
          this.profileToEdit.permissions.forums === 'NwsAdmin' &&
          this.profileToEdit.permissions.members === 'DirAdmin'
        )
      );
    }

    return true;
  }

  public async createOrEditProfile() {
    const result: ActionEmitterResult = {};
    this.processing = true;

    if (this.profileToEdit) {
      result.type = ActionType.EDIT_PROFILE;
    } else {
      result.type = ActionType.CREATE_PROFILE;
    }

    try {
      const body: Profile = {
        title: this.createProfileForm.controls.title.value,
        permissions: {
          information: this.getInfPerms(
            this.createProfileForm.controls.information.value
          ),
          library: this.getLibPerms(
            this.createProfileForm.controls.library.value
          ),
          members: this.getMemPerms(
            this.createProfileForm.controls.members.value
          ),
          events: this.getEvtPerms(
            this.createProfileForm.controls.events.value
          ),
          newsgroups: this.getNwsPerms(
            this.createProfileForm.controls.newsgroups.value
          ),
        },
      };

      if (
        this.profileToEdit &&
        this.profileToEdit.id &&
        this.profileToEdit.permissions &&
        body.permissions
      ) {
        body.id = this.profileToEdit.id;
        body.permissions.visibility = this.profileToEdit.permissions.visibility;
        await firstValueFrom(
          this.profileService.putProfile(this.profileToEdit.id, body)
        );
      } else {
        await firstValueFrom(
          this.profileService.postProfile(this.groupId, body)
        );
      }

      this.showDialog = false;
      result.result = ActionResult.SUCCEED;
    } catch (error) {
      result.result = ActionResult.FAILED;
    }

    this.processing = false;

    if (this.profileToEdit) {
      this.profileUpdated.emit(result);
    } else {
      this.profileCreated.emit(result);
    }
  }

  public isEdition(): boolean {
    return (
      this.profileToEdit !== undefined && this.profileToEdit.id !== undefined
    );
  }

  public getLabel() {
    return this.profileToEdit ? 'label.edit' : 'label.create';
  }

  public getInfPerms(i: number): string {
    return informationPermissionKeys[i];
  }

  public getInfIndex(s: string): number {
    return informationPermissionKeys.indexOf(s);
  }

  public getLibPerms(i: number): string {
    return libraryPermissionKeys[i];
  }

  public getLibIndex(s: string): number {
    return libraryPermissionKeys.indexOf(s);
  }

  public getMemPerms(i: number): string {
    return directoryPermissionKeys[i];
  }

  public getMemIndex(s: string): number {
    return directoryPermissionKeys.indexOf(s);
  }

  public getEvtPerms(i: number): string {
    return agendaPermissionKeys[i];
  }

  public getEvtIndex(s: string): number {
    return agendaPermissionKeys.indexOf(s);
  }

  public getNwsPerms(i: number): string {
    return newsGroupPermissionKeys[i];
  }

  public getNwsIndex(s: string): number {
    return newsGroupPermissionKeys.indexOf(s);
  }

  private getMaxPermission(maxPermission: number): number {
    if (
      this.profileToEdit &&
      (this.profileToEdit.groupName === 'guest' ||
        this.profileToEdit.groupName === 'GROUP_EVERYONE')
    ) {
      return 1;
    }
    return maxPermission;
  }

  public getMaxInfoPerm(): number {
    return this.getMaxPermission(3);
  }

  public getMaxLibPerm(): number {
    return this.getMaxPermission(5);
  }

  public getMaxMembersPerm(): number {
    return this.getMaxPermission(3);
  }

  public getMaxEventsPerm(): number {
    return this.getMaxPermission(2);
  }

  public getMaxForumsPerm(): number {
    if (
      this.profileToEdit &&
      (this.profileToEdit.groupName === 'guest' ||
        this.profileToEdit.groupName === 'GROUP_EVERYONE')
    ) {
      return 1;
    }
    return 4;
  }

  public isGuestOrRegistered(): boolean {
    if (this.profileToEdit) {
      return (
        this.profileToEdit.name === 'guest' ||
        this.profileToEdit.name === 'EVERYONE'
      );
    }

    return false;
  }
}
