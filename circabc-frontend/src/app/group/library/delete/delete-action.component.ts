import { Component, EventEmitter, Input, Output } from '@angular/core';

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
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-delete-action',
  templateUrl: './delete-action.component.html',
  preserveWhitespaces: true,
})
export class DeleteActionComponent {
  @Input()
  node!: ModelNode;
  @Output()
  public readonly modalHide = new EventEmitter<ActionEmitterResult>();

  public showModal = false;
  public deleting = false;

  public get confirmationMessage(): string {
    if (this.node.type === undefined) {
      return '';
    }

    if (this.node.type.indexOf('folderlink') !== -1) {
      return 'text.delete-link.confirmation';
    } else if (this.node.type.indexOf('folder') !== -1) {
      return 'text.delete-space.confirmation';
    } else {
      return 'text.delete-content.confirmation';
    }
  }
  constructor(
    private spaceService: SpaceService,
    private contentService: ContentService,
    private clipboardService: ClipboardService,
    private actionService: ActionService
  ) {}

  public async delete() {
    if (this.node.id) {
      this.deleting = true;
      if (
        this.node.type &&
        (this.node.type.indexOf('folder') !== -1 ||
          this.node.type.indexOf('folderlink') !== -1)
      ) {
        const result: ActionEmitterResult = {};
        result.type = ActionType.DELETE_SPACE;

        try {
          await firstValueFrom(this.spaceService.deleteSpace(this.node.id));
          result.result = ActionResult.SUCCEED;
          this.clipboardService.removeItem(this.node);
          this.showModal = false;
          this.actionService.propagateActionFinished(result);
        } catch (error) {
          result.result = ActionResult.FAILED;
        }
        this.modalHide.emit(result);
      } else if (this.node.type && this.node.type.indexOf('folder') === -1) {
        const result: ActionEmitterResult = {};
        result.type = ActionType.DELETE_CONTENT;

        try {
          await firstValueFrom(this.contentService.deleteContent(this.node.id));
          result.result = ActionResult.SUCCEED;
          this.clipboardService.removeItem(this.node);
          this.showModal = false;
        } catch (error) {
          result.result = ActionResult.FAILED;
        }
        this.modalHide.emit(result);
      }
      this.deleting = false;
    }
  }

  public cancelWizard(_action: string): void {
    this.showModal = false;

    const result: ActionEmitterResult = {};
    result.result = ActionResult.CANCELED;
    result.type = ActionType.DELETE_SPACE;

    this.modalHide.emit(result);
  }
}
