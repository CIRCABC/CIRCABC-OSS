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
import { Node as ModelNode, TopicService } from 'app/core/generated/circabc';
import { nameValidator } from 'app/core/validation.service';
import { ControlMessageComponent } from 'app/shared/control-message/control-message.component';
import { ModalComponent } from 'app/shared/modal/modal.component';

/**
 * Modal component for editing an existing forum topic.
 *
 * Renders a modal dialog (via {@link ModalComponent}) containing a reactive
 * form with a single, validated `name` field. When the user confirms, the
 * component persists the renamed topic through the {@link TopicService} and
 * notifies the parent of the outcome via the {@link EditTopicComponent.modalHide}
 * output. Cancelling closes the dialog without persisting any change.
 *
 * Key collaborators:
 * - {@link FormBuilder} to construct the reactive edit form.
 * - {@link TopicService} (generated CIRCABC API client) to update the topic.
 */
@Component({
  selector: 'cbc-edit-topic',
  templateUrl: './edit-topic.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ModalComponent,
    ReactiveFormsModule,
    ControlMessageComponent,
    TranslocoModule,
  ],
})
export class EditTopicComponent implements OnInit, OnChanges {
  /** Angular reactive forms builder used to create the edit form group. */
  private readonly fb = inject(FormBuilder);
  /** Generated CIRCABC API client used to persist topic updates. */
  private readonly topicService = inject(TopicService);

  /** Input controlling the visibility of the edit modal dialog. */
  readonly showModal = input(false);
  /** Input holding the topic node to be edited; its `name` seeds the form. */
  readonly topic = input<ModelNode>();
  /**
   * Output emitted when the modal is dismissed, carrying the result of the
   * edit operation (success, failure or cancellation) so the parent can react.
   */
  readonly modalHide = output<ActionEmitterResult>();

  /** Whether an update request is currently in progress (disables the UI). */
  public updating = signal(false);
  /** Reactive form group backing the topic edit dialog. */
  public editTopicForm!: FormGroup;

  /**
   * Angular lifecycle hook that initialises the reactive form with a required,
   * validated `name` control (non-empty, valid name, max length 50).
   */
  ngOnInit(): void {
    this.editTopicForm = this.fb.group(
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
   * Angular lifecycle hook that reacts to changes of the `topic` input by
   * patching the form's `name` control with the incoming topic name, provided
   * the form has already been initialised.
   *
   * @param changes The set of changed input properties for this component.
   */
  ngOnChanges(changes: SimpleChanges): void {
    if (changes.topic && this.editTopicForm) {
      this.editTopicForm.controls.name.patchValue(
        changes.topic.currentValue.name
      );
    }
  }

  /**
   * Convenience accessor for the form's `name` control.
   *
   * @returns The {@link AbstractControl} backing the topic name field.
   */
  get nameControl(): AbstractControl {
    return this.editTopicForm.controls.name;
  }

  /**
   * Cancels the edit operation and closes the modal without persisting any
   * change, emitting a {@link ActionResult.CANCELED} result of type
   * {@link ActionType.EDIT_TOPIC} through {@link modalHide}.
   */
  cancel() {
    const res: ActionEmitterResult = {};
    res.result = ActionResult.CANCELED;
    res.type = ActionType.EDIT_TOPIC;
    this.modalHide.emit(res);
  }

  /**
   * Persists the edited topic name via {@link TopicService.putTopic}.
   *
   * Sets {@link updating} while the request is in flight, applies the new name
   * to the current topic and, on completion, emits a
   * {@link ActionResult.SUCCEED} or {@link ActionResult.FAILED} result of type
   * {@link ActionType.EDIT_TOPIC} through {@link modalHide}. If no valid topic
   * with an id is present, no request is made and the result is left unset.
   * Any error raised by the service call is caught and reported as a failure.
   *
   * @returns A promise that resolves once the update attempt has completed and
   * the outcome has been emitted.
   */
  async save() {
    this.updating.set(true);
    const res: ActionEmitterResult = {};
    res.type = ActionType.EDIT_TOPIC;

    try {
      const topic = this.topic();
      if (topic?.id) {
        topic.name = this.editTopicForm.value.name;
        await this.topicService.putTopicAsync({ id: topic.id, node: topic });
        res.result = ActionResult.SUCCEED;
      }
    } catch (error) {
      console.error(error);
      res.result = ActionResult.FAILED;
    }

    this.updating.set(false);
    this.modalHide.emit(res);
  }
}
