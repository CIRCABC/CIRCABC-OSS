import {
  Component,
  Input,
  OnChanges,
  OnInit,
  SimpleChanges,
  output,
  input,
} from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';

import { TranslocoModule } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { Node as ModelNode, TopicService } from 'app/core/generated/circabc';
import { nameValidator } from 'app/core/validation.service';
import { ControlMessageComponent } from 'app/shared/control-message/control-message.component';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-edit-topic',
  templateUrl: './edit-topic.component.html',
  preserveWhitespaces: true,
  imports: [
    ModalComponent,
    ReactiveFormsModule,
    ControlMessageComponent,
    TranslocoModule,
  ],
})
export class EditTopicComponent implements OnInit, OnChanges {
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  showModal = false;
  readonly topic = input<ModelNode>();
  readonly modalHide = output<ActionEmitterResult>();

  public updating = false;
  public editTopicForm!: FormGroup;

  constructor(
    private fb: FormBuilder,
    private topicService: TopicService
  ) {}

  ngOnInit(): void {
    this.editTopicForm = this.fb.group(
      {
        name: [
          '',
          [Validators.required, nameValidator, Validators.maxLength(50)],
        ],
      },
      {
        updateOn: 'change',
      }
    );
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.topic && this.editTopicForm) {
      this.editTopicForm.controls.name.patchValue(
        changes.topic.currentValue.name
      );
    }
  }

  get nameControl(): AbstractControl {
    return this.editTopicForm.controls.name;
  }

  cancel() {
    const res: ActionEmitterResult = {};
    res.result = ActionResult.CANCELED;
    res.type = ActionType.EDIT_TOPIC;
    this.modalHide.emit(res);
    this.showModal = false;
  }

  async save() {
    this.updating = true;
    const res: ActionEmitterResult = {};
    res.type = ActionType.EDIT_TOPIC;

    try {
      const topic = this.topic();
      if (topic?.id) {
        topic.name = this.editTopicForm.value.name;
        await firstValueFrom(this.topicService.putTopic(topic.id, topic));
        res.result = ActionResult.SUCCEED;
      }
    } catch (_error) {
      res.result = ActionResult.FAILED;
    }

    this.showModal = false;
    this.updating = false;
    this.modalHide.emit(res);
  }
}
