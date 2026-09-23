import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  model,
  OnChanges,
  output,
  signal,
} from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { TranslocoModule } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { Node as ModelNode, PostService } from 'app/core/generated/circabc';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';

/**
 * Modal component that lets a moderator reject a pending forum post.
 *
 * It renders a confirmation dialog containing a reactive form with an
 * optional rejection reason. When confirmed, it calls the backend to mark
 * the post as unverified (rejected) and notifies the parent component of the
 * outcome so the surrounding view can refresh or close the modal.
 *
 * @remarks
 * Collaborates with {@link PostService} to perform the rejection and with
 * {@link FormBuilder} to build the reactive form.
 */
@Component({
  selector: 'cbc-reject-post',
  templateUrl: './reject-post.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, SpinnerComponent, TranslocoModule],
})
export class RejectPostComponent implements OnChanges {
  /** API client used to submit the rejection verdict for the post. */
  private readonly postService = inject(PostService);
  /** Factory used to build the reactive rejection form. */
  private readonly formBuilder = inject(FormBuilder);

  /**
   * Required input holding the forum post node to be rejected.
   * Its `id` is used when calling the backend.
   */
  readonly post = input.required<ModelNode>();

  /**
   * Two-way bound model controlling the visibility of the rejection modal.
   * Set to `false` once the action is accepted or cancelled.
   */
  showModal = model<boolean>(false);
  /**
   * Emits once the modal is dismissed, carrying the outcome of the action
   * (success or cancellation) so the parent can react accordingly.
   */
  readonly modalHide = output<ActionEmitterResult>();

  /** Whether the rejection request is currently in progress (drives the spinner). */
  public readonly executing = signal(false);

  /** Reactive form backing the modal, containing the optional `rejectReason` field. */
  public rejectPostForm!: FormGroup;

  /**
   * Angular lifecycle hook. Rebuilds the reactive rejection form whenever a
   * bound input changes, initializing an empty `rejectReason` control.
   */
  ngOnChanges() {
    this.rejectPostForm = this.formBuilder.group(
      {
        rejectReason: [''],
      },
      {
        updateOn: 'change',
      }
    );
  }

  /**
   * Confirms the rejection of the post.
   *
   * Submits the current post id and rejection reason to the backend, hides
   * the modal, and emits a {@link ActionEmitterResult} with a
   * {@link ActionResult.SUCCEED} result and {@link ActionType.REJECT_POST}
   * type. Toggles {@link executing} around the asynchronous call.
   *
   * @returns A promise that resolves once the rejection request completes and
   * the outcome has been emitted.
   */
  public async accept() {
    this.executing.set(true);

    await this.postService.putVerifyAsync({
      id: this.post().id as string,
      approve: false,
      rejectReason: this.rejectPostForm.controls.rejectReason.value,
    });

    this.showModal.set(false);

    const result: ActionEmitterResult = {};
    result.result = ActionResult.SUCCEED;
    result.type = ActionType.REJECT_POST;
    this.modalHide.emit(result);

    this.executing.set(false);
  }

  /**
   * Cancels the rejection without contacting the backend.
   *
   * Hides the modal and emits a {@link ActionEmitterResult} with a
   * {@link ActionResult.CANCELED} result and {@link ActionType.REJECT_POST}
   * type.
   *
   * @param _action - The originating action identifier (unused).
   */
  public cancel(_action: string): void {
    this.showModal.set(false);
    const result: ActionEmitterResult = {};
    result.result = ActionResult.CANCELED;
    result.type = ActionType.REJECT_POST;
    this.modalHide.emit(result);
  }
}
