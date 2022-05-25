import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  Output,
  SimpleChanges,
} from '@angular/core';

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
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-delete-multiple',
  templateUrl: './delete-multiple.component.html',
  styleUrls: ['./delete-multiple.component.scss'],
  preserveWhitespaces: true,
})
export class DeleteMultipleComponent implements OnChanges {
  @Input()
  nodes!: ModelNode[];
  @Input()
  showModal = false;
  @Output()
  public readonly modalHide = new EventEmitter<ActionEmitterResult>();

  public deleting = false;
  public progressValue = 0;
  public progressMax = 0;

  constructor(
    private spaceService: SpaceService,
    private contentService: ContentService,
    private clipboardService: ClipboardService
  ) {}

  ngOnChanges(changes: SimpleChanges) {
    this.progressValue = 0;
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

  public cancelWizard(_action: string): void {
    this.showModal = false;

    const result: ActionEmitterResult = {};
    result.result = ActionResult.CANCELED;
    result.type = ActionType.DELETE_ALL;

    this.modalHide.emit(result);
  }

  public async deleteAll() {
    this.deleting = true;
    for (const node of this.nodes) {
      if (node.id) {
        if (node.type && node.type.indexOf('folder') !== -1) {
          await firstValueFrom(this.spaceService.deleteSpace(node.id));
        } else if (node.type && node.type.indexOf('folder') === -1) {
          await firstValueFrom(this.contentService.deleteContent(node.id));
        }
        this.clipboardService.removeItem(node);
      }
      this.progressValue += 1;
    }

    this.showModal = false;
    const result: ActionEmitterResult = {};
    result.result = ActionResult.SUCCEED;
    result.type = ActionType.DELETE_ALL;
    this.modalHide.emit(result);
  }
}
