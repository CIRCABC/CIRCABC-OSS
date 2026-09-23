import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  model,
  OnChanges,
  OnInit,
  output,
  SimpleChanges,
  signal,
} from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { MatSliderModule } from '@angular/material/slider';
import { TranslocoModule } from '@jsverse/transloco';
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
import { MultilingualInputComponent } from 'app/shared/input/multilingual-input.component';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { PermissionDescriptorComponent } from './permission-descriptor/permission-descriptor.component';

/**
 * Dialog/wizard component that lets administrators create a new access
 * profile or edit an existing one for a given interest group.
 *
 * The component renders a reactive form with a title field (via the
 * multilingual input) and a set of Material sliders — one per service area
 * (information, library, members, events, newsgroups). Each slider position
 * maps to a permission key defined in the corresponding permission evaluator.
 * On submit it either creates the profile (`postProfile`) or updates the
 * existing one (`putProfile`) through the {@link ProfileService}, then emits
 * the outcome to the parent component.
 *
 * Special/system profiles (e.g. leader, guest, GROUP_EVERYONE/registered) are
 * detected so the UI can restrict editing or cap the maximum selectable
 * permission level.
 *
 * @remarks Key collaborators: {@link ProfileService} (persistence) and the
 * permission key tables from the evaluator modules.
 */
@Component({
  selector: 'cbc-create-profile',
  templateUrl: './create-profile.component.html',
  styleUrl: './create-profile.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ReactiveFormsModule,
    MultilingualInputComponent,
    MatSliderModule,
    PermissionDescriptorComponent,
    SpinnerComponent,
    TranslocoModule,
  ],
})
export class CreateProfileComponent implements OnInit, OnChanges {
  /** Reactive forms builder used to construct the profile form group. */
  private readonly fb = inject(FormBuilder);
  /** API client used to create and update profiles on the backend. */
  private readonly profileService = inject(ProfileService);

  /**
   * Required input: identifier of the interest group the profile belongs to.
   * Used as the target when creating a new profile.
   */
  readonly groupId = input.required<string>();

  /**
   * Two-way bound flag controlling the visibility of the create/edit dialog.
   * Set to `false` by the component when the wizard is cancelled or a save
   * succeeds.
   */
  readonly showDialog = model(false);
  /**
   * Two-way bound profile being edited. When `undefined` the component runs
   * in creation mode; when set, the form is pre-populated for editing.
   */
  readonly profileToEdit = model<Profile>();
  /** Emits the result of a successful/failed profile creation. */
  readonly profileCreated = output<ActionEmitterResult>();
  /** Emits the result of a successful/failed profile update. */
  readonly profileUpdated = output<ActionEmitterResult>();
  /** Emits when the user cancels the wizard. */
  readonly canceled = output();

  /** Reactive form holding the title and per-service permission slider values. */
  public createProfileForm!: FormGroup;
  /** True while a create/edit request is in flight (drives the spinner/disabled state). */
  public processing = signal(false);

  /**
   * Angular lifecycle hook. Initializes {@link createProfileForm} with a
   * required title and permission sliders defaulted to level `0`.
   */
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

  /**
   * Angular lifecycle hook. When a profile to edit is supplied, rebuilds the
   * form with the profile's title and converts each stored permission key
   * back into its slider index. When no profile is supplied, resets the form
   * values to the creation defaults.
   *
   * @param changes The set of changed input/model properties for this cycle.
   */
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

  /**
   * Resets all form controls to their default creation values (empty title
   * and all permission sliders at level `0`).
   */
  public initProfileFormValues() {
    this.createProfileForm.controls.title.patchValue('');
    this.createProfileForm.controls.information.patchValue(0);
    this.createProfileForm.controls.library.patchValue(0);
    this.createProfileForm.controls.members.patchValue(0);
    this.createProfileForm.controls.events.patchValue(0);
    this.createProfileForm.controls.newsgroups.patchValue(0);
  }

  /**
   * Cancels the wizard: hides the dialog, clears the edited profile, resets
   * the form and emits {@link canceled}.
   */
  public cancelWizard() {
    this.showDialog.set(false);
    this.profileToEdit.set(undefined);
    this.createProfileForm.reset();
    this.canceled.emit();
  }

