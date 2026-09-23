import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  OnChanges,
  OnInit,
  output,
  SimpleChanges,
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
import { ContentService, Node as ModelNode } from 'app/core/generated/circabc';
import { nameValidator } from 'app/core/validation.service';
import { ControlMessageComponent } from 'app/shared/control-message/control-message.component';
import { ModalComponent } from 'app/shared/modal/modal.component';

/**
 * Modal component that renders the "create topic" form for a forum.
 *
 * It displays a {@link ModalComponent} containing a reactive form with a single
 * required `name` field (validated for non-emptiness, allowed characters via
 * {@link nameValidator} and a maximum length of 50 characters). On submit it
 * creates a new topic under the target forum through the generated
 * {@link ContentService} and notifies the parent of the outcome via the
 * {@link CreateDetailsTopicComponent.modalHide} output.
 *
 * Key collaborators:
 * - {@link FormBuilder} to build the reactive form.
 * - {@link ContentService} (generated CIRCABC API client) to persist the topic.
 */
@Component({
  selector: 'cbc-create-details-topic',
  templateUrl: './create-details-topic.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ModalComponent,
    ReactiveFormsModule,
    ControlMessageComponent,
    TranslocoModule,
  ],
})
export class CreateDetailsTopicComponent implements OnInit, OnChanges {
  /** Reactive form factory used to build the topic creation form. */
  private readonly fb = inject(FormBuilder);
  /** Generated CIRCABC content API client used to create the topic. */
  private readonly contentService = inject(ContentService);

  /**
   * Input controlling the visibility of the modal dialog.
   * When `true` the create-topic modal is shown.
   */
  public showModal = input<boolean>(false);

  /**
   * Required input describing the forum node under which the new topic
   * will be created. Its `id` is used as the parent identifier on save.
   */
  readonly forum = input.required<ModelNode>();
  /**
   * Output emitted when the modal is dismissed, either by cancelling or after
   * a save attempt. The emitted {@link ActionEmitterResult} carries the
   * {@link ActionType.CREATE_TOPIC} type and the outcome
   * ({@link ActionResult.CANCELED}, {@link ActionResult.SUCCEED} or
   * {@link ActionResult.FAILED}).
   */
  readonly modalHide = output<ActionEmitterResult>();

  /** Flag indicating a topic creation request is in progress. */
  public creating = signal(false);
  /** Reactive form group backing the topic creation fields. */
  public createTopicForm!: FormGroup;

  /**
   * Angular lifecycle hook. Initializes {@link createTopicForm} with a
   * required, validated `name` control that updates on every change.
   */
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

  /**
   * Angular lifecycle hook. When the `topic` input changes and the form is
   * already initialized, patches the `name` control with the incoming topic's
   * name so the form reflects the current value.
   *
   * @param changes The set of changed input properties for this component.
   */
  ngOnChanges(changes: SimpleChanges): void {
    if (changes.topic && this.createTopicForm) {
      this.createTopicForm.controls.name.patchValue(
        changes.topic.currentValue.name
      );
    }
  }

  /**
   * Convenience accessor for the `name` form control.
   *
   * @returns The {@link AbstractControl} backing the topic name field.
   */
  get nameControl(): AbstractControl {
    return this.createTopicForm.controls.name;
  }

  /**
   * Cancels topic creation, resets the form and emits a
   * {@link ActionEmitterResult} with {@link ActionResult.CANCELED}.
   */
  cancel() {
    const res: ActionEmitterResult = {};
    res.result = ActionResult.CANCELED;
    res.type = ActionType.CREATE_TOPIC;
    this.reset();
    this.modalHide.emit(res);
  }

  /**
   * Submits the form to create a new topic under the current {@link forum}.
   *
   * Sends the form values to {@link ContentService.postTopic}, tracks progress
   * via {@link creating}, resets the form and emits the result through
   * {@link modalHide}. On failure the emitted result is set to
   * {@link ActionResult.FAILED}; on success it is {@link ActionResult.SUCCEED}.
   *
   * @returns A promise that resolves once the creation attempt completes and
   * the result has been emitted.
   */
  async save() {
    this.creating.set(true);
    const res: ActionEmitterResult = {};
    res.type = ActionType.CREATE_TOPIC;

    const body: ModelNode = {
      ...this.createTopicForm.value,
    };

    try {
      await this.contentService.postTopicAsync({
        id: this.forum().id as string,
        node: body,
      });
      res.result = ActionResult.SUCCEED;
    } catch (error) {
      console.error(error);
      res.result = ActionResult.FAILED;
    }

    this.creating.set(false);
    this.reset();
    this.modalHide.emit(res);
  }

  /**
   * Resets {@link createTopicForm} back to its pristine, empty state.
   * No-op when the form has not been initialized.
   */
  private reset() {
    if (this.createTopicForm !== undefined) {
      this.createTopicForm.reset({
        name: '',
      });
      this.createTopicForm.controls.name.markAsPristine();
    }
  }
}
