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
import {
  ArchiveNode,
  ArchiveService,
  type InterestGroup,
} from 'app/core/generated/circabc';
import { ModalComponent } from 'app/shared/modal/modal.component';

/**
 * Confirmation modal component for permanently purging (hard-deleting)
 * archived documents and folders from an interest group's recycle bin.
 *
 * Renders a {@link ModalComponent} that lists the nodes queued for purging and
 * lets the user confirm or cancel the operation. On confirmation it iterates
 * over the provided nodes and permanently removes each one through the
 * {@link ArchiveService}, then emits the aggregated outcome. It is intended to
 * be embedded in the group document administration screens.
 *
 * Key collaborators:
 * - {@link ArchiveService}: performs the actual permanent deletion of each
 *   archived node.
 * - {@link ModalComponent}: provides the modal shell used by the template.
 */
@Component({
  selector: 'cbc-purge-item',
  templateUrl: './purge-item.component.html',
  styleUrl: './purge-item.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ModalComponent, TranslocoModule],
})
export class PurgeItemComponent {
  /** Service used to permanently delete archived (recycle-bin) documents. */
  private readonly archiveService = inject(ArchiveService);

  /**
   * Two-way bindable flag controlling the visibility of the confirmation
   * modal. `true` shows the modal, `false` hides it.
   */
  showModal = model(false);
  /**
   * Input list of archived nodes to be purged when the user confirms the
   * operation. Defaults to an empty array.
   */
  readonly purgeableNodes = input<ArchiveNode[]>([]);
  /**
   * Required input identifying the interest group that owns the archived
   * nodes; its id is used when issuing the delete requests.
   */
  readonly currentIg = input.required<InterestGroup>();
  /**
   * Output emitted when the user cancels the purge. Carries an
   * {@link ActionEmitterResult} with a `CANCELED` result.
   */
  readonly cancelPurge = output<ActionEmitterResult>();
  /**
   * Output emitted once the purge attempt completes. Carries an
   * {@link ActionEmitterResult} indicating whether the operation succeeded or
   * failed.
   */
  readonly finishPurge = output<ActionEmitterResult>();

  /**
   * Indicates whether a purge operation is currently in progress. Used to
   * drive loading/disabled states in the template.
   */
  public readonly processing = signal(false);

  /**
   * Determines whether the given archived node represents a folder.
   *
   * @param node - The archived node to inspect.
   * @returns `true` if the node's type includes `'folder'`; otherwise `false`
   * (including when the node has no type).
   */
  isFolder(node: ArchiveNode): boolean {
    if (node.type) {
      return node.type.includes('folder');
    }
    return false;
  }

  /**
   * Permanently deletes every node in {@link purgeableNodes} for the current
   * interest group by calling {@link ArchiveService.deleteDeletedDocument} for
   * each node that has an id.
   *
   * Sets {@link processing} to `true` while running and back to `false` when
   * finished. Deletion errors are caught internally and reflected in the
   * emitted result rather than being thrown. Always emits {@link finishPurge}
   * with an {@link ActionEmitterResult} of type `PURGE_CONTENT` whose result is
   * `SUCCEED` when all deletions complete or `FAILED` if any deletion throws.
   *
   * @returns A promise that resolves once all deletions have been attempted
   * and the outcome has been emitted.
   */
  async purge() {
    this.processing.set(true);
    const res: ActionEmitterResult = {};
    res.type = ActionType.PURGE_CONTENT;

    try {
      for (const node of this.purgeableNodes()) {
        if (node.id) {
          await this.archiveService.deleteDeletedDocumentAsync({
            id: this.currentIg().id as string,
            nodeId: node.id,
          });
        }
      }
      res.result = ActionResult.SUCCEED;
    } catch (error) {
      console.error(error);
      res.result = ActionResult.FAILED;
    }
    this.finishPurge.emit(res);
    this.processing.set(false);
  }

  /**
   * Cancels the purge operation without deleting anything and notifies the
   * parent by emitting {@link cancelPurge} with an {@link ActionEmitterResult}
   * of type `PURGE_CONTENT` and result `CANCELED`.
   */
  onCancel() {
    const res: ActionEmitterResult = {};
    res.result = ActionResult.CANCELED;
    res.type = ActionType.PURGE_CONTENT;
    this.cancelPurge.emit(res);
  }
}
