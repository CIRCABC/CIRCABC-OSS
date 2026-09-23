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
  RestoreNodeMetadata,
} from 'app/core/generated/circabc';
import { FilePickerComponent } from 'app/shared/file-picker/file-picker.component';
import { ModalComponent } from 'app/shared/modal/modal.component';

/**
 * Modal component that lets a group administrator restore one or more archived
 * (deleted) nodes back into an interest group's library.
 *
 * It renders a {@link ModalComponent} listing the restorable nodes and, when a
 * target folder must be chosen, embeds a {@link FilePickerComponent} to select
 * the destination folder. Restoration is delegated to the generated
 * {@link ArchiveService}, and the outcome of the user's action is communicated
 * to the parent through the {@link cancelRestore} and {@link finishRestore}
 * outputs.
 */
@Component({
  selector: 'cbc-restore-item',
  templateUrl: './restore-item.component.html',
  styleUrl: './restore-item.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ModalComponent, FilePickerComponent, TranslocoModule],
})
export class RestoreItemComponent {
  /** Generated API client used to perform the restore operation. */
  private readonly archiveService = inject(ArchiveService);

  /** Two-way bound flag controlling the visibility of the restore modal. */
  showModal = model(false);
  /** Archived nodes that will be restored when {@link restore} is invoked. */
  readonly restorableNodes = input<ArchiveNode[]>([]);

  /** Interest group into which the archived nodes are restored (required). */
  currentIg = input.required<InterestGroup>();
  /** Emitted when the user cancels the restore, carrying a CANCELED result. */
  readonly cancelRestore = output<ActionEmitterResult>();
  /**
   * Emitted once the restore attempt completes, carrying a SUCCEED or FAILED
   * result depending on the outcome.
   */
  readonly finishRestore = output<ActionEmitterResult>();

  /** Whether a restore operation is currently in progress. */
  public readonly processing = signal(false);
  /** Ids of the folder nodes selected as the restore target. */
  public selectedNodes: string[] = [];
  /** Whether the folder picker is currently displayed. */
  public folderPicker = false;

  /**
   * Determines whether the given archived node represents a folder.
   *
   * @param node - The archived node to inspect.
   * @returns `true` when the node's type contains `'folder'`, otherwise `false`.
   */
  isFolder(node: ArchiveNode): boolean {
    if (node.type) {
      return node.type.includes('folder');
    }
    return false;
  }

  /**
   * Determines whether the given archived node represents an external link.
   *
   * @param node - The archived node to inspect.
   * @returns `true` when the node has an HTML mimetype and a non-empty URL
   * property, otherwise `false`.
   */
  isLink(node: ArchiveNode): boolean {
    if (node.properties?.mimetype && node.properties.url) {
      return (
        node.properties.mimetype === 'text/html' && node.properties.url !== ''
      );
    }
    return false;
  }

  /**
   * Restores every node in {@link restorableNodes} into the current interest
   * group, using the first entry of {@link selectedNodes} as the target folder.
   *
   * Sets {@link processing} while running and emits the outcome through
   * {@link finishRestore} with a SUCCEED result when all nodes are restored, or
   * a FAILED result if any restore call throws.
   *
   * @returns A promise that resolves once all restore requests have completed
   * and the result has been emitted.
   */
  async restore() {
    this.processing.set(true);
    const res: ActionEmitterResult = {};
    res.type = ActionType.RESTORE_CONTENT;

    try {
      for (const node of this.restorableNodes()) {
        const body: RestoreNodeMetadata = {
          archiveNodeId: node.id,
          targetFolderId: this.selectedNodes[0] ?? '',
        };

        await this.archiveService.restoreDocumentAsync({
          id: this.currentIg().id as string,
          restoreNodeMetadata: body,
        });
      }

      res.result = ActionResult.SUCCEED;
    } catch {
      res.result = ActionResult.FAILED;
    }
    this.finishRestore.emit(res);
    this.processing.set(false);
  }

  /**
   * Aborts the restore flow and notifies the parent by emitting a CANCELED
   * result through {@link cancelRestore}.
   */
  onCancel() {
    const res: ActionEmitterResult = {};
    res.result = ActionResult.CANCELED;
    res.type = ActionType.RESTORE_CONTENT;
    this.cancelRestore.emit(res);
  }

  /** Displays the folder picker so a restore target folder can be chosen. */
  showFolderPicker() {
    this.folderPicker = true;
  }

  /**
   * Hides the folder picker and clears any previously selected target folder.
   */
  hideFolderPicker() {
    this.folderPicker = false;
    this.selectedNodes = [];
  }
}
