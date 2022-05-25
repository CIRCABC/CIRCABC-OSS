import { Component, EventEmitter, Input, Output } from '@angular/core';

import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { Node as ModelNode, TopicService } from 'app/core/generated/circabc';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-delete-topic',
  templateUrl: './delete-topic.component.html',
  preserveWhitespaces: true,
})
export class DeleteTopicComponent {
  @Input()
  topic!: ModelNode;
  @Input()
  showModal = false;
  @Output()
  readonly modalHide = new EventEmitter<ActionEmitterResult>();

  public deleting = false;

  constructor(private topicService: TopicService) {}

  public async delete() {
    this.deleting = true;

    const result: ActionEmitterResult = {};
    result.type = ActionType.DELETE_TOPIC;

    if (this.topic.id) {
      try {
        await firstValueFrom(this.topicService.deleteTopic(this.topic.id));
        result.result = ActionResult.SUCCEED;
      } catch (error) {
        result.result = ActionResult.FAILED;
      }
    }
    this.deleting = false;
    this.showModal = false;
    this.modalHide.emit(result);
  }

  public cancelWizard(_action: string): void {
    this.showModal = false;
    const result: ActionEmitterResult = {};
    result.result = ActionResult.CANCELED;
    result.type = ActionType.DELETE_TOPIC;
    this.modalHide.emit(result);
  }
}
