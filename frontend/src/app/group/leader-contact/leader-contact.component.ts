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
import { EmailService, type InterestGroup } from 'app/core/generated/circabc';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { RichTextEditorComponent } from 'app/shared/rich-text-editor/rich-text-editor.component';

/**
 * Standalone Angular component that renders a modal dialog allowing a user to
 * contact the leaders of an interest group by email.
 *
 * The component displays a modal (via {@link ModalComponent}) containing a
 * reactive form with a single rich-text {@link RichTextEditorComponent}
 * message field. When submitted, it sends the message to the group's leaders
 * through the {@link EmailService} and reports the outcome back to the parent
 * via the {@link LeaderContactComponent.modalHide} output.
 *
 * Key collaborators:
 * - {@link FormBuilder} to build the reactive form.
 * - {@link EmailService} (generated CIRCABC API client) to deliver the email.
 */
@Component({
  selector: 'cbc-leader-contact',
  templateUrl: './leader-contact.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ModalComponent,
    ReactiveFormsModule,
    RichTextEditorComponent,
    TranslocoModule,
  ],
})
export class LeaderContactComponent implements OnInit {
  /** FormBuilder used to construct the reactive contact form. */
  private readonly fb = inject(FormBuilder);
  /** Generated CIRCABC API client used to send the email to group leaders. */
  private readonly emailService = inject(EmailService);

  /**
   * Input controlling whether the contact modal is displayed.
   * Defaults to `false` (hidden).
   */
  readonly showModal = input(false);
  /**
   * Required input providing the interest group whose leaders will be
   * contacted. The group's `id` is used to target the email.
   */
  readonly group = input.required<InterestGroup>();
  /**
   * Output emitted when the modal is closed, either after a send attempt or a
   * cancellation. Carries an {@link ActionEmitterResult} describing the
   * outcome (succeeded, failed or canceled) so the parent can react.
   */
  readonly modalHide = output<ActionEmitterResult>();

  /**
   * Indicates whether a send operation is currently in progress. Used to
   * reflect a busy state in the template while the email is being sent.
   */
  public readonly processing = signal(false);
  /** Reactive form holding the message to send to the group leaders. */
  public contactLeaderForm!: FormGroup;

  /**
   * Angular lifecycle hook. Initializes {@link contactLeaderForm} with a
   * required `message` control.
   *
   * @returns A promise that resolves once the form has been initialized.
   */
  ngOnInit() {
    this.contactLeaderForm = this.fb.group({
      message: ['', Validators.required],
    });
  }

  /**
   * Sends the message entered in {@link contactLeaderForm} to the leaders of
   * the current group via {@link EmailService.contactLeadersByEmail}.
   *
   * Sets {@link processing} to `true` while the request is in flight. On
   * completion, emits an {@link ActionEmitterResult} of type
   * `CONTACT_LEADERS` through {@link modalHide} with `SUCCEED` when the email
   * is sent successfully or `FAILED` when the request throws. If the group has
   * no `id`, no email is sent and no result flag is set.
   *
   * @returns A promise that resolves once the send attempt has completed and
   * the result has been emitted.
   */
  public async send() {
    this.processing.set(true);
    const res: ActionEmitterResult = {};
    res.type = ActionType.CONTACT_LEADERS;

    const group = this.group();
    if (group?.id) {
      try {
        await this.emailService.contactLeadersByEmailAsync({
          id: group.id,
          body: this.contactLeaderForm.value.message,
        });

        res.result = ActionResult.SUCCEED;
      } catch (error) {
        console.error(error);
        res.result = ActionResult.FAILED;
      }
    }

    this.processing.set(false);
    this.modalHide.emit(res);
  }

  /**
   * Cancels the contact operation without sending an email and closes the
   * modal by emitting an {@link ActionEmitterResult} of type `CONTACT_LEADERS`
   * with result `CANCELED` through {@link modalHide}.
   */
  public cancel() {
    const res: ActionEmitterResult = {};
    res.type = ActionType.CONTACT_LEADERS;
    res.result = ActionResult.CANCELED;
    this.modalHide.emit(res);
  }
}
