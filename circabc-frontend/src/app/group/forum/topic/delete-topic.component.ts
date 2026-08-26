import { Component, Input, output, input } from '@angular/core';

import { TranslocoModule } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { Node as ModelNode, TopicService } from 'app/core/generated/circabc';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-delete-topic',
  templateUrl: './delete-topic.component.html',
  preserveWhitespaces: true,
  imports: [SpinnerComponent, I18nPipe, TranslocoModule],
})
export class DeleteTopicComponent {
  readonly topic = input.required<ModelNode>();
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  showModal = false;
  readonly modalHide = output<ActionEmitterResult>();

  public deleting = false;

  constructor(private topicService: TopicService) {}

  public async delete() {
    this.deleting = true;

    const result: ActionEmitterResult = {};
    result.type = ActionType.DELETE_TOPIC;

    const topic = this.topic();
    if (topic.id) {
      try {
        await firstValueFrom(this.topicService.deleteTopic(topic.id));
        result.result = ActionResult.SUCCEED;
      } catch (_error) {
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
