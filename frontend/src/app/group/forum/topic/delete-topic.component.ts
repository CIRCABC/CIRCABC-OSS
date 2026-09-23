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
import { Node as ModelNode, TopicService } from 'app/core/generated/circabc';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';

/**
 * Confirmation dialog component for deleting a forum topic.
 *
 * Renders a modal (backed by {@link SpinnerComponent} while the operation is
 * in progress) that lets the user confirm or cancel deletion of a topic node.
 * When confirmed, it calls the backend {@link TopicService} to delete the
 * topic and reports the outcome to the parent component through the
 * `modalHide` output as an {@link ActionEmitterResult}.
 *
 * Key collaborators:
 * - {@link TopicService}: performs the actual topic deletion via the CIRCABC API.
 * - {@link ActionEmitterResult} / {@link ActionResult} / {@link ActionType}:
 *   describe the result emitted back to the parent.
 */
@Component({
  selector: 'cbc-delete-topic',
  templateUrl: './delete-topic.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [SpinnerComponent, I18nPipe, TranslocoModule],
})
export class DeleteTopicComponent {
  /** Backend service used to delete the topic node. */
  private readonly topicService = inject(TopicService);

  /** Required input: the topic node to be deleted. */
  readonly topic = input.required<ModelNode>();
  /** Two-way bindable flag controlling the visibility of the confirmation modal. */
  showModal = model<boolean>(false);
  /**
   * Emits once the dialog is dismissed, carrying the outcome of the delete
   * action (succeeded, failed or canceled) to the parent component.
   */
  readonly modalHide = output<ActionEmitterResult>();

  /** True while a delete request is in flight; used to show the spinner and disable actions. */
  public readonly deleting = signal(false);

  /**
   * Deletes the current topic via {@link TopicService}.
   *
   * Sets {@link deleting} while the request runs, closes the modal, and emits
   * a {@link ActionType.DELETE_TOPIC} result on `modalHide` indicating whether
   * the deletion succeeded or failed. Any error from the service is caught and
   * reported as {@link ActionResult.FAILED} rather than being rethrown.
   *
   * @returns A promise that resolves once the deletion attempt completes and
   * the result has been emitted.
   */
  public async delete() {
    this.deleting.set(true);

    const result: ActionEmitterResult = {};
    result.type = ActionType.DELETE_TOPIC;

    const topic = this.topic();
    if (topic.id) {
      try {
        await this.topicService.deleteTopicAsync({ id: topic.id });
        result.result = ActionResult.SUCCEED;
      } catch (error) {
        console.error(error);
        result.result = ActionResult.FAILED;
      }
    }
    this.deleting.set(false);
    this.showModal.set(false);
    this.modalHide.emit(result);
  }

  /**
   * Cancels the deletion, closing the modal without contacting the backend.
   *
   * Emits a {@link ActionType.DELETE_TOPIC} result with
   * {@link ActionResult.CANCELED} on `modalHide` so the parent can react to the
   * dismissal.
   *
   * @param _action The originating cancel action identifier (unused).
   */
  public cancelWizard(_action: string): void {
    this.showModal.set(false);
    const result: ActionEmitterResult = {};
    result.result = ActionResult.CANCELED;
    result.type = ActionType.DELETE_TOPIC;
    this.modalHide.emit(result);
  }
}
