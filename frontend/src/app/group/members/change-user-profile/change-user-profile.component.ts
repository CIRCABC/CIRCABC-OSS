import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  model,
  OnChanges,
  output,
  resource,
  SimpleChanges,
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
  type UserProfile,
} from 'app/core/generated/circabc';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';

/**
 * Modal component that lets an administrator change the access profile
 * (role) of a single member within an interest group.
 *
 * The component renders a modal dialog (via {@link ModalComponent}) containing
 * a reactive form with a profile selector and two notification checkboxes
 * (user and admin notifications). On initialization it loads the list of
 * available profiles for the target group — excluding the `guest` and
 * `EVERYONE` pseudo-profiles — and pre-selects the member's current profile.
 * When confirmed, it submits the updated membership to the backend through
 * {@link MembersService} and reports the outcome to the parent via the
 * {@link ChangeUserProfileComponent.modalHide | modalHide} output.
 *
 * Key collaborators: {@link MembersService} (persisting the membership change),
 * {@link ProfileService} (loading the group's profiles), {@link FormBuilder}
 * (building the reactive form) and {@link I18nPipe} (resolving localized
 * profile titles).
 */
@Component({
  selector: 'cbc-change-user-profile',
  templateUrl: './change-user-profile.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ModalComponent, ReactiveFormsModule, I18nPipe, TranslocoModule],
})
export class ChangeUserProfileComponent implements OnChanges {
  /** Builder used to construct the reactive profile-change form. */
  private readonly fb = inject(FormBuilder);
  /** Backend service used to persist the updated membership. */
  private readonly membersService = inject(MembersService);
  /** Backend service used to retrieve the group's available profiles. */
  private readonly profileService = inject(ProfileService);
  /** Pipe used to resolve localized profile titles. */
  private readonly i18nPipe = inject(I18nPipe);

  /**
   * Two-way bindable model controlling the visibility of the modal dialog.
   * `true` shows the dialog; it is set to `false` when the action is
   * cancelled or completed.
   */
  readonly showModal = model(false);
  /** Required input: identifier of the group the member belongs to. */
  readonly groupId = input.required<string>();

  /** Required input: the member whose access profile is being changed. */
  readonly member = input.required<UserProfile>();
  /**
   * Output emitted when the modal closes, carrying the result of the action
   * (a {@link ActionType.CHANGE_PROFILE} result that either succeeded, failed
   * or was cancelled).
   */
  readonly modalHide = output<ActionEmitterResult>();

  /**
   * Resource that loads the assignable profiles for {@link groupId},
   * excluding the `guest` and `EVERYONE` pseudo-profiles. Idle while no
   * group id is available.
   */
  private readonly profilesResource = resource({
    params: () => this.groupId() || undefined,
    loader: async ({ params: groupId }) => {
      try {
        const profilesUnclean = await this.profileService.getProfilesAsync({
          id: groupId,
        });
        return profilesUnclean.filter(
          (profile) => profile.name !== 'guest' && profile.name !== 'EVERYONE'
        );
      } catch (e) {
        console.error(e);
        return [];
      }
    },
    defaultValue: [],
  });

  /**
   * List of assignable profiles for the group, excluding the `guest` and
   * `EVERYONE` pseudo-profiles.
   */
  public readonly profiles = this.profilesResource.value;
  /** Reactive form holding the selected profile and notification flags. */
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
  /** `true` while the profile change is being submitted to the backend. */
  public processing = signal(false);
  /** Snapshot of the member as it was before any change was applied. */
  public oldMember!: UserProfile;

  /**
   * Angular lifecycle hook. Reacts to changes of the {@link member} input by
   * pre-selecting the member's current profile in the form and refreshing the
   * {@link oldMember} snapshot.
   *
   * @param changes The set of input property changes reported by Angular.
   */
  ngOnChanges(changes: SimpleChanges): void {
    if (changes.member) {
      this.changeProfileForm.controls.selectedProfile.setValue(
        changes.member.currentValue.profile.id
      );
      this.oldMember = { ...this.member() };
    }
  }

  /**
   * Cancels the profile change: closes the modal and emits a
   * {@link ActionType.CHANGE_PROFILE} result marked as
   * {@link ActionResult.CANCELED} through {@link modalHide}.
   */
  public cancel() {
    this.showModal.set(false);
    const res: ActionEmitterResult = {};
    res.type = ActionType.CHANGE_PROFILE;
    res.result = ActionResult.CANCELED;
    this.modalHide.emit(res);
  }

  /**
   * Submits the profile change to the backend. Builds a
   * {@link MembershipPostDefinition} from the form values and the currently
   * selected profile, persists it via {@link MembersService.putMember}, then
   * emits the outcome ({@link ActionResult.SUCCEED} or
   * {@link ActionResult.FAILED}) through {@link modalHide}. On success the
   * modal is closed. Any error from the request is caught and reported as a
   * failed result.
   *
   * @returns A promise that resolves once the request has completed and the
   * result has been emitted.
   */
  public async changeProfile() {
    this.processing.set(true);

    const body: MembershipPostDefinition = {};
    body.adminNotifications = this.changeProfileForm.value.adminNotifications;
    body.userNotifications = this.changeProfileForm.value.userNotifications;
    const newMember = this.member();
    newMember.profile = this.getProfile();
    body.memberships = [newMember];

    const res: ActionEmitterResult = {};
    res.type = ActionType.CHANGE_PROFILE;

    try {
      await this.membersService.putMemberAsync({
        id: this.groupId(),
        membershipPostDefinition: body,
      });
      res.result = ActionResult.SUCCEED;
      this.showModal.set(false);
    } catch (error) {
      console.error(error);
      res.result = ActionResult.FAILED;
    }

    this.modalHide.emit(res);
    this.processing.set(false);
  }

  /**
   * Resolves the {@link Profile} currently selected in the form by matching
   * its id against the loaded {@link profiles} list.
   *
   * @returns The matching profile, or `undefined` if no profile matches the
   * current selection.
   */
  public getProfile(): Profile | undefined {
    for (const profile of this.profiles()) {
      if (this.changeProfileForm.value.selectedProfile === profile.id) {
        return profile;
      }
    }

    return undefined;
  }

  /**
   * Computes a human-readable, localized title for a member's profile.
   * Falls back to the profile's raw name when no localized title is
   * available, and to an empty string when the member has no profile.
   *
   * @param member The member whose profile title should be resolved.
   * @returns The localized profile title, the profile name as a fallback, or
   * an empty string when no profile/name is available.
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
