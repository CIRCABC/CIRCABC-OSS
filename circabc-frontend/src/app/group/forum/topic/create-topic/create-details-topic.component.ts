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
import { ContentService, Node as ModelNode } from 'app/core/generated/circabc';
import { ValidationService } from 'app/core/validation.service';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-create-details-topic',
  templateUrl: './create-details-topic.component.html',
  preserveWhitespaces: true,
})
export class CreateDetailsTopicComponent implements OnInit, OnChanges {
  @Input()
  showModal = false;
  @Input()
  forum!: ModelNode;
  @Output()
  readonly modalHide = new EventEmitter<ActionEmitterResult>();

  public creating = false;
  public createTopicForm!: FormGroup;

  constructor(
    private fb: FormBuilder,
    private contentService: ContentService
  ) {}

  ngOnInit(): void {
    this.createTopicForm = this.fb.group(
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
    if (changes.topic && this.createTopicForm) {
      this.createTopicForm.controls.name.patchValue(
        changes.topic.currentValue.name
      );
    }
  }

  get nameControl(): AbstractControl {
    return this.createTopicForm.controls.name;
  }

  cancel() {
    const res: ActionEmitterResult = {};
    res.result = ActionResult.CANCELED;
    res.type = ActionType.CREATE_TOPIC;
    this.showModal = false;
    this.reset();
    this.modalHide.emit(res);
  }

  async save() {
    this.creating = true;
    const res: ActionEmitterResult = {};
    res.type = ActionType.CREATE_TOPIC;

    const body: ModelNode = {
      ...this.createTopicForm.value,
    };

    try {
      await firstValueFrom(
        this.contentService.postTopic(this.forum.id as string, body)
      );
      res.result = ActionResult.SUCCEED;
    } catch (error) {
      res.result = ActionResult.FAILED;
    }

    this.showModal = false;
    this.creating = false;
    this.reset();
    this.modalHide.emit(res);
  }

  private reset() {
    if (this.createTopicForm !== undefined) {
      this.createTopicForm.reset({
        name: '',
      });
      this.createTopicForm.controls.name.markAsPristine();
    }
  }
}
