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
 * Modal component that lets a user report (signal) a forum post as abusive.
 *
 * It renders a confirmation modal containing the post message and a reactive
 * form with a free-text field for describing the reason for the report. On
 * confirmation it submits the report to the backend via {@link PostService}
 * and notifies the parent through the {@link SignalAbuseComponent.modalHide}
 * output; on cancellation it simply closes the modal and emits a cancelled
 * result.
 *
 * @remarks Collaborates with {@link PostService} to persist the abuse report
 * and with {@link FormBuilder} to build the reactive form.
 */
@Component({
  selector: 'cbc-signal-abuse',
  templateUrl: './signal-abuse.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, SpinnerComponent, TranslocoModule],
})
export class SignalAbuseComponent implements OnChanges {
  /** Backend service used to submit the abuse report for the post. */
  private readonly postService = inject(PostService);
  /** Factory used to build the reactive form for the report. */
  private readonly formBuilder = inject(FormBuilder);

  /**
   * Required input: the forum post being reported. Its `id` identifies the
   * target of the abuse report and its properties provide the displayed message.
   */
  readonly post = input.required<ModelNode>();

  /**
   * Two-way bindable model controlling the visibility of the modal.
   * Set to `false` when the report is submitted or cancelled.
   */
  showModal = model<boolean>(false);
  /**
   * Output emitted when the modal closes, carrying an {@link ActionEmitterResult}
   * that indicates whether the report succeeded or was cancelled.
   */
  readonly modalHide = output<ActionEmitterResult>();

  /** Whether an abuse report submission is currently in progress. */
  public readonly executing = signal(false);

  /** Reactive form holding the abuse report text; built in {@link ngOnChanges}. */
  public signalPostForm!: FormGroup;

  /**
   * Angular lifecycle hook. (Re)initialises the reactive form with an empty
   * `abuseText` control whenever the component's inputs change.
   */
  ngOnChanges() {
    this.signalPostForm = this.formBuilder.group(
      {
        abuseText: [''],
      },
      {
        updateOn: 'change',
      }
    );
  }

  /**
   * Submits the abuse report for the current post to the backend and closes
   * the modal on success.
   *
   * Sets {@link executing} while the request is in flight, emits a
   * {@link modalHide} event with a `SUCCEED` / `SIGNAL_ABUSE_POST` result once
   * the report is persisted, and finally clears {@link executing}.
   *
   * @returns A promise that resolves once the report has been submitted and
   * the modal has been closed.
   */
  public async accept() {
    this.executing.set(true);

    await this.postService.postAbuseAsync({
      id: this.post().id as string,
      abuseText: this.signalPostForm.controls.abuseText.value,
    });

    this.showModal.set(false);

    const result: ActionEmitterResult = {};
    result.result = ActionResult.SUCCEED;
    result.type = ActionType.SIGNAL_ABUSE_POST;
    this.modalHide.emit(result);

    this.executing.set(false);
  }

  /**
   * Cancels the abuse report, closing the modal without submitting anything.
   * Emits a {@link modalHide} event with a `CANCELED` / `SIGNAL_ABUSE_POST` result.
   *
   * @param _action The originating action identifier (unused).
   */
  public cancel(_action: string): void {
    this.showModal.set(false);
    const result: ActionEmitterResult = {};
    result.result = ActionResult.CANCELED;
    result.type = ActionType.SIGNAL_ABUSE_POST;
    this.modalHide.emit(result);
  }

  /**
   * The message body of the reported post, shown in the modal.
   *
   * @returns The post's message text, or an empty string when the post has no
   * properties.
   */
  // post.properties.message
  get message(): string {
    const post = this.post();
    if (post.properties) {
      return post.properties.message;
    }
    return '';
  }
}
