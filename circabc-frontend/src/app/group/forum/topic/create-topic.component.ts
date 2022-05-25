import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
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
import {
  ContentService,
  ForumService,
  Node as ModelNode,
} from 'app/core/generated/circabc';
import { removeNulls } from 'app/core/util';
import { ValidationService } from 'app/core/validation.service';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-create-topic',
  templateUrl: './create-topic.component.html',
  preserveWhitespaces: true,
})
export class CreateTopicComponent implements OnInit {
  @Input()
  public showWizard!: boolean;
  @Input()
  forum!: ModelNode;
  @Output()
  readonly modalHide = new EventEmitter<ActionEmitterResult>();

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
            (control: AbstractControl) =>
              ValidationService.titleValidator(control),
            (control: AbstractControl) =>
              ValidationService.maxLengthTitleValidator(control, 50),
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
    if (this.forum && this.forum.id && this.forum.type) {
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
        if (this.forum.type.indexOf('forum') !== -1) {
          await firstValueFrom(
            this.forumService.postForumContent(this.forum.id, body)
          );
        } else if (this.forum.type.indexOf('content') !== -1) {
          await firstValueFrom(
            this.contentService.postTopic(this.forum.id, body)
          );
        }

        result.result = ActionResult.SUCCEED;
        this.modalHide.emit(result);
        this.reset();
        this.showWizard = false;
      } catch (error) {
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
