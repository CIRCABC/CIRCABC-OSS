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
import { ActionService } from 'app/action-result/action.service';
import { ForumService, Node as ModelNode } from 'app/core/generated/circabc';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';

/**
 * Standalone component that renders a confirmation modal for deleting a forum.
 *
 * The modal presents the user with a confirmation prompt and, once confirmed,
 * deletes the forum through the backend {@link ForumService}. While the request
 * is in flight a {@link SpinnerComponent} is displayed. On completion (success,
 * failure or cancellation) the outcome is emitted via {@link modalHide} and also
 * broadcast application-wide through the {@link ActionService}.
 *
 * @remarks
 * Key collaborators:
 * - {@link ForumService} — performs the actual forum deletion REST call.
 * - {@link ActionService} — propagates the finished action to other listeners.
 */
@Component({
  selector: 'cbc-delete-forum',
  templateUrl: './delete-forum.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [SpinnerComponent, I18nPipe, TranslocoModule],
})
export class DeleteForumComponent {
  /** Generated API client used to perform the forum deletion request. */
  private readonly forumService = inject(ForumService);
  /** Service used to broadcast the completed delete action to the rest of the app. */
  private readonly actionService = inject(ActionService);

  /**
   * Required input holding the forum node to be deleted.
   * Its `id` is used to issue the deletion request.
   */
  readonly forum = input.required<ModelNode>();

  /**
   * Two-way bindable model controlling the visibility of the confirmation modal.
   * Set to `false` once the delete or cancel flow completes.
   */
  showModal = model(false);
  /**
   * Output emitted when the modal is dismissed, carrying the outcome of the
   * action (success, failure or cancellation) as an {@link ActionEmitterResult}.
   */
  readonly modalHide = output<ActionEmitterResult>();

  /** Whether a deletion request is currently in progress; drives the spinner display. */
  public readonly deleting = signal(false);

  /**
   * Confirms and performs the forum deletion.
   *
   * Sets {@link deleting} to `true`, calls {@link ForumService.deleteForum}
   * with the forum's id and records whether it succeeded or failed. Regardless
   * of the outcome, it hides the modal, emits the result via {@link modalHide}
   * and propagates it through the {@link ActionService}. Deletion is only
   * attempted when the forum has an `id`.
   *
   * @returns A promise that resolves once the deletion flow has completed and
   * the result has been emitted.
   */
  public async delete() {
    this.deleting.set(true);

    const result: ActionEmitterResult = {};
    result.type = ActionType.DELETE_FORUM;

    const forum = this.forum();
    if (forum.id) {
      try {
        await this.forumService.deleteForumAsync({ id: forum.id });
        result.result = ActionResult.SUCCEED;
      } catch (error) {
        console.error(error);
        result.result = ActionResult.FAILED;
      }
    }
    this.deleting.set(false);
    this.showModal.set(false);
    this.modalHide.emit(result);
    this.actionService.propagateActionFinished(result);
  }

  /**
   * Cancels the deletion flow without deleting the forum.
   *
   * Hides the modal and emits a {@link ActionEmitterResult} with a
   * {@link ActionResult.CANCELED} result via {@link modalHide}.
   *
   * @param _action - The cancellation action identifier (unused).
   */
  public cancelWizard(_action: string): void {
    this.showModal.set(false);
    const result: ActionEmitterResult = {};
    result.result = ActionResult.CANCELED;
    result.type = ActionType.DELETE_FORUM;
    this.modalHide.emit(result);
  }
}
