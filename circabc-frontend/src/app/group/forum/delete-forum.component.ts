import { Component, EventEmitter, Input, Output } from '@angular/core';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { ActionService } from 'app/action-result/action.service';
import { ForumService, Node as ModelNode } from 'app/core/generated/circabc';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-delete-forum',
  templateUrl: './delete-forum.component.html',
  preserveWhitespaces: true,
})
export class DeleteForumComponent {
  @Input()
  forum!: ModelNode;
  @Input()
  showModal = false;
  @Output()
  readonly modalHide = new EventEmitter<ActionEmitterResult>();

  public deleting = false;

  constructor(
    private forumService: ForumService,
    private actionService: ActionService
  ) {}

  public async delete() {
    this.deleting = true;

    const result: ActionEmitterResult = {};
    result.type = ActionType.DELETE_FORUM;

    if (this.forum.id) {
      try {
        await firstValueFrom(this.forumService.deleteForum(this.forum.id));
        result.result = ActionResult.SUCCEED;
      } catch (error) {
        result.result = ActionResult.FAILED;
      }
    }
    this.deleting = false;
    this.showModal = false;
    this.modalHide.emit(result);
    this.actionService.propagateActionFinished(result);
  }

  public cancelWizard(_action: string): void {
    this.showModal = false;
    const result: ActionEmitterResult = {};
    result.result = ActionResult.CANCELED;
    result.type = ActionType.DELETE_FORUM;
    this.modalHide.emit(result);
  }
}