  /**
   * Determines whether the current {@link profileToEdit} may be modified.
   * System/leader profiles and the fully-administrative profile are locked.
   *
   * @returns `true` if the profile can be edited (including creation mode
   * where no profile is set), `false` for protected system profiles.
   */
  public isEditable(): boolean {
    const profileToEdit = this.profileToEdit();
    if (profileToEdit?.name && profileToEdit.permissions) {
      return !(
        profileToEdit.name.toLowerCase() === 'leader' ||
        profileToEdit.name === 'IGLeader' ||
        profileToEdit.name === '000' ||
        (profileToEdit.permissions.library === 'LibAdmin' &&
          profileToEdit.permissions.information === 'InfAdmin' &&
          profileToEdit.permissions.events === 'EveAdmin' &&
          profileToEdit.permissions.forums === 'NwsAdmin' &&
          profileToEdit.permissions.members === 'DirAdmin')
      );
    }

    return true;
  }

  /**
   * Submits the form to either create or update a profile.
   *
   * Builds a {@link Profile} payload by translating each slider index into its
   * permission key. In edit mode it preserves the existing id and visibility
   * and calls {@link ProfileService.putProfile}; otherwise it calls
   * {@link ProfileService.postProfile} for the current {@link groupId}. The
   * outcome is emitted via {@link profileUpdated} or {@link profileCreated}.
   *
   * @returns A promise that resolves once the request completes and the
   * result has been emitted. Request failures are caught internally and
   * reported as a {@link ActionResult.FAILED} result rather than thrown.
   */
  public async createOrEditProfile() {
    const result: ActionEmitterResult = {};
    this.processing.set(true);

    const profileToEdit = this.profileToEdit();
    if (profileToEdit) {
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

      if (profileToEdit?.id && profileToEdit.permissions && body.permissions) {
        body.id = profileToEdit.id;
        body.permissions.visibility = profileToEdit.permissions.visibility;
        await this.profileService.putProfileAsync({
          id: profileToEdit.id,
          profile: body,
        });
      } else {
        await this.profileService.postProfileAsync({
          id: this.groupId(),
          profile: body,
        });
      }

      this.showDialog.set(false);
      result.result = ActionResult.SUCCEED;
    } catch (error) {
      console.error(error);
      result.result = ActionResult.FAILED;
    }

    this.processing.set(false);

    if (profileToEdit) {
      this.profileUpdated.emit(result);
    } else {
      this.profileCreated.emit(result);
    }
  }

  /**
   * @returns `true` when the component is editing an existing (persisted)
   * profile, i.e. {@link profileToEdit} has an id.
   */
  public isEdition(): boolean {
    return this.profileToEdit()?.id !== undefined;
  }

  /**
   * @returns The i18n label key for the submit button — `label.save` when
   * editing, `label.create` otherwise.
   */
  public getLabel() {
    return this.profileToEdit() ? 'label.save' : 'label.create';
  }

  /**
   * Maps an information-service slider index to its permission key.
   *
   * @param i Slider index.
   * @returns The corresponding information permission key.
   */
  public getInfPerms(i: number): string {
    return informationPermissionKeys[i];
  }

  /**
   * Maps an information permission key to its slider index.
   *
   * @param s Permission key.
   * @returns The index of the key, or `-1` if not found.
   */
  public getInfIndex(s: string): number {
    return informationPermissionKeys.indexOf(s);
  }

  /**
   * Maps a library-service slider index to its permission key.
   *
   * @param i Slider index.
   * @returns The corresponding library permission key.
   */
  public getLibPerms(i: number): string {
    return libraryPermissionKeys[i];
  }

  /**
   * Maps a library permission key to its slider index.
   *
   * @param s Permission key.
   * @returns The index of the key, or `-1` if not found.
   */
  public getLibIndex(s: string): number {
    return libraryPermissionKeys.indexOf(s);
  }

