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
import { ContentService, Node as ModelNode } from 'app/core/generated/circabc';
import { nameValidator } from 'app/core/validation.service';
import { ControlMessageComponent } from 'app/shared/control-message/control-message.component';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-create-details-topic',
  templateUrl: './create-details-topic.component.html',
  preserveWhitespaces: true,
  imports: [
    ModalComponent,
    ReactiveFormsModule,
    ControlMessageComponent,
    TranslocoModule,
  ],
})
export class CreateDetailsTopicComponent implements OnInit, OnChanges {
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  showModal = false;
  readonly forum = input.required<ModelNode>();
  readonly modalHide = output<ActionEmitterResult>();

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
          [Validators.required, nameValidator, Validators.maxLength(50)],
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
        this.contentService.postTopic(this.forum().id as string, body)
      );
      res.result = ActionResult.SUCCEED;
    } catch (_error) {
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
