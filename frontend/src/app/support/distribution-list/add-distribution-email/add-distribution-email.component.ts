import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  linkedSignal,
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
  AppMessageService,
  DistributionMail,
} from 'app/core/generated/circabc';
import { ControlMessageComponent } from 'app/shared/control-message/control-message.component';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';

/**
 * Modal component that lets support administrators add email addresses to the
 * application-wide distribution list.
 *
 * Renders a modal dialog (via {@link ModalComponent}) containing a reactive
 * form with a single, email-validated input. Submitted addresses are persisted
 * through the {@link AppMessageService} and accumulated in {@link emailCreated}
 * so the template can show which addresses were created during the current
 * session.
 *
 * The dialog visibility is controlled by the `showModal` input and mirrored
 * back to the parent through the `showModalChange` and `modalClosed` outputs.
 */
@Component({
  selector: 'cbc-add-distribution-email',
  templateUrl: './add-distribution-email.component.html',
  styleUrl: './add-distribution-email.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ModalComponent,
    ReactiveFormsModule,
    ControlMessageComponent,
    SpinnerComponent,
    TranslocoModule,
  ],
})
export class AddDistributionEmailComponent implements OnInit {
  /** Builder used to create the reactive {@link emailForm}. */
  private readonly fb = inject(FormBuilder);
  /** API client used to persist new distribution email addresses. */
  private readonly appMessageService = inject(AppMessageService);

  /**
   * Input (aliased as `showModal`) providing the initial open/closed state of
   * the dialog requested by the parent component.
   */
  // eslint-disable-next-line @angular-eslint/no-input-rename
  showModalInput = input(false, { alias: 'showModal' });
  /**
   * Writable signal tracking the current visibility of the modal. Seeded from
   * {@link showModalInput} and updated internally (e.g. when cancelling).
   */
  showModal = linkedSignal(this.showModalInput);
  /**
   * Output emitting the new visibility state whenever the modal is closed from
   * within the component, allowing the parent to sync its own state.
   */
  readonly showModalChange = output<boolean>();
  /** Output emitted when the modal has been dismissed/closed. */
  readonly modalClosed = output();

  /** Reactive form holding the single `emailAddress` control. */
  public emailForm!: FormGroup;
  /** Addresses successfully created during the current dialog session. */
  public emailCreated = signal<DistributionMail[]>([]);
  /** Flag indicating an add request is in progress (drives the spinner). */
  public processing = signal(false);

  /**
   * Angular lifecycle hook. Initialises {@link emailForm} with an
   * email-validated `emailAddress` control.
   */
  ngOnInit() {
    this.emailForm = this.fb.group({
      emailAddress: ['', Validators.email],
    });
  }

  /**
   * Closes the modal without adding further addresses: resets visibility,
   * notifies the parent via {@link showModalChange}, clears the list of
   * created emails and emits {@link modalClosed}.
   */
  cancel() {
    this.showModal.set(false);
    this.showModalChange.emit(this.showModal());
    this.emailCreated.set([]);
    this.modalClosed.emit();
  }

  /**
   * Convenience accessor for the `emailAddress` form control.
   *
   * @returns The `emailAddress` control, or `undefined` if the form has not
   * been initialised yet.
   */
  get emailAddressControl() {
    return this.emailForm?.controls.emailAddress;
  }

  /**
   * Validates and persists the currently entered email address.
   *
   * When the address is non-empty (and not the literal string `'null'`) and
   * the form is valid, sends it to {@link AppMessageService.addDistributionEmails},
   * appends it to {@link emailCreated} and resets the input. The
   * {@link processing} flag guards the UI during the request. Errors are caught
   * and logged to the console rather than propagated.
   *
   * @returns A promise that resolves once the add attempt has completed.
   */
  public async addDistributionEmail() {
    if (
      this.emailForm.value.emailAddress &&
      this.emailForm.value.emailAddress !== 'null' &&
      this.emailForm.value.emailAddress !== '' &&
      this.emailForm.valid
    ) {
      try {
        this.processing.set(true);
        const body: DistributionMail[] = [];
        body.push(this.emailForm.value);
        await this.appMessageService.addDistributionEmailsAsync({
          distributionMail: body,
        });
        this.emailCreated.set([...this.emailCreated(), this.emailForm.value]);
        this.emailForm.controls.emailAddress.reset();
      } catch (error) {
        console.error(error);
      } finally {
        this.processing.set(false);
      }
    }
  }
}
