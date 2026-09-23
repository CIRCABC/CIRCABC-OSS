import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { TranslocoModule } from '@jsverse/transloco';
import { CategoryService } from 'app/core/generated/circabc';
import { RichTextEditorComponent } from 'app/shared/rich-text-editor/rich-text-editor.component';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';

/**
 * Dialog component that renders the form used to decline (reject) an interest
 * group creation request.
 *
 * Displayed inside a Material dialog, it presents a required rich-text
 * "argument" field (the justification shown to the requester) and, on
 * submission, calls the backend to validate/reject the associated interest
 * group request. A spinner is shown while the rejection call is in progress.
 *
 * The component relies on data injected via {@link MAT_DIALOG_DATA} (the target
 * `request` and its parent `categoryId`) and closes the dialog with a `true`
 * result once processing completes, signalling the caller that the request was
 * handled.
 *
 * Key collaborators:
 * - {@link CategoryService} — performs the interest group request validation.
 * - {@link MatDialogRef} — controls and closes the hosting dialog.
 */
@Component({
  selector: 'cbc-declne-form',
  templateUrl: './decline-form.component.html',
  styleUrl: './decline-form.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    RichTextEditorComponent,
    ReactiveFormsModule,
    SpinnerComponent,
    TranslocoModule,
  ],
})
export class DeclineFormComponent implements OnInit {
  /** Angular reactive forms builder used to construct {@link decliningForm}. */
  fb = inject(FormBuilder);
  /**
   * Data injected into the Material dialog. Expected to expose the target
   * `request` and its parent `categoryId`.
   */
  data = inject(MAT_DIALOG_DATA);
  /** Reference to the hosting dialog, used to close it with a result. */
  dialogRef = inject(MatDialogRef<DeclineFormComponent>);
  /** Generated API service used to submit the request rejection. */
  categoryService = inject(CategoryService);

  /** The interest group request being declined, taken from the dialog data. */
  request = this.data.request;
  /** Identifier of the category the request belongs to, from the dialog data. */
  categoryId = this.data.categoryId;
  /**
   * Reactive form holding the rejection details (required `argument`, an
   * `agreement` flag defaulting to `-1`, and the request `id`).
   */
  decliningForm!: FormGroup;
  /** Whether a rejection request is currently in flight (drives the spinner). */
  readonly processing = signal(false);

  /**
   * Angular lifecycle hook. Initializes {@link decliningForm} with the required
   * `argument` control, a default `agreement` value and the current request id.
   */
  ngOnInit() {
    this.decliningForm = this.fb.group({
      argument: ['', Validators.required],
      agreement: -1,
      id: this.request.id,
    });
  }

  /**
   * Submits the decline form to reject the interest group request.
   *
   * Sets {@link processing} while the call is in flight, invokes
   * {@link CategoryService.validateInterestGroupRequests} with the current form
   * values when both `categoryId` and `request.id` are present, then clears the
   * processing flag and closes the dialog with a `true` result. Errors from the
   * backend call are caught and logged to the console rather than propagated.
   *
   * @returns A promise that resolves once the rejection attempt has completed
   * and the dialog has been closed.
   */
  public async reject() {
    this.processing.set(true);
    try {
      if (this.categoryId && this.request.id) {
        await this.categoryService.validateInterestGroupRequestsAsync({
          id: this.categoryId,
          idRequest: this.request.id,
          groupCreationRequestApproval: this.decliningForm.value,
        });
      }
    } catch (error) {
      console.error(error);
    }
    this.processing.set(false);
    this.dialogRef.close(true);
  }
}
