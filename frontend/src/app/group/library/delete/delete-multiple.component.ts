import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  OnChanges,
  output,
  SimpleChanges,
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
import {
  ContentService,
  Node as ModelNode,
  SpaceService,
} from 'app/core/generated/circabc';
import { ClipboardService } from 'app/group/library/clipboard/clipboard.service';
import { DataCyDirective } from 'app/shared/directives/data-cy.directive';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';

/**
 * Modal component that performs bulk deletion of library nodes (folders and
 * files) from an interest group.
 *
 * Rendered as a confirmation dialog, it displays a progress indicator while
 * iterating over the provided {@link nodes} and deleting each one. Folders are
 * removed through the {@link SpaceService} and non-folder content through the
 * {@link ContentService}. Each deleted node is also removed from the shared
 * clipboard via the {@link ClipboardService}. A slide toggle lets the user
 * choose whether subscribers are notified of the deletion. When the operation
 * finishes (or is cancelled), the outcome is emitted through {@link modalHide}
 * so the parent can close the dialog.
 *
 * Key collaborators: {@link SpaceService}, {@link ContentService} and
 * {@link ClipboardService}.
 */
@Component({
  selector: 'cbc-delete-multiple',
  templateUrl: './delete-multiple.component.html',
  styleUrl: './delete-multiple.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ReactiveFormsModule,
    MatSlideToggleModule,
    SpinnerComponent,
    DataCyDirective,
    TranslocoModule,
  ],
})
export class DeleteMultipleComponent implements OnChanges {
  /** Reactive-forms factory used to build the notification toggle form. */
  private readonly notifyFormBuilder = inject(FormBuilder);
  /** API client used to delete folder (space) nodes. */
  private readonly spaceService = inject(SpaceService);
  /** API client used to delete content (file) nodes. */
  private readonly contentService = inject(ContentService);
  /** Shared clipboard service; deleted nodes are removed from it. */
  private readonly clipboardService = inject(ClipboardService);

  /**
   * Required input: the list of library nodes (folders and/or files) to
   * delete. Its length drives the maximum value of the progress indicator.
   */
  readonly nodes = input.required<ModelNode[]>();

  /** Input controlling whether the deletion modal is visible. */
  readonly showModal = input(false);
  /**
   * Output emitted when the modal should close, carrying the outcome of the
   * operation (either {@link ActionResult.SUCCEED} or
   * {@link ActionResult.CANCELED}) for the {@link ActionType.DELETE_ALL} action.
   */
  public readonly modalHide = output<ActionEmitterResult>();

  /** Whether a deletion is currently in progress (drives the spinner/UI state). */
  public deleting = false;
  /** Number of nodes deleted so far; current value of the progress bar. */
  public progressValue = signal(0);
  /** Total number of nodes to delete; maximum value of the progress bar. */
  public progressMax = 0;
  /** Whether subscribers should be notified of the deletion. */
  private notify = true;
  /** Reactive form backing the "notify subscribers" slide toggle. */
  notifyFormGroup = this.notifyFormBuilder.group({
    notify: true,
  });

  /**
   * Angular lifecycle hook reacting to input changes. Resets the progress and
   * deleting state, and recomputes {@link progressMax} from the current
   * {@link nodes} value.
   *
   * @param changes - The set of changed input properties for this component.
   */
  ngOnChanges(changes: SimpleChanges) {
    this.progressValue.set(0);
    this.deleting = false;
    const chng = changes.nodes;
    if (chng) {
      if (chng.currentValue) {
        this.progressMax = (chng.currentValue as ModelNode[]).length;
      } else {
        this.progressMax = 0;
      }
    }
  }

  /**
   * Cancels the deletion wizard without deleting anything and requests the
   * modal to close.
   *
   * @param _action - Unused action identifier from the triggering control.
   */
  public cancelWizard(_action: string): void {
    const result: ActionEmitterResult = {};
    result.result = ActionResult.CANCELED;
    result.type = ActionType.DELETE_ALL;

    this.modalHide.emit(result);
  }

  /**
   * Deletes every node in {@link nodes} sequentially. Folder nodes are removed
   * via {@link SpaceService.deleteSpace} and file nodes via
   * {@link ContentService.deleteContent}, honoring the user's notification
   * preference. Each processed node is removed from the clipboard and
   * {@link progressValue} is advanced. On completion, emits a success result
   * through {@link modalHide}.
   *
   * @returns A promise that resolves once all nodes have been deleted and the
   * result has been emitted.
   */
  public async deleteAll() {
    if (!this.notifyFormGroup.controls.notify.value) {
      this.notify = false;
    }
    this.deleting = true;
    for (const node of this.nodes()) {
      if (node.id) {
        if (node.type?.includes('folder')) {
          await this.spaceService.deleteSpaceAsync({
            id: node.id,
            notify: this.notify,
          });
        } else if (node.type && !node.type.includes('folder')) {
          await this.contentService.deleteContentAsync({
            id: node.id,
            notify: this.notify,
          });
        }
        this.clipboardService.removeItem(node);
      }
      this.progressValue.update((value) => value + 1);
    }

    const result: ActionEmitterResult = {};
    result.result = ActionResult.SUCCEED;
    result.type = ActionType.DELETE_ALL;
    this.modalHide.emit(result);
  }
}
