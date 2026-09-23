import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  model,
  output,
  signal,
} from '@angular/core';

import { TranslocoModule } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { MembersService } from 'app/core/generated/circabc';
import { SelectableUserProfile } from 'app/core/ui-model/index';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';

/**
 * Modal component that lets an administrator remove (uninvite) several group
 * members at once.
 *
 * It renders a confirmation dialog (via {@link ModalComponent}) listing the
 * selected users and, on confirmation, deletes each member's membership from
 * the group sequentially through the {@link MembersService}. The outcome of the
 * operation (success, failure or cancellation) is reported to the parent
 * component through the {@link UninviteMultipleComponent.modalHide} output.
 */
@Component({
  selector: 'cbc-uninvite-multiple',
  templateUrl: './uninvite-multiple.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ModalComponent, I18nPipe, TranslocoModule],
})
export class UninviteMultipleComponent {
  /** Generated API client used to delete group memberships. */
  private readonly membersService = inject(MembersService);

  /**
   * Flag indicating whether a deletion operation is currently in progress.
   * Used by the template to show progress feedback and disable actions.
   */
  public readonly deleting = signal(false);

  /** Required input: the users whose memberships should be removed. */
  readonly users = input.required<SelectableUserProfile[]>();

  /** Required input: the identifier of the group to remove the members from. */
  readonly groupId = input.required<string>();

  /**
   * Required two-way bound model controlling the visibility of the modal.
   * Set to `false` once the operation completes or is cancelled.
   */
  readonly showModal = model.required<boolean>();

  /**
   * Emits the result of the uninvite operation (succeeded, failed or
   * cancelled) so the parent component can react accordingly.
   */
  public readonly modalHide = output<ActionEmitterResult>();

  /**
   * Removes every selected user's membership from the group.
   *
   * Iterates over {@link users} and calls the members service for each user
   * that has a valid user id. On completion it emits a
   * {@link ActionType.REMOVE_MEMBERSHIPS} result via {@link modalHide}
   * (with {@link ActionResult.SUCCEED} or {@link ActionResult.FAILED}) and
   * hides the modal. Errors thrown by the service are caught internally and
   * reported as a failed result rather than being propagated.
   *
   * @returns A promise that resolves once all deletions have been attempted
   * and the result has been emitted.
   */
  async deleteMembers() {
    const result: ActionEmitterResult = {};
    result.type = ActionType.REMOVE_MEMBERSHIPS;
    this.deleting.set(true);

    try {
      for (const member of this.users()) {
        if (member.user?.userId) {
          await this.membersService.deleteMemberAsync({
            id: this.groupId(),
            userId: member.user.userId,
          });
        }
      }

      result.result = ActionResult.SUCCEED;
    } catch (error) {
      console.error(error);
      result.result = ActionResult.FAILED;
    }

    this.modalHide.emit(result);
    this.deleting.set(false);
    this.showModal.set(false);
  }

  /**
   * Cancels the operation without removing any membership.
   *
   * Emits a {@link ActionType.REMOVE_MEMBERSHIPS} result with
   * {@link ActionResult.CANCELED} via {@link modalHide} and hides the modal.
   */
  public cancel() {
    const result: ActionEmitterResult = {};
    result.type = ActionType.REMOVE_MEMBERSHIPS;
    result.result = ActionResult.CANCELED;
    this.modalHide.emit(result);
    this.showModal.set(false);
  }
}
