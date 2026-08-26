import { Component, Input, OnInit, output, input } from '@angular/core';
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
import {
  ContentService,
  ForumService,
  Node as ModelNode,
} from 'app/core/generated/circabc';
import { removeNulls } from 'app/core/util';
import {
  maxLengthTitleValidator,
  titleValidator,
} from 'app/core/validation.service';
import { ControlMessageComponent } from 'app/shared/control-message/control-message.component';
import { MultilingualInputComponent } from 'app/shared/input/multilingual-input.component';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-create-topic',
  templateUrl: './create-topic.component.html',
  preserveWhitespaces: true,
  imports: [
    ModalComponent,
    ReactiveFormsModule,
    MultilingualInputComponent,
    ControlMessageComponent,
    TranslocoModule,
  ],
})
export class CreateTopicComponent implements OnInit {
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  public showWizard!: boolean;
  readonly forum = input.required<ModelNode>();
  readonly modalHide = output<ActionEmitterResult>();

  public newTopicForm!: FormGroup;
  public creating = false;

  constructor(
    private forumService: ForumService,
    private fb: FormBuilder,
    private contentService: ContentService,
    private i18nPipe: I18nPipe
  ) {}

  ngOnInit() {
    this.buildForm();
  }

  private buildForm() {
    this.newTopicForm = this.fb.group(
      {
        name: [''],
        description: [''],
        title: [
          '',
          [
            Validators.required,
            (control: AbstractControl) => titleValidator(control),
            (control: AbstractControl) => maxLengthTitleValidator(control, 50),
          ],
        ],
      },
      {
        updateOn: 'change',
      }
    );
  }

  public cancelWizard(_action: string): void {
    this.showWizard = false;

    const result: ActionEmitterResult = {};
    result.result = ActionResult.CANCELED;
    result.type = ActionType.CREATE_TOPIC;

    this.reset();

    this.modalHide.emit(result);
  }

  public async createTopic() {
    const forum = this.forum();
    if (forum?.id && forum.type) {
      this.creating = true;

      const result: ActionEmitterResult = {};
      result.type = ActionType.CREATE_TOPIC;

      const body: ModelNode = {
        ...this.newTopicForm.value,
      };

      body.name = removeNulls(
        this.i18nPipe.transform(this.newTopicForm.value.title)
      );

      try {
        if (forum.type.indexOf('forum') !== -1) {
          await firstValueFrom(
            this.forumService.postForumContent(forum.id, body)
          );
        } else if (forum.type.indexOf('content') !== -1) {
          await firstValueFrom(this.contentService.postTopic(forum.id, body));
        }

        result.result = ActionResult.SUCCEED;
        this.modalHide.emit(result);
        this.reset();
        this.showWizard = false;
      } catch (_error) {
        result.result = ActionResult.FAILED;
      }

      this.creating = false;
    }
  }

  private reset() {
    if (this.newTopicForm !== undefined) {
      this.newTopicForm.reset({
        name: '',
        title: '',
        description: '',
      });
      this.newTopicForm.controls.title.markAsPristine();
    }
  }

  get titleControl(): AbstractControl {
    return this.newTopicForm.controls.title;
  }
}
