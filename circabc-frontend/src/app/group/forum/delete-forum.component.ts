import { Component, Input, output, input } from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { ActionService } from 'app/action-result/action.service';
import { ForumService, Node as ModelNode } from 'app/core/generated/circabc';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-delete-forum',
  templateUrl: './delete-forum.component.html',
  preserveWhitespaces: true,
  imports: [SpinnerComponent, I18nPipe, TranslocoModule],
})
export class DeleteForumComponent {
  readonly forum = input.required<ModelNode>();
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  showModal = false;
  readonly modalHide = output<ActionEmitterResult>();

  public deleting = false;

  constructor(
    private forumService: ForumService,
    private actionService: ActionService
  ) {}

  public async delete() {
    this.deleting = true;

    const result: ActionEmitterResult = {};
    result.type = ActionType.DELETE_FORUM;

    const forum = this.forum();
    if (forum.id) {
      try {
        await firstValueFrom(this.forumService.deleteForum(forum.id));
        result.result = ActionResult.SUCCEED;
      } catch (_error) {
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
