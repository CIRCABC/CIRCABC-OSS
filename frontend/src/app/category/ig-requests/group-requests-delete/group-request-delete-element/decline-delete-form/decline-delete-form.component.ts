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
import {
  CategoryService,
  GroupDeletionRequestApproval,
} from 'app/core/generated/circabc';
import { RichTextEditorComponent } from 'app/shared/rich-text-editor/rich-text-editor.component';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';

/**
 * Dialog component that renders the form used to decline (reject) an interest
 * group deletion request.
 *
 * It is opened inside a Material dialog and presents a rich-text editor where
 * the reviewer must provide a mandatory justification argument for the
 * rejection. On submission it sends a {@link GroupDeletionRequestApproval} with
 * a negative agreement value to the backend via {@link CategoryService} and
 * closes the dialog reporting success.
 *
 * The dialog expects `MAT_DIALOG_DATA` to contain the `categoryId` and the
 * `request` (whose `id` identifies the deletion request being declined).
 *
 * Collaborators:
 * - {@link FormBuilder} to build the reactive form.
 * - {@link CategoryService} to persist the rejection decision.
 * - {@link MatDialogRef} to close the dialog and return the outcome.
 */
@Component({
  selector: 'cbc-decline-delete-form',
  templateUrl: './decline-delete-form.component.html',
  styleUrl: './decline-delete-form.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    RichTextEditorComponent,
    ReactiveFormsModule,
    SpinnerComponent,
    TranslocoModule,
  ],
})
export class DeclineDeleteFormComponent implements OnInit {
  /** Factory used to build the reactive declining form. */
  fb = inject(FormBuilder);
  /**
   * Data injected into the Material dialog. Expected to expose `categoryId`
   * and the `request` object (with its `id`) targeted by the rejection.
   */
  data = inject(MAT_DIALOG_DATA);
  /** Reference to the enclosing dialog, used to close and return the result. */
  dialogRef = inject(MatDialogRef<DeclineDeleteFormComponent>);
  /** Backend service used to submit the deletion request validation. */
  categoryService = inject(CategoryService);
  /** Reactive form holding the mandatory `argument` (rejection justification). */
  decliningForm!: FormGroup;
  /** Whether a rejection submission is currently in progress. */
  processing = signal(false);
  /** Payload sent to the backend describing the rejection decision. */
  groupDeletionRequestApproval!: GroupDeletionRequestApproval;

  /**
   * Angular lifecycle hook. Initializes the reactive form with a required
   * `argument` control and prepares the {@link GroupDeletionRequestApproval}
   * payload with the target request id and a negative agreement flag.
   */
  ngOnInit() {
    this.decliningForm = this.fb.group({
      argument: ['', Validators.required],
    });

    this.groupDeletionRequestApproval = {
      id: this.data.request.id,
      argument: '',
      agreement: -1,
    };
  }

  /**
   * Submits the rejection of the group deletion request.
   *
   * Copies the form's `argument` value into the approval payload, toggles the
   * {@link processing} flag while the request is in flight, sends the decision
   * through {@link CategoryService.validateInterestGroupDeleteRequests} and
   * closes the dialog with `true` on success.
   *
   * @returns A promise that resolves once the request completes and the dialog
   * has been closed.
   */
  async reject() {
    this.groupDeletionRequestApproval.argument =
      this.decliningForm.controls.argument.value;

    this.processing.set(true);
    await this.categoryService.validateInterestGroupDeleteRequestsAsync({
      id: this.data.categoryId,
      groupDeletionRequestApproval: this.groupDeletionRequestApproval,
    });
    this.processing.set(false);
    this.dialogRef.close(true);
  }
}
