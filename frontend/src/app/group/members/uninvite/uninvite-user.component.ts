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
import { MembersService, User } from 'app/core/generated/circabc';
import { ModalComponent } from 'app/shared/modal/modal.component';

/**
 * Confirmation dialog component used to remove (uninvite) a pending or existing
 * member from an interest group.
 *
 * Rendered as a modal (via {@link ModalComponent}) that asks the user to confirm
 * the removal of the given {@link User} from the group identified by `groupId`.
 * On confirmation it delegates the deletion to the {@link MembersService} and
 * notifies the parent component of the outcome through the `modalHide` output.
 * Translated labels are provided through Transloco.
 */
@Component({
  selector: 'cbc-uninvite-user',
  templateUrl: './uninvite-user.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ModalComponent, TranslocoModule],
})
export class UninviteUserComponent {
  /** Backend API client used to delete the member from the group. */
  private readonly membersService = inject(MembersService);

  /** The user to be removed from the group. Optional; the action is a no-op when undefined. */
  readonly user = input<User>();
  /** Identifier of the group the user should be removed from. Required. */
  readonly groupId = input.required<string>();
  /**
   * Two-way bound flag controlling the visibility of the confirmation dialog.
   * Set to `false` when the dialog is dismissed or the action completes.
   */
  readonly showDialog = model.required<boolean>();
  /**
   * Emitted when the dialog closes, reporting the outcome of the interaction
   * (cancellation or successful membership removal) to the parent component.
   */
  public readonly modalHide = output<ActionEmitterResult>();

  /** Indicates whether a removal request is currently in progress. */
  public readonly removing = signal(false);

  /**
   * Handles cancellation of the wizard/dialog.
   *
   * When invoked with the `'close'` action, it hides the dialog and emits a
   * cancellation result through `modalHide`. Any other value is ignored.
   *
   * @param backTo Navigation/action hint; `'close'` triggers dismissal.
   */
  public cancelWizard(backTo: string) {
    if (backTo === 'close') {
      this.showDialog.set(false);
      this.modalHide.emit({ result: ActionResult.CANCELED });
    }
  }

  /**
   * Removes the current user from the group.
   *
   * Does nothing unless both `groupId` and the user's `userId` are defined.
   * While the request is in flight the `removing` flag is set. On success it
   * hides the dialog and emits a {@link ActionResult.SUCCEED} result of type
   * {@link ActionType.REMOVE_MEMBERSHIP} through `modalHide`.
   *
   * @returns A promise that resolves once the removal request has completed
   *   (or immediately when the required identifiers are missing).
   */
  public async uninviteMember() {
    const groupId = this.groupId();
    const user = this.user();
    if (groupId !== undefined && user?.userId !== undefined) {
      this.removing.set(true);
      await this.membersService.deleteMemberAsync({
        id: groupId,
        userId: user.userId,
      });

      this.showDialog.set(false);

      const result: ActionEmitterResult = {};
      result.result = ActionResult.SUCCEED;
      result.type = ActionType.REMOVE_MEMBERSHIP;

      this.modalHide.emit(result);
      this.removing.set(false);
    }
  }
}
