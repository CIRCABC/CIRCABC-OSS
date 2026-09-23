import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  model,
  OnInit,
  output,
  signal,
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

/**
 * Modal wizard component that lets a user create a new (sub)forum under a
 * given parent node.
 *
 * Rendered through the `cbc-create-forum` selector, it displays a modal
 * dialog (via {@link ModalComponent}) containing a reactive form with a
 * multilingual title input plus name and description fields. On submission it
 * posts the new forum to the backend through {@link ForumService} and reports
 * the outcome to callers via the {@link modalHide} output and the
 * {@link ActionService} action bus.
 *
 * Key collaborators:
 * - {@link FormBuilder} to build the reactive form.
 * - {@link ForumService} to persist the new subforum.
 * - {@link I18nPipe} to resolve the multilingual title into a node name.
 * - {@link ActionService} to broadcast that the create action finished.
 */
@Component({
  selector: 'cbc-create-forum',
  templateUrl: './create-forum.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ModalComponent,
    ReactiveFormsModule,
    MultilingualInputComponent,
    ControlMessageComponent,
    TranslocoModule,
  ],
})
export class CreateForumComponent implements OnInit {
  /** Reactive form builder used to construct {@link createForumForm}. */
  private readonly fb = inject(FormBuilder);
  /** Backend API client used to persist the new subforum. */
  private readonly forumService = inject(ForumService);
  /** Pipe used to resolve the multilingual title into a plain node name. */
  private readonly i18nPipe = inject(I18nPipe);
  /** Service used to broadcast that the create-forum action has finished. */
  private readonly actionService = inject(ActionService);

  /**
   * Two-way bindable model controlling the visibility of the modal wizard.
   * Set to `true` to show the dialog; the component sets it back to `false`
   * on cancel or successful creation.
   */
  public showWizard = model<boolean>(false);
  /**
   * Required input holding the parent node under which the new forum is
   * created. Its `id` is used as the target of the create request.
   */
  public readonly parentNode = input.required<ModelNode>();
  /**
   * Output emitted when the modal closes, carrying the outcome of the
   * create-forum action (success, cancellation or failure) together with the
   * created node when applicable.
   */
  public readonly modalHide = output<ActionEmitterResult>();

  /** Reactive form backing the create-forum dialog (name, title, description). */
  public createForumForm!: FormGroup;
  /** Flag indicating that a create request is currently in progress. */
  public readonly creating = signal(false);

  /**
   * Angular lifecycle hook. Builds the reactive form when the component is
   * initialized.
   */
  ngOnInit() {
    this.buildForm();
  }

  /**
   * Initializes {@link createForumForm} with `name`, `description` and a
   * required, validated `title` control. The title uses the shared title
   * validators and is limited to 255 characters.
   */
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

  /**
   * Cancels the wizard without creating a forum.
   *
   * Hides the dialog, resets the form and emits a
   * {@link ActionResult.CANCELED} result of type
   * {@link ActionType.CREATE_FORUM} through {@link modalHide} and the
   * {@link ActionService}.
   *
   * @param _action - The originating action identifier (unused).
   */
  public cancelWizard(_action: string): void {
    this.showWizard.set(false);

    const result: ActionEmitterResult = {};
    result.result = ActionResult.CANCELED;
    result.type = ActionType.CREATE_FORUM;

    this.reset();

    this.modalHide.emit(result);
    this.actionService.propagateActionFinished(result);
  }

  /**
   * Submits the form to create a new subforum under {@link parentNode}.
   *
   * Builds a node from the form value, derives its `name` from the
   * multilingual title and posts it via {@link ForumService.postSubforums}.
   * On success the dialog is hidden and the created node is attached to the
   * result. Regardless of outcome, a result of type
   * {@link ActionType.CREATE_FORUM} is emitted through {@link modalHide} and
   * the {@link ActionService}, the form is reset and {@link creating} is
   * cleared. Errors are caught internally and reported as
   * {@link ActionResult.FAILED}.
   *
   * @returns A promise that resolves once the create attempt has completed
   * and the result has been propagated.
   */
  public async createForum() {
    this.creating.set(true);

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
        const response = await this.forumService.postSubforumsAsync({
          id: parentNode.id,
          node: forumNode,
        });

        this.showWizard.set(false);

        result.node = response;
        result.result = ActionResult.SUCCEED;
      }
    } catch (error) {
      console.error(error);
      result.result = ActionResult.FAILED;
    }

    this.modalHide.emit(result);
    this.actionService.propagateActionFinished(result);
    this.reset();
    this.creating.set(false);
  }

  /**
   * Resets {@link createForumForm} back to empty values and marks the title
   * control as pristine. No-op if the form has not been built yet.
   */
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

  /**
   * Convenience accessor for the `title` form control, used by the template
   * to display validation messages.
   *
   * @returns The `title` {@link AbstractControl} of {@link createForumForm}.
   */
  get titleControl(): AbstractControl {
    return this.createForumForm.controls.title;
  }
}
