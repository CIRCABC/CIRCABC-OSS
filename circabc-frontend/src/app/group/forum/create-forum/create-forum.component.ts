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
import { ActionService } from 'app/action-result/action.service';
import { ForumService, Node as ModelNode } from 'app/core/generated/circabc';
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
  selector: 'cbc-create-forum',
  templateUrl: './create-forum.component.html',
  preserveWhitespaces: true,
  imports: [
    ModalComponent,
    ReactiveFormsModule,
    MultilingualInputComponent,
    ControlMessageComponent,
    TranslocoModule,
  ],
})
export class CreateForumComponent implements OnInit {
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  public showWizard!: boolean;
  public readonly parentNode = input.required<ModelNode>();
  public readonly modalHide = output<ActionEmitterResult>();

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
            (control: AbstractControl) => titleValidator(control),
            (control: AbstractControl) => maxLengthTitleValidator(control, 255),
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

      const parentNode = this.parentNode();
      if (parentNode.id !== undefined) {
        forumNode.name = removeNulls(
          this.i18nPipe.transform(this.createForumForm.value.title)
        );
        const response = await firstValueFrom(
          this.forumService.postSubforums(parentNode.id, forumNode)
        );

        this.showWizard = false;

        result.node = response;
        result.result = ActionResult.SUCCEED;
      }
    } catch (_error) {
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
