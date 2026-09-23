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

/**
 * Modal wizard component that lets a user create a new forum topic.
 *
 * Renders a modal dialog (via {@link ModalComponent}) containing a reactive
 * form with a multilingual title input plus name and description fields. On
 * submission it posts the new topic to the backend, targeting either a forum
 * node ({@link ForumService.postForumContent}) or a content node
 * ({@link ContentService.postTopic}) depending on the type of the parent
 * {@link forum} node.
 *
 * The component's visibility is controlled through the two-way bindable
 * {@link showWizard} model, and completion or cancellation is reported to the
 * parent through the {@link modalHide} output.
 */
@Component({
  selector: 'cbc-create-topic',
  templateUrl: './create-topic.component.html',
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
export class CreateTopicComponent implements OnInit {
  /** Backend service used to create a topic under a forum-type node. */
  private readonly forumService = inject(ForumService);
  /** Angular reactive-forms builder used to construct {@link newTopicForm}. */
  private readonly fb = inject(FormBuilder);
  /** Backend service used to create a topic under a content-type node. */
  private readonly contentService = inject(ContentService);
  /** Pipe used to resolve the multilingual title into a plain node name. */
  private readonly i18nPipe = inject(I18nPipe);

  /**
   * Two-way bindable model controlling the visibility of the modal wizard.
   * `true` shows the dialog, `false` hides it.
   */
  public showWizard = model<boolean>(false);
  /**
   * Required input: the parent node under which the topic is created. Its
   * `type` determines whether the forum or content service is used.
   */
  readonly forum = input.required<ModelNode>();
  /**
   * Output emitted when the modal closes, carrying the outcome of the
   * operation (succeeded, failed or cancelled) as an {@link ActionEmitterResult}.
   */
  readonly modalHide = output<ActionEmitterResult>();

  /** Reactive form holding the topic's name, description and multilingual title. */
  public newTopicForm!: FormGroup;
  /** Whether a topic creation request is currently in progress. */
  public creating = signal(false);

  /**
   * Angular lifecycle hook. Builds the reactive form when the component
   * initializes.
   */
  ngOnInit() {
    this.buildForm();
  }

  /**
   * Constructs {@link newTopicForm} with `name`, `description` and `title`
   * controls. The `title` control is required and validated for content and
   * a maximum length of 50 characters.
   */
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

  /**
   * Cancels the wizard without creating a topic. Hides the dialog, resets the
   * form and emits a {@link modalHide} event with a cancelled result.
   *
   * @param _action - The originating action identifier (unused).
   */
  public cancelWizard(_action: string): void {
    this.showWizard.set(false);

    const result: ActionEmitterResult = {};
    result.result = ActionResult.CANCELED;
    result.type = ActionType.CREATE_TOPIC;

    this.reset();

    this.modalHide.emit(result);
  }

  /**
   * Submits the form to create a new topic under the current {@link forum}
   * node. Depending on the parent node's `type`, the request is routed to the
   * forum service (forum nodes) or the content service (content nodes). On
   * success the dialog is closed and reset and a succeeded {@link modalHide}
   * event is emitted; on failure a failed result is prepared. Does nothing if
   * the parent node lacks an `id` or `type`.
   *
   * @returns A promise that resolves once the creation attempt completes.
   */
  public async createTopic() {
    const forum = this.forum();
    if (forum?.id && forum.type) {
      this.creating.set(true);

      const result: ActionEmitterResult = {};
      result.type = ActionType.CREATE_TOPIC;

      const body: ModelNode = {
        ...this.newTopicForm.value,
      };

      body.name = removeNulls(
        this.i18nPipe.transform(this.newTopicForm.value.title)
      );

      try {
        if (forum.type.includes('forum')) {
          await this.forumService.postForumContentAsync({
            id: forum.id,
            node: body,
          });
        } else if (forum.type.includes('content')) {
          await this.contentService.postTopicAsync({
            id: forum.id,
            node: body,
          });
        }

        result.result = ActionResult.SUCCEED;
        this.modalHide.emit(result);
        this.reset();
        this.showWizard.set(false);
      } catch (error) {
        console.error(error);
        result.result = ActionResult.FAILED;
      }

      this.creating.set(false);
    }
  }

  /**
   * Resets {@link newTopicForm} to empty values and marks the title control as
   * pristine. No-op if the form has not been built yet.
   */
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

  /**
   * Convenience accessor for the form's `title` control, used by the template
   * to display validation messages.
   *
   * @returns The `title` {@link AbstractControl} of {@link newTopicForm}.
   */
  get titleControl(): AbstractControl {
    return this.newTopicForm.controls.title;
  }
}
