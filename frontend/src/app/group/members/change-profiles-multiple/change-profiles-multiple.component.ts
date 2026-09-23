import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  output,
  resource,
  signal,
} from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { TranslocoModule } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import {
  MembershipPostDefinition,
  MembersService,
  Profile,
  ProfileService,
  UserProfile,
} from 'app/core/generated/circabc';
import { SelectableUserProfile } from 'app/core/ui-model';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';

/**
 * Modal component that lets an administrator assign a single access profile to
 * multiple selected group members at once.
 *
 * Rendered inside a {@link ModalComponent}, it presents a reactive form with a
 * profile selector and notification toggles. On confirmation it builds a
 * {@link MembershipPostDefinition} and submits it via the {@link MembersService}
 * to update every selected user's profile in one request. The outcome is
 * reported back to the parent through the {@link modalHide} output.
 *
 * Key collaborators:
 * - {@link ProfileService} to load the group's assignable profiles.
 * - {@link MembersService} to persist the membership changes.
 * - {@link I18nPipe} to resolve localized profile titles.
 */
@Component({
  selector: 'cbc-change-profiles-multiple',
  templateUrl: './change-profiles-multiple.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ModalComponent, ReactiveFormsModule, I18nPipe, TranslocoModule],
})
export class ChangeProfilesMultipleComponent {
  /** Angular reactive forms builder used to construct the profile-change form. */
  private readonly fb = inject(FormBuilder);
  /** API client used to submit the batch membership/profile update. */
  private readonly membersService = inject(MembersService);
  /** API client used to fetch the assignable profiles for the group. */
  private readonly profileService = inject(ProfileService);
  /** Pipe used to resolve localized profile titles for display. */
  private readonly i18nPipe = inject(I18nPipe);

  /** Required input: the collection of selected users whose profile will be changed. */
  readonly users = input.required<SelectableUserProfile[]>();
  /** Required input: the identifier of the group the members belong to. */
  readonly groupId = input.required<string>();

  /** Required input: controls the visibility of the underlying modal. */
  readonly showModal = input.required<boolean>();
  /**
   * Output emitted when the modal closes, carrying the result of the operation
   * (succeeded, failed or canceled) as an {@link ActionEmitterResult}.
   */
  public readonly modalHide = output<ActionEmitterResult>();

  /**
   * Loads the group's profiles, keeping only those that can be assigned to
   * members and filtering out the built-in `guest` and `EVERYONE` profiles.
   * Stays idle while {@link groupId} is empty. Errors are logged and result in
   * an empty list, since this component has no dedicated error UI.
   */
  private readonly profilesResource = resource({
    params: () => this.groupId() || undefined,
    loader: async ({ params: groupId }) => {
      try {
        const profilesUnclean = await this.profileService.getProfilesAsync({
          id: groupId,
        });
        const cleaned: Profile[] = [];
        for (const profile of profilesUnclean) {
          if (profile.name !== 'guest' && profile.name !== 'EVERYONE') {
            cleaned.push(profile);
          }
        }
        return cleaned;
      } catch (error) {
        console.error('Error while loading the group profiles', error);
        return [];
      }
    },
    defaultValue: [],
  });
  /** Assignable profiles for the group, excluding the `guest` and `EVERYONE` profiles. */
  public readonly profiles = this.profilesResource.value;
  /** Reactive form holding the selected profile and notification preferences. */
  public changeProfileForm: FormGroup = this.fb.group(
    {
      selectedProfile: [''],
      userNotifications: [false],
      adminNotifications: [false],
    },
    {
      updateOn: 'change',
    }
  );
  /** Whether a profile-change request is currently in progress (used to disable UI). */
  public readonly processing = signal(false);

  /**
   * Applies the currently selected profile to every selected user and submits
   * the batch update to the backend. Emits an {@link ActionEmitterResult} via
   * {@link modalHide} indicating success or failure, and toggles
   * {@link processing} around the request. Errors from the update call are
   * caught and reported as a failed result rather than propagated.
   *
   * @returns A promise that resolves once the update has completed and the
   * result has been emitted.
   */
  public async changeProfiles() {
    this.processing.set(true);

    const body: MembershipPostDefinition = {};
    body.adminNotifications = this.changeProfileForm.value.adminNotifications;
    body.userNotifications = this.changeProfileForm.value.userNotifications;
    body.memberships = [];

    for (const user of this.users()) {
      const newMember = user;
      newMember.profile = this.getProfile();
      body.memberships.push(newMember);
    }

    const res: ActionEmitterResult = {};
    res.type = ActionType.CHANGE_PROFILE;

    try {
      await this.membersService.putMemberAsync({
        id: this.groupId(),
        membershipPostDefinition: body,
      });
      res.result = ActionResult.SUCCEED;
    } catch (error) {
      console.error(error);
      console.error('Error while updating the users profiles');
      res.result = ActionResult.FAILED;
    }

    this.modalHide.emit(res);
    this.processing.set(false);
  }

  /**
   * Resolves the {@link Profile} matching the currently selected profile id in
   * the form.
   *
   * @returns The matching {@link Profile}, or `undefined` if the form is not
   * initialized or no profile matches the selection.
   */
  public getProfile(): Profile | undefined {
    if (this.changeProfileForm) {
      for (const profile of this.profiles()) {
        if (this.changeProfileForm.value.selectedProfile === profile.id) {
          return profile;
        }
      }
    }

    return undefined;
  }

  /**
   * Cancels the operation without applying any changes and closes the modal by
   * emitting a canceled {@link ActionEmitterResult} through {@link modalHide}.
   */
  public cancel() {
    const res: ActionEmitterResult = {};
    res.type = ActionType.CHANGE_PROFILE;
    res.result = ActionResult.CANCELED;
    this.modalHide.emit(res);
  }

  /**
   * Computes a human-readable, localized display title for a member's current
   * profile. Falls back to the profile's raw name when no localized title is
   * available, and to an empty string when the member has no profile or name.
   *
   * @param member The user profile whose profile title should be resolved.
   * @returns The localized profile title, the profile name as a fallback, or an
   * empty string.
   */
  public getProfileTitle(member: UserProfile): string {
    if (member.profile === undefined) {
      return '';
    }
    const title = this.i18nPipe.transform(member.profile.title);
    if (title === undefined || title === '') {
      if (member.profile.name === undefined) {
        return '';
      }
      return member.profile.name;
    }
    return title;
  }
}
