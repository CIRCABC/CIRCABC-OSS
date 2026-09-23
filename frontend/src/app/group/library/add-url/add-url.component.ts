import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
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
import { Node as ModelNode, SpaceService } from 'app/core/generated/circabc';
import { fileNameValidator } from 'app/core/validation.service';
import { ControlMessageComponent } from 'app/shared/control-message/control-message.component';
import { ModalComponent } from 'app/shared/modal/modal.component';

/**
 * Modal component that lets a user add a URL link node inside a library space.
 *
 * The component renders a modal dialog (via {@link ModalComponent}) containing a
 * reactive form with two fields — a display name and a URL — and submits the new
 * URL node to the backend through {@link SpaceService}. On completion (success,
 * failure or cancellation) it notifies the host component through the
 * {@link AddUrlComponent.modalHide} output so the host can close the modal and
 * react to the outcome.
 *
 * Key collaborators:
 * - {@link FormBuilder} — builds the reactive form used for input and validation.
 * - {@link SpaceService} — generated CIRCABC API client used to create the URL node.
 */
@Component({
  selector: 'cbc-add-url',
  templateUrl: './add-url.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ModalComponent,
    ReactiveFormsModule,
    ControlMessageComponent,
    TranslocoModule,
  ],
})
export class AddUrlComponent implements OnInit {
  /** Angular reactive-forms builder used to construct {@link createUrlForm}. */
  private readonly fb = inject(FormBuilder);
  /** Generated CIRCABC API client used to persist the new URL node. */
  private readonly spaceService = inject(SpaceService);

  /** Whether the modal dialog is currently displayed. */
  readonly showModal = input(false);
  /**
   * Required input: the parent space node under which the new URL node will be
   * created. Its `id` identifies the target container for the POST request.
   */
  public readonly parentNode = input.required<ModelNode>();
  /**
   * Emitted when the modal should be closed, carrying the outcome of the
   * operation (success, failure or cancellation) so the host can react.
   */
  public readonly modalHide = output<ActionEmitterResult>();

  /** Reactive form holding the `name` and `url` controls for the new node. */
  public createUrlForm!: FormGroup;
  /** True while a create request is in flight; used to disable/indicate progress. */
  public readonly processing = signal(false);

  /**
   * Angular lifecycle hook. Initializes {@link createUrlForm} with the `name`
   * (required, validated via {@link fileNameValidator}) and `url` (required)
   * controls.
   */
  ngOnInit() {
    this.createUrlForm = this.fb.group(
      {
        name: ['', [Validators.required, fileNameValidator]],
        url: ['', Validators.required],
      },
      {
        updateOn: 'change',
      }
    );
  }

  /**
   * Creates a new URL node under the parent node using the current form values.
   *
   * Sets {@link processing} while the request runs, builds the node body from the
   * form's `name` and `url` values, and POSTs it via {@link SpaceService.postURL}.
   * On success the form is reset; on failure the error is swallowed and reported
   * through the result. In all cases (when the parent has an id) a result is
   * emitted through {@link modalHide}.
   *
   * @returns A promise that resolves once the operation and emission complete.
   */
  async createUrl() {
    this.processing.set(true);
    const parentNode = this.parentNode();
    if (parentNode.id !== undefined) {
      const res: ActionEmitterResult = {};
      res.type = ActionType.ADD_URL;

      const body: ModelNode = {
        name: this.createUrlForm.value.name,
        properties: {
          url: this.createUrlForm.value.url,
        },
      };

      try {
        await this.spaceService.postURLAsync({ id: parentNode.id, node: body });
        res.result = ActionResult.SUCCEED;
        this.createUrlForm.reset();
      } catch (error) {
        console.error(error);
        res.result = ActionResult.FAILED;
      }

      this.modalHide.emit(res);
    }
    this.processing.set(false);
  }

  /**
   * Cancels the operation: resets the form and emits a
   * {@link ActionResult.CANCELED} result of type {@link ActionType.ADD_URL}
   * through {@link modalHide} so the host can close the modal.
   */
  cancel() {
    this.createUrlForm.reset({ name: '', url: '' });
    const res: ActionEmitterResult = {};
    res.result = ActionResult.CANCELED;
    res.type = ActionType.ADD_URL;
    this.modalHide.emit(res);
  }

  /**
   * Convenience accessor for the form's `name` control.
   * @returns The `name` {@link AbstractControl} of {@link createUrlForm}.
   */
  get nameControl(): AbstractControl {
    return this.createUrlForm.controls.name;
  }

  /**
   * Convenience accessor for the form's `url` control.
   * @returns The `url` {@link AbstractControl} of {@link createUrlForm}.
   */
  get urlControl(): AbstractControl {
    return this.createUrlForm.controls.url;
  }
}
