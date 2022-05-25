import { Component, EventEmitter, Input, Output } from '@angular/core';

import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import {
  ArchiveNode,
  ArchiveService,
  InterestGroup,
  RestoreNodeMetadata,
} from 'app/core/generated/circabc';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-restore-item',
  templateUrl: './restore-item.component.html',
  styleUrls: ['./restore-item.component.scss'],
  preserveWhitespaces: true,
})
export class RestoreItemComponent {
  @Input()
  showModal = false;
  @Input()
  restorableNodes: ArchiveNode[] = [];
  @Input()
  currentIg!: InterestGroup;
  @Output()
  readonly cancel = new EventEmitter<ActionEmitterResult>();
  @Output()
  readonly finish = new EventEmitter<ActionEmitterResult>();

  public processing = false;
  public selectedNodes: string[] = [];
  public folderPicker = false;

  constructor(private archiveService: ArchiveService) {}

  isFolder(node: ArchiveNode): boolean {
    if (node.type) {
      return node.type.indexOf('folder') !== -1;
    } else {
      return false;
    }
  }

  isLink(node: ArchiveNode): boolean {
    if (node.properties && node.properties.mimetype && node.properties.url) {
      return (
        node.properties.mimetype === 'text/html' && node.properties.url !== ''
      );
    } else {
      return false;
    }
  }

  async restore() {
    this.processing = true;
    const res: ActionEmitterResult = {};
    res.type = ActionType.RESTORE_CONTENT;

    try {
      for (const node of this.restorableNodes) {
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
    } catch (error) {
      res.result = ActionResult.FAILED;
    }
    this.finish.emit(res);
    this.processing = false;
  }

  onCancel() {
    const res: ActionEmitterResult = {};
    res.result = ActionResult.CANCELED;
    res.type = ActionType.RESTORE_CONTENT;
    this.showModal = false;
    this.cancel.emit(res);
  }

  showFolderPicker() {
    this.folderPicker = true;
  }

  hideFolderPicker() {
    this.folderPicker = false;
    this.selectedNodes = [];
  }
}