  /**
   * Maps a members-service slider index to its (directory) permission key.
   *
   * @param i Slider index.
   * @returns The corresponding directory permission key.
   */
  public getMemPerms(i: number): string {
    return directoryPermissionKeys[i];
  }

  /**
   * Maps a members (directory) permission key to its slider index.
   *
   * @param s Permission key.
   * @returns The index of the key, or `-1` if not found.
   */
  public getMemIndex(s: string): number {
    return directoryPermissionKeys.indexOf(s);
  }

  /**
   * Maps an events-service slider index to its (agenda) permission key.
   *
   * @param i Slider index.
   * @returns The corresponding agenda permission key.
   */
  public getEvtPerms(i: number): string {
    return agendaPermissionKeys[i];
  }

  /**
   * Maps an events (agenda) permission key to its slider index.
   *
   * @param s Permission key.
   * @returns The index of the key, or `-1` if not found.
   */
  public getEvtIndex(s: string): number {
    return agendaPermissionKeys.indexOf(s);
  }

  /**
   * Maps a newsgroups-service slider index to its permission key.
   *
   * @param i Slider index.
   * @returns The corresponding newsgroup permission key.
   */
  public getNwsPerms(i: number): string {
    return newsGroupPermissionKeys[i];
  }

  /**
   * Maps a newsgroup permission key to its slider index.
   *
   * @param s Permission key.
   * @returns The index of the key, or `-1` if not found.
   */
  public getNwsIndex(s: string): number {
    return newsGroupPermissionKeys.indexOf(s);
  }

  /**
   * Caps the maximum selectable permission level. For the special `guest` and
   * `GROUP_EVERYONE` profiles the maximum is forced to `1`; otherwise the
   * provided maximum is returned unchanged.
   *
   * @param maxPermission The service-specific default maximum slider level.
   * @returns The effective maximum permission level.
   */
  private getMaxPermission(maxPermission: number): number {
    const profileToEdit = this.profileToEdit();
    if (
      profileToEdit &&
      (profileToEdit.groupName === 'guest' ||
        profileToEdit.groupName === 'GROUP_EVERYONE')
    ) {
      return 1;
    }
    return maxPermission;
  }

  /** @returns The maximum selectable information permission level. */
  public getMaxInfoPerm(): number {
    return this.getMaxPermission(3);
  }

  /** @returns The maximum selectable library permission level. */
  public getMaxLibPerm(): number {
    return this.getMaxPermission(5);
  }

  /** @returns The maximum selectable members (directory) permission level. */
  public getMaxMembersPerm(): number {
    return this.getMaxPermission(3);
  }

  /** @returns The maximum selectable events (agenda) permission level. */
  public getMaxEventsPerm(): number {
    return this.getMaxPermission(2);
  }

  /**
   * @returns The maximum selectable forums/newsgroups permission level —
   * `1` for the `guest` and `GROUP_EVERYONE` profiles, otherwise `4`.
   */
  public getMaxForumsPerm(): number {
    const profileToEdit = this.profileToEdit();
    if (
      profileToEdit &&
      (profileToEdit.groupName === 'guest' ||
        profileToEdit.groupName === 'GROUP_EVERYONE')
    ) {
      return 1;
    }
    return 4;
  }

  /**
   * @returns `true` if the edited profile is either the `guest` or the
   * registered (`EVERYONE`) system profile.
   */
  public isGuestOrRegistered(): boolean {
    const profileToEdit = this.profileToEdit();
    if (profileToEdit) {
      return (
        profileToEdit.name === 'guest' || profileToEdit.name === 'EVERYONE'
      );
    }

    return false;
  }

  /** @returns `true` if the edited profile is the `guest` system profile. */
  public isGuest(): boolean {
    const profileToEdit = this.profileToEdit();
    if (profileToEdit) {
      return profileToEdit.name === 'guest';
    }

    return false;
  }

  /**
   * @returns `true` if the edited profile is the registered-users
   * (`EVERYONE`) system profile.
   */
  public isRegistered(): boolean {
    const profileToEdit = this.profileToEdit();
    if (profileToEdit) {
      return profileToEdit.name === 'EVERYONE';
    }

    return false;
  }
}
