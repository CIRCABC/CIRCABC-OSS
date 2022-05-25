import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges,
} from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  Validators,
} from '@angular/forms';

import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { Node as ModelNode, TopicService } from 'app/core/generated/circabc';
import { ValidationService } from 'app/core/validation.service';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-edit-topic',
  templateUrl: './edit-topic.component.html',
  preserveWhitespaces: true,
})
export class EditTopicComponent implements OnInit, OnChanges {
  @Input()
  showModal = false;
  @Input()
  topic: ModelNode | undefined;
  @Output()
  readonly modalHide = new EventEmitter<ActionEmitterResult>();

  public updating = false;
  public editTopicForm!: FormGroup;

  constructor(private fb: FormBuilder, private topicService: TopicService) {}

  ngOnInit(): void {
    this.editTopicForm = this.fb.group(
      {
        name: [
          '',
          [
            Validators.required,
            ValidationService.nameValidator,
            Validators.maxLength(50),
          ],
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
      if (this.topic && this.topic.id) {
        this.topic.name = this.editTopicForm.value.name;
        await firstValueFrom(
          this.topicService.putTopic(this.topic.id, this.topic)
        );
        res.result = ActionResult.SUCCEED;
      }
    } catch (error) {
      res.result = ActionResult.FAILED;
    }

    this.showModal = false;
    this.updating = false;
    this.modalHide.emit(res);
  }
}
