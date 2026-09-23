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
import { MembersService } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { RichTextEditorComponent } from 'app/shared/rich-text-editor/rich-text-editor.component';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';

/**
 * Modal dialog component that lets the current user apply for membership of an
 * interest group.
 *
 * The component renders a reactive form (built with `FormBuilder`) containing
 * the applicant's username, a fixed `submitNew` action and a required rich-text
 * message, together with a spinner shown while the application is being sent.
 * On submit it posts the application to the backend through
 * {@link MembersService} and reports the outcome to the parent component via the
 * `finished` / `canceled` outputs.
 *
 * Key collaborators:
 * - {@link LoginService} to resolve the current username.
 * - {@link MembersService} (generated CIRCABC API client) to persist the
 *   membership application.
 */
@Component({
  selector: 'cbc-membership-application',
  templateUrl: './membership-application.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ReactiveFormsModule,
    RichTextEditorComponent,
    SpinnerComponent,
    TranslocoModule,
  ],
})
export class MembershipApplicationComponent implements OnInit {
  /** Reactive forms builder used to construct {@link applicationForm}. */
  private readonly fb = inject(FormBuilder);
  /** Provides the currently authenticated user's username. */
  private readonly loginService = inject(LoginService);
  /** Generated CIRCABC API client used to submit the membership application. */
  private readonly membersService = inject(MembersService);

  /**
   * Two-way bindable model controlling the visibility of the application modal.
   * Set to `false` when the dialog is cancelled.
   */
  showModal = model<boolean>(false);
  /** Input: identifier of the group the user is applying to join. */
  readonly groupId = input<string>();
  /**
   * Output: emitted with a {@link ActionEmitterResult} of type
   * `APPLY_FOR_MEMBERSHIP` and result `CANCELED` when the user cancels.
   */
  readonly canceled = output<ActionEmitterResult>();
  /**
   * Output: emitted with a {@link ActionEmitterResult} of type
   * `APPLY_FOR_MEMBERSHIP` and result `SUCCEED` or `FAILED` once the
   * application submission completes.
   */
  readonly finished = output<ActionEmitterResult>();

  /** Whether an application submission is currently in progress. */
  public readonly processing = signal(false);
  /** Reactive form holding the username, action and message fields. */
  public applicationForm!: FormGroup;

  /**
   * Angular lifecycle hook. Initializes {@link applicationForm} with the
   * current username, a default `submitNew` action and a required message.
   */
  ngOnInit() {
    this.applicationForm = this.fb.group(
      {
        username: [this.loginService.getCurrentUsername()],
        action: ['submitNew'],
        message: ['', Validators.required],
      },
      {
        updateOn: 'change',
      }
    );
  }

  /**
   * Submits the membership application for the current {@link groupId}.
   *
   * No-op when no group id is set. Sets {@link processing} while the request is
   * in flight, posts the form value via {@link MembersService.postApplicant},
   * resets the form on success and emits the outcome through {@link finished}.
   *
   * @returns A promise that resolves once the submission completes and the
   * result has been emitted.
   */
  async submitApplication() {
    const groupId = this.groupId();
    if (groupId === undefined) {
      return;
    }
    this.processing.set(true);
    const res: ActionEmitterResult = {};
    res.type = ActionType.APPLY_FOR_MEMBERSHIP;

    try {
      await this.membersService.postApplicantAsync({
        id: groupId,
        applicantAction: this.applicationForm.value,
      });
      res.result = ActionResult.SUCCEED;
      this.applicationForm.reset();
    } catch (error) {
      console.error(error);
      res.result = ActionResult.FAILED;
    }

    this.finished.emit(res);
    this.processing.set(false);
  }

  /**
   * Cancels the application, resetting the form fields to their defaults,
   * hiding the modal via {@link showModal} and emitting a `CANCELED` result
   * through {@link canceled}.
   */
  cancel() {
    this.applicationForm.controls.username.setValue(
      this.loginService.getCurrentUsername()
    );
    this.applicationForm.controls.action.setValue('submitNew');
    this.applicationForm.controls.message.setValue('');

    const res: ActionEmitterResult = {};
    res.type = ActionType.APPLY_FOR_MEMBERSHIP;
    res.result = ActionResult.CANCELED;

    this.showModal.set(false);
    this.canceled.emit(res);
  }
}
