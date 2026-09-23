import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  OnInit,
  output,
  signal,
} from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';

import { TranslocoModule } from '@jsverse/transloco';
import { ContentService } from 'app/core/generated/circabc';
import { ModalComponent } from 'app/shared/modal/modal.component';

/**
 * Modal component that lets a user check in a previously checked-out document
 * in the group library.
 *
 * It renders a form (inside a {@link ModalComponent}) allowing the user to
 * provide a version comment, flag the change as minor, and optionally keep the
 * document checked out. On submission it calls the backend
 * {@link ContentService} to perform the check-in and notifies the parent
 * component (typically the library details view) so it can refresh the
 * displayed document.
 */
@Component({
  selector: 'cbc-checkin',
  templateUrl: './checkin.component.html',
  styleUrl: './checkin.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ModalComponent, ReactiveFormsModule, TranslocoModule],
})
export class CheckinComponent implements OnInit {
  /** Generated API client used to perform the check-in operation on the backend. */
  private readonly contentService = inject(ContentService);
  /** Angular reactive forms factory used to build the check-in form. */
  private readonly formBuilder = inject(FormBuilder);

  /** Input controlling whether the check-in modal is visible. */
  public readonly showModal = input(false);
  /** Output emitted when the modal should be hidden/closed. */
  public readonly modalHide = output();
  /** Output emitted after the document has been successfully checked in. */
  public readonly checkedIn = output();
  /** Required input holding the identifier of the node (document) to check in. */
  public readonly nodeId = input.required<string>();
  /**
   * Reactive form backing the check-in dialog. Contains the `comment`,
   * `minorChange` and `keepCheckedOut` controls. Initialised in {@link ngOnInit}.
   */
  public form!: FormGroup;

  /** Enables/disables the spinner while a lengthy check-in operation is running. */
  // to enable/disable the spinner for lengthy operations
  public readonly processing = signal(false);

  /**
   * Angular lifecycle hook. Builds the reactive form when the component is
   * initialised.
   */
  public ngOnInit(): void {
    this.buildForm();
  }

  /**
   * Creates the reactive form with its default values: an empty `comment`,
   * `minorChange` set to `true` and `keepCheckedOut` set to `false`.
   */
  private buildForm() {
    this.form = this.formBuilder.group(
      {
        comment: [''],
        minorChange: [true],
        keepCheckedOut: [false],
      },
      {
        updateOn: 'change',
      }
    );
  }

  /**
   * Closes the check-in modal by emitting {@link modalHide} and resets the
   * {@link processing} flag.
   */
  public closePopupWindow(): void {
    this.modalHide.emit();
    this.processing.set(false);
  }

  /**
   * Performs the document check-in.
   *
   * Sends the current form values (minor-change flag, keep-checked-out flag and
   * version comment) to the backend via {@link ContentService.putCheckin},
   * resets the form afterwards, emits {@link checkedIn} to notify the parent
   * view, and finally closes the modal.
   *
   * @returns A promise that resolves once the check-in request has completed
   * and the modal has been closed.
   */
  public async checkin() {
    this.processing.set(true);

    // checkin document
    await this.contentService.putCheckinAsync({
      id: this.nodeId(),
      minorChange: this.form.controls.minorChange.value,
      keepCheckedOut: this.form.controls.keepCheckedOut.value,
      endEditInline: undefined,
      comment: this.form.controls.comment.value,
    });

    this.form.reset();
    this.form.controls.minorChange.patchValue(true);

    // emit an event to signal that the document has been checked in
    // will be used by the library details to display the document
    this.checkedIn.emit();

    // close form/wizard
    this.closePopupWindow();
  }
}
