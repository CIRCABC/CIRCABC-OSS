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
} from 'app/core/generated/circabc';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-purge-item',
  templateUrl: './purge-item.component.html',
  styleUrls: ['./purge-item.component.scss'],
  preserveWhitespaces: true,
})
export class PurgeItemComponent {
  @Input()
  showModal = false;
  @Input()
  purgeableNodes: ArchiveNode[] = [];
  @Input()
  currentIg!: InterestGroup;
  @Output()
  readonly cancel = new EventEmitter<ActionEmitterResult>();
  @Output()
  readonly finish = new EventEmitter<ActionEmitterResult>();

  public processing = false;

  constructor(private archiveService: ArchiveService) {}

  isFolder(node: ArchiveNode): boolean {
    if (node.type) {
      return node.type.indexOf('folder') !== -1;
    } else {
      return false;
    }
  }

  async purge() {
    this.processing = true;
    const res: ActionEmitterResult = {};
    res.type = ActionType.PURGE_CONTENT;

    try {
      for (const node of this.purgeableNodes) {
        if (node.id) {
          await firstValueFrom(
            this.archiveService.deleteDeletedDocument(
              this.currentIg.id as string,
              node.id
            )
          );
        }
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
    res.type = ActionType.PURGE_CONTENT;
    this.showModal = false;
    this.cancel.emit(res);
  }
}
