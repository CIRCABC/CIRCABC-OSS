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
} from 'app/core/generated/circabc';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-purge-item',
  templateUrl: './purge-item.component.html',
  styleUrl: './purge-item.component.scss',
  preserveWhitespaces: true,
  imports: [ModalComponent, TranslocoModule],
})
export class PurgeItemComponent {
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  showModal = false;
  readonly purgeableNodes = input<ArchiveNode[]>([]);
  readonly currentIg = input.required<InterestGroup>();
  readonly cancelPurge = output<ActionEmitterResult>();
  readonly finishPurge = output<ActionEmitterResult>();

  public processing = false;

  constructor(private archiveService: ArchiveService) {}

  isFolder(node: ArchiveNode): boolean {
    if (node.type) {
      return node.type.indexOf('folder') !== -1;
    }
    return false;
  }

  async purge() {
    this.processing = true;
    const res: ActionEmitterResult = {};
    res.type = ActionType.PURGE_CONTENT;

    try {
      for (const node of this.purgeableNodes()) {
        if (node.id) {
          await firstValueFrom(
            this.archiveService.deleteDeletedDocument(
              this.currentIg().id as string,
              node.id
            )
          );
        }
      }
      this.showModal = false;
      res.result = ActionResult.SUCCEED;
    } catch (_error) {
      res.result = ActionResult.FAILED;
    }
    this.finishPurge.emit(res);
    this.processing = false;
  }

  onCancel() {
    const res: ActionEmitterResult = {};
    res.result = ActionResult.CANCELED;
    res.type = ActionType.PURGE_CONTENT;
    this.showModal = false;
    this.cancelPurge.emit(res);
  }
}
