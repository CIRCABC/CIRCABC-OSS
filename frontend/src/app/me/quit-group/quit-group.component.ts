import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  linkedSignal,
  output,
  signal,
} from '@angular/core';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { ActionType } from 'app/action-result';
import { type InterestGroup, MembersService } from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { getErrorTranslation, getSuccessTranslation } from 'app/core/util';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';

/**
 * Confirmation dialog component that lets a user quit (leave) an interest
 * group by removing their own membership.
 *
 * Renders a modal (via {@link ModalComponent}) that displays the group being
 * left and offers confirm/cancel actions. On confirmation it delegates the
 * membership removal to the backend through {@link MembersService} and reports
 * the outcome to the user via {@link UiMessageService} using translated
 * success/error messages.
 *
 * Key collaborators:
 * - {@link MembersService}: performs the actual membership deletion call.
 * - {@link UiMessageService}: surfaces success/error notifications.
 * - {@link TranslocoService} / {@link I18nPipe}: resolve localized labels and
 *   messages.
 */
@Component({
  selector: 'cbc-quit-group',
  templateUrl: './quit-group.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ModalComponent, TranslocoModule],
})
export class QuitGroupComponent {
  /** Generated API client used to delete the user's membership. */
  private readonly membersService = inject(MembersService);
  /** Pipe used to resolve the localized value of the group's i18n title. */
  private readonly i18nPipe = inject(I18nPipe);
  /** Transloco service used to translate success/error message keys. */
  private readonly translateService = inject(TranslocoService);
  /** Service used to display success/error notifications to the user. */
  private readonly uiMessageService = inject(UiMessageService);

  /**
   * Input (aliased as `show`) controlling the initial visibility of the modal.
   * Backs the internal {@link show} signal so the component can also close the
   * dialog on its own.
   */
  // eslint-disable-next-line @angular-eslint/no-input-rename
  public readonly showInput = input(false, { alias: 'show' });
  /**
   * Writable signal driving the modal's visibility. Initialized from
   * {@link showInput} and updated internally when the dialog is confirmed or
   * cancelled.
   */
  public readonly show = linkedSignal(this.showInput);

  /** Required input: the interest group the user is about to leave. */
  public readonly group = input.required<InterestGroup>();

  /** Input: the username of the member whose membership will be removed. */
  public readonly username = input('');

  /**
   * Output emitting the modal's new visibility state whenever it is closed,
   * enabling two-way binding on the `show` property.
   */
  public readonly showChange = output<boolean>();

  /** Output emitted once the membership has been successfully removed. */
  public readonly membershipRemoved = output();

  /** Whether a membership-removal request is currently in progress. */
  public readonly processing = signal(false);

  /**
   * Removes the current user's membership from the selected group.
   *
   * Calls {@link MembersService.deleteMember} for the current group and
   * username. On success it closes the modal, emits {@link showChange} and
   * {@link membershipRemoved}, and shows a translated success message. Any
   * failure is caught and reported via a translated error message. The
   * {@link processing} flag is toggled around the operation.
   *
   * @returns A promise that resolves once the removal attempt has completed.
   */
  public async removeMembership() {
    this.processing.set(true);

    try {
      const group = this.group();
      if (group?.id) {
        await this.membersService.deleteMemberAsync({
          id: group.id,
          userId: this.username(),
        });
        this.show.set(false);
        this.showChange.emit(this.show());
        this.membershipRemoved.emit();
        const res = this.translateService.translate(
          getSuccessTranslation(ActionType.REMOVE_MEMBERSHIP)
        );
        this.uiMessageService.addSuccessMessage(res, true);
      }
    } catch (error) {
      console.error(error);
      const res = this.translateService.translate(
        getErrorTranslation(ActionType.REMOVE_MEMBERSHIP)
      );
      this.uiMessageService.addErrorMessage(res, true);
    }

    this.processing.set(false);
  }

  /**
   * Cancels the operation by closing the modal without removing the
   * membership. Emits the updated visibility state through {@link showChange}.
   */
  public cancel() {
    this.show.set(false);
    this.showChange.emit(this.show());
  }

  /**
   * Builds a human-readable label for the group being left.
   *
   * Prefers the localized group title (resolved via {@link I18nPipe}) when it
   * yields a non-empty value, otherwise falls back to the group's raw name.
   *
   * @returns The display label for the group, or an empty string if neither a
   * name nor a localized title is available.
   */
  public getGroupLabel(): string {
    let result = '';

    const group = this.group();
    if (group.name) {
      result = group.name;
    }

    if (group.title) {
      const title = this.i18nPipe.transform(group.title);

      if (title !== '') {
        result = title;
      }
    }

    return result;
  }
}
