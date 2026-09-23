import {
  ChangeDetectionStrategy,
  Component,
  inject,
  model,
  output,
  signal,
} from '@angular/core';

import { TranslocoModule, TranslocoService } from '@jsverse/transloco';

import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { Profile, ProfileService } from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { getErrorTranslation } from 'app/core/util';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';

/**
 * Confirmation dialog component for deleting an access {@link Profile} within an
 * interest group.
 *
 * Renders a modal (via {@link ModalComponent}) prompting the user to confirm the
 * deletion of the currently bound profile. On confirmation it delegates the
 * removal to the backend through {@link ProfileService}, surfaces any failure to
 * the user via {@link UiMessageService} (with a translated message resolved
 * through {@link TranslocoService}), and reports the outcome to the host
 * component using the {@link DeleteProfileComponent.profileDeleted} output.
 *
 * Key collaborators:
 * - {@link ProfileService} — performs the actual profile deletion request.
 * - {@link UiMessageService} — displays error notifications on failure.
 * - {@link TranslocoService} — resolves the localized error message.
 */
@Component({
  selector: 'cbc-delete-profile',
  templateUrl: './delete-profile.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ModalComponent, I18nPipe, TranslocoModule],
})
export class DeleteProfileComponent {
  /** Backend service used to delete the profile. */
  private readonly profileService = inject(ProfileService);
  /** Service used to display error notifications to the user. */
  private readonly uiMessageService = inject(UiMessageService);
  /** Service used to resolve localized error messages. */
  private readonly translateService = inject(TranslocoService);

  /**
   * Two-way bound model holding the {@link Profile} to be deleted. Cleared
   * (set to `undefined`) once the deletion succeeds or the dialog is cancelled.
   */
  readonly profile = model<Profile>();
  /**
   * Two-way bound model controlling the visibility of the confirmation modal.
   * Set to `false` to close the dialog.
   */
  readonly showModal = model(false);
  /**
   * Output emitting an {@link ActionEmitterResult} once the deletion flow
   * completes, indicating whether the profile was deleted, or the action was
   * cancelled.
   */
  readonly profileDeleted = output<ActionEmitterResult>();

  /**
   * Indicates whether a deletion request is currently in progress. Used to
   * reflect the pending state in the template.
   */
  public readonly deleting = signal(false);

  /**
   * Cancels the deletion flow: closes the modal, clears the bound profile and
   * emits a {@link ActionEmitterResult} with a {@link ActionResult.CANCELED}
   * result for the {@link ActionType.DELETE_PROFILE} action.
   */
  cancelWizard() {
    this.showModal.set(false);
    this.profile.set(undefined);

    const result: ActionEmitterResult = {};
    result.type = ActionType.DELETE_PROFILE;
    result.result = ActionResult.CANCELED;

    this.profileDeleted.emit(result);
  }

  /**
   * Confirms and performs the deletion of the currently bound profile.
   *
   * When a profile with a valid id is present, issues the delete request via
   * {@link ProfileService}. On success, emits a {@link ActionResult.SUCCEED}
   * result, closes the modal and clears the bound profile. On failure, displays
   * a translated error message via {@link UiMessageService} without emitting a
   * result. The {@link DeleteProfileComponent.deleting} flag is toggled around
   * the operation.
   *
   * @returns A promise that resolves once the deletion attempt completes.
   */
  async delete() {
    this.deleting.set(true);
    const profile = this.profile();
    if (profile?.id) {
      const result: ActionEmitterResult = {};
      result.type = ActionType.DELETE_PROFILE;

      try {
        await this.profileService.deleteProfileAsync({ id: profile.id });
        result.result = ActionResult.SUCCEED;
        this.profileDeleted.emit(result);
        this.showModal.set(false);
        this.profile.set(undefined);
      } catch (error) {
        console.error(error);
        const res = this.translateService.translate(
          getErrorTranslation(ActionType.DELETE_PROFILE)
        );
        this.uiMessageService.addErrorMessage(res);
      }
    }

    this.deleting.set(false);
  }
}
