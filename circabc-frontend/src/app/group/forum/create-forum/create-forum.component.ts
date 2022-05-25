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
import { ActionService } from 'app/action-result/action.service';
import { ForumService, Node as ModelNode } from 'app/core/generated/circabc';
import { removeNulls } from 'app/core/util';
import { ValidationService } from 'app/core/validation.service';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-create-forum',
  templateUrl: './create-forum.component.html',
  preserveWhitespaces: true,
})
export class CreateForumComponent implements OnInit {
  @Input()
  public showWizard!: boolean;
  @Input()
  public parentNode!: ModelNode;
  @Output()
  public readonly modalHide = new EventEmitter<ActionEmitterResult>();

  public createForumForm!: FormGroup;
  public creating = false;

  public constructor(
    private fb: FormBuilder,
    private forumService: ForumService,
    private i18nPipe: I18nPipe,
    private actionService: ActionService
  ) {}

  ngOnInit() {
    this.buildForm();
  }

  private buildForm(): void {
    this.createForumForm = this.fb.group(
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
    result.type = ActionType.CREATE_FORUM;

    this.reset();

    this.modalHide.emit(result);
    this.actionService.propagateActionFinished(result);
  }

  public async createForum() {
    this.creating = true;

    const result: ActionEmitterResult = {};
    result.type = ActionType.CREATE_FORUM;

    try {
      const forumNode: ModelNode = {
        ...this.createForumForm.value,
      };

      if (this.parentNode.id !== undefined) {
        forumNode.name = removeNulls(
          this.i18nPipe.transform(this.createForumForm.value.title)
        );
        const response = await firstValueFrom(
          this.forumService.postSubforums(this.parentNode.id, forumNode)
        );

        this.showWizard = false;

        result.node = response;
        result.result = ActionResult.SUCCEED;
      }
    } catch (error) {
      result.result = ActionResult.FAILED;
    }

    this.modalHide.emit(result);
    this.actionService.propagateActionFinished(result);
    this.reset();
    this.creating = false;
  }

  private reset() {
    if (this.createForumForm !== undefined) {
      this.createForumForm.reset({
        name: '',
        title: '',
        description: '',
      });
      this.createForumForm.controls.title.markAsPristine();
    }
  }

  get titleControl(): AbstractControl {
    return this.createForumForm.controls.title;
  }
}
