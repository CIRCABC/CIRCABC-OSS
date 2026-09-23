import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from '@angular/material/dialog';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { TranslocoModule } from '@jsverse/transloco';
import { ExternalRepositoryData } from 'app/core/generated/circabc';

/**
 * Configuration payload injected into {@link ConfirmDialogComponent} through
 * the Angular Material `MAT_DIALOG_DATA` token.
 *
 * It carries the textual content, action labels and presentation options that
 * the confirmation dialog renders.
 */
export interface DialogData {
  /** Dialog heading/title text. */
  title: string;
  /** Primary body message displayed to the user. */
  message: string;
  /** Secondary body message displayed below {@link DialogData.message}. */
  message2: string;
  /** Optional already-translated message used instead of a translation key. */
  messageTranslated?: string;
  /** Label for the confirm/OK action button. */
  labelOK: string;
  /** Label for the cancel action button. */
  labelCancel: string;
  /** Identifier used to select the dialog layout/presentation variant. */
  layoutStyle: string;
  /** Collection of external repository log entries shown in the dialog. */
  nodeLog: ExternalRepositoryData[];
  /** Initial notification preference, or `null` when not applicable. */
  notify: boolean | null;
}
/**
 * Reusable confirmation dialog rendered inside an Angular Material dialog.
 *
 * It displays a title, one or two messages (optionally an already-translated
 * message) and optional external repository log entries, together with confirm
 * and cancel actions. A slide toggle lets the user choose a notification
 * preference for layouts that require it. The dialog communicates the user's
 * choice back to the opener by resolving the `MatDialogRef` with a result
 * value.
 */
@Component({
  selector: 'cbc-confirm-dialog',
  templateUrl: './confirm-dialog.component.html',
  styleUrl: './confirm-dialog.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    MatCardModule,
    MatDialogModule,
    ReactiveFormsModule,
    MatSlideToggleModule,
    TranslocoModule,
  ],
})
export class ConfirmDialogComponent {
  /** Form builder used to construct the notification preference form group. */
  private readonly notifyFormBuilder = inject(FormBuilder);
  /** Reference to the opened dialog, used to close it and return a result. */
  dialogRef = inject<MatDialogRef<ConfirmDialogComponent>>(MatDialogRef);
  /** Injected dialog configuration provided by the dialog opener. */
  data = inject<DialogData>(MAT_DIALOG_DATA);

  /** Local copy of the dialog configuration data. */
  dialogData!: DialogData;
  /**
   * Reactive form group backing the notification slide toggle; its `notify`
   * control holds the user's notification preference (defaults to `true`).
   */
  notifyFormGroup = this.notifyFormBuilder.group({
    notify: true,
  });

  /**
   * Confirms the dialog, closing it and resolving the `MatDialogRef` with
   * `true`.
   *
   * @returns Nothing; the confirmation result is delivered via the dialog ref.
   */
  onOk(): void {
    // Close the dialog, return true
    this.dialogRef.close(true);
  }

  /**
   * Cancels the dialog, closing it and resolving the `MatDialogRef` with
   * `false`.
   *
   * @returns Nothing; the cancellation result is delivered via the dialog ref.
   */
  onCancel(): void {
    // Close the dialog, return false
    this.dialogRef.close(false);
  }

  /**
   * Closes the dialog and resolves the `MatDialogRef` with the current value
   * of the notification toggle, communicating the user's notification
   * preference back to the opener.
   *
   * @returns Nothing; the notification preference is delivered via the dialog
   * ref.
   */
  closeNotification(): void {
    this.dialogRef.close(this.notifyFormGroup.controls.notify.value);
  }
}
