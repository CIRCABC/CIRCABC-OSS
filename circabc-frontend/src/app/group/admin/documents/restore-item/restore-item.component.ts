import { Component, Input, output, input } from '@angular/core';

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
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-restore-item',
  templateUrl: './restore-item.component.html',
  styleUrl: './restore-item.component.scss',
  preserveWhitespaces: true,
  imports: [ModalComponent, FilePickerComponent, TranslocoModule],
})
export class RestoreItemComponent {
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  showModal = false;
  readonly restorableNodes = input<ArchiveNode[]>([]);
  // TODO: Skipped for migration because:
  //  This input is used in a control flow expression (e.g. `@if` or `*ngIf`)
  //  and migrating would break narrowing currently.
  @Input()
  currentIg!: InterestGroup;
  readonly cancelRestore = output<ActionEmitterResult>();
  readonly finishRestore = output<ActionEmitterResult>();

  public processing = false;
  public selectedNodes: string[] = [];
  public folderPicker = false;

  constructor(private archiveService: ArchiveService) {}

  isFolder(node: ArchiveNode): boolean {
    if (node.type) {
      return node.type.indexOf('folder') !== -1;
    }
    return false;
  }

  isLink(node: ArchiveNode): boolean {
    if (node.properties?.mimetype && node.properties.url) {
      return (
        node.properties.mimetype === 'text/html' && node.properties.url !== ''
      );
    }
    return false;
  }

  async restore() {
    this.processing = true;
    const res: ActionEmitterResult = {};
    res.type = ActionType.RESTORE_CONTENT;

    try {
      for (const node of this.restorableNodes()) {
        const body: RestoreNodeMetadata = {
          archiveNodeId: node.id,
          targetFolderId:
            this.selectedNodes[0] !== undefined ? this.selectedNodes[0] : '',
        };

        await firstValueFrom(
          this.archiveService.restoreDocument(this.currentIg.id as string, body)
        );
      }

      this.showModal = false;
      res.result = ActionResult.SUCCEED;
    } catch {
      res.result = ActionResult.FAILED;
    }
    this.finishRestore.emit(res);
    this.processing = false;
  }

  onCancel() {
    const res: ActionEmitterResult = {};
    res.result = ActionResult.CANCELED;
    res.type = ActionType.RESTORE_CONTENT;
    this.showModal = false;
    this.cancelRestore.emit(res);
  }

  showFolderPicker() {
    this.folderPicker = true;
  }

  hideFolderPicker() {
    this.folderPicker = false;
    this.selectedNodes = [];
  }
}
