import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  output,
  signal,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';

import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { TranslocoModule } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { ActionService } from 'app/action-result/action.service';
import {
  ContentService,
  Node as ModelNode,
  SpaceService,
} from 'app/core/generated/circabc';
import { ClipboardService } from 'app/group/library/clipboard/clipboard.service';
import { ModalComponent } from 'app/shared/modal/modal.component';

/**
 * Modal-based action component that deletes a library node (a space/folder,
 * a folder link or a content item) from an interest group's document library.
 *
 * The component renders a confirmation modal (via {@link ModalComponent}) with
 * a context-aware confirmation message and a slide toggle that lets the user
 * decide whether affected members should be notified about the deletion. On
 * confirmation it delegates to the appropriate backend API — {@link SpaceService}
 * for folders/folder links or {@link ContentService} for content — removes the
 * node from the {@link ClipboardService}, and broadcasts the outcome through the
 * {@link ActionService} and the {@link modalHide} output.
 *
 * Key collaborators: {@link SpaceService} and {@link ContentService} (generated
 * CIRCABC API clients), {@link ClipboardService}, {@link ActionService} and
 * Angular's {@link FormBuilder}.
 */
@Component({
  selector: 'cbc-delete-action',
  templateUrl: './delete-action.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ModalComponent,
    ReactiveFormsModule,
    MatSlideToggleModule,
    TranslocoModule,
  ],
})
export class DeleteActionComponent {
  private readonly notifyFormBuilder = inject(FormBuilder);
  private readonly spaceService = inject(SpaceService);
  private readonly contentService = inject(ContentService);
  private readonly clipboardService = inject(ClipboardService);
  private readonly actionService = inject(ActionService);

  /**
   * Required input holding the library node to delete. Its `type` determines
   * whether a space, a folder link or a content item is removed, and its `id`
   * is passed to the corresponding delete API call.
   */
  readonly node = input.required<ModelNode>();

  /**
   * Output emitted once the delete flow completes (success, failure or
   * cancellation), carrying an {@link ActionEmitterResult} describing the
   * action type and its result so the parent can react (e.g. refresh the view).
   */
  public readonly modalHide = output<ActionEmitterResult>();

  /** Whether the confirmation modal is currently visible. */
  public showModal = signal(false);

  /** Whether a delete request is in progress; used to drive UI busy state. */
  public deleting = signal(false);

  /**
   * Whether affected members should be notified of the deletion. Mirrors the
   * form toggle value and is forwarded to the delete API calls.
   */
  private notify = true;

  /**
   * Resolves the translation key for the confirmation message based on the
   * node's type: folder link, space (folder) or content.
   *
   * @returns The Transloco translation key for the confirmation text, or an
   * empty string when the node type is undefined.
   */
  public get confirmationMessage(): string {
    const node = this.node();
    if (node.type === undefined) {
      return '';
    }

    if (node.type.includes('folderlink')) {
      return 'text.delete-link.confirmation';
    }
    if (node.type.includes('folder')) {
      return 'text.delete-space.confirmation';
    }
    return 'text.delete-content.confirmation';
  }
  /** Reactive form group backing the "notify members" slide toggle. */
  notifyFormGroup = this.notifyFormBuilder.group({
    notify: true,
  });

  /**
   * Performs the deletion of the current {@link node}. Reads the notify toggle,
   * then dispatches to {@link SpaceService.deleteSpace} for folders/folder links
   * or {@link ContentService.deleteContent} for content items. On success the
   * node is removed from the clipboard, the modal is closed, and (for spaces)
   * the action is propagated via {@link ActionService}. Regardless of outcome,
   * the result is emitted through {@link modalHide}.
   *
   * Failures from the backend calls are caught internally and reported as a
   * failed {@link ActionResult} rather than being re-thrown.
   *
   * @returns A promise that resolves once the delete flow has completed and the
   * result has been emitted.
   */
  public async delete() {
    if (!this.notifyFormGroup.controls.notify.value) {
      this.notify = false;
    }
    const node = this.node();
    if (node.id) {
      this.deleting.set(true);
      if (
        node.type &&
        (node.type.includes('folder') || node.type.includes('folderlink'))
      ) {
        const result: ActionEmitterResult = {};
        result.type = ActionType.DELETE_SPACE;

        try {
          await this.spaceService.deleteSpaceAsync({
            id: node.id,
            notify: this.notify,
          });
          result.result = ActionResult.SUCCEED;
          this.clipboardService.removeItem(node);
          this.showModal.set(false);
          this.actionService.propagateActionFinished(result);
        } catch (error) {
          console.error(error);
          result.result = ActionResult.FAILED;
        }
        this.modalHide.emit(result);
      } else if (node.type && !node.type.includes('folder')) {
        const result: ActionEmitterResult = {};
        result.type = ActionType.DELETE_CONTENT;

        try {
          await this.contentService.deleteContentAsync({
            id: node.id,
            notify: this.notify,
          });
          result.result = ActionResult.SUCCEED;
          this.clipboardService.removeItem(node);
          this.showModal.set(false);
        } catch (error) {
          console.error(error);
          result.result = ActionResult.FAILED;
        }
        this.modalHide.emit(result);
      }
      this.deleting.set(false);
    }
  }

  /**
   * Cancels the delete flow, hides the modal and emits a canceled
   * {@link ActionEmitterResult} through {@link modalHide}.
   *
   * @param _action The originating action identifier from the modal; unused.
   */
  public cancelWizard(_action: string): void {
    this.showModal.set(false);

    const result: ActionEmitterResult = {};
    result.result = ActionResult.CANCELED;
    result.type = ActionType.DELETE_SPACE;

    this.modalHide.emit(result);
  }
}
