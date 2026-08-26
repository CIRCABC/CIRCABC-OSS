import { Component, output, input } from '@angular/core';
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
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-delete-action',
  templateUrl: './delete-action.component.html',
  preserveWhitespaces: true,
  imports: [
    ModalComponent,
    ReactiveFormsModule,
    MatSlideToggleModule,
    TranslocoModule,
  ],
})
export class DeleteActionComponent {
  readonly node = input.required<ModelNode>();
  public readonly modalHide = output<ActionEmitterResult>();

  public showModal = false;
  public deleting = false;
  private notify = true;

  public get confirmationMessage(): string {
    const node = this.node();
    if (node.type === undefined) {
      return '';
    }

    if (node.type.indexOf('folderlink') !== -1) {
      return 'text.delete-link.confirmation';
    }
    if (node.type.indexOf('folder') !== -1) {
      return 'text.delete-space.confirmation';
    }
    return 'text.delete-content.confirmation';
  }
  notifyFormGroup = this.notifyFormBuilder.group({
    notify: true,
  });
  constructor(
    private notifyFormBuilder: FormBuilder,
    private spaceService: SpaceService,
    private contentService: ContentService,
    private clipboardService: ClipboardService,
    private actionService: ActionService
  ) {}

  public async delete() {
    if (!this.notifyFormGroup.controls.notify.value) {
      this.notify = false;
    }
    const node = this.node();
    if (node.id) {
      this.deleting = true;
      if (
        node.type &&
        (node.type.indexOf('folder') !== -1 ||
          node.type.indexOf('folderlink') !== -1)
      ) {
        const result: ActionEmitterResult = {};
        result.type = ActionType.DELETE_SPACE;

        try {
          await firstValueFrom(
            this.spaceService.deleteSpace(node.id, this.notify)
          );
          result.result = ActionResult.SUCCEED;
          this.clipboardService.removeItem(node);
          this.showModal = false;
          this.actionService.propagateActionFinished(result);
        } catch (_error) {
          result.result = ActionResult.FAILED;
        }
        this.modalHide.emit(result);
      } else if (node.type && node.type.indexOf('folder') === -1) {
        const result: ActionEmitterResult = {};
        result.type = ActionType.DELETE_CONTENT;

        try {
          await firstValueFrom(
            this.contentService.deleteContent(node.id, this.notify)
          );
          result.result = ActionResult.SUCCEED;
          this.clipboardService.removeItem(node);
          this.showModal = false;
        } catch (_error) {
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
