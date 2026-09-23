import {
  ChangeDetectionStrategy,
  Component,
  inject,
  WritableSignal,
} from '@angular/core';
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from '@angular/material/dialog';
import { TranslocoModule } from '@jsverse/transloco';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';

/**
 * Data contract passed to {@link SummarizeDialogComponent} through the
 * `MAT_DIALOG_DATA` injection token when the dialog is opened.
 *
 * It carries the state required to render an AI-generated document summary,
 * including loading progress and error information.
 */
export interface SummarizeDialogData {
  /** Display name of the document node being summarized. */
  nodeName: string;
  /** Whether the summary is still being generated; drives the spinner display. */
  loading: WritableSignal<boolean>;
  /** The generated summary text, populated once generation completes. */
  summary: WritableSignal<string>;
  /** Error message to display when summary generation fails; empty when none. */
  error: WritableSignal<string>;
}

/**
 * Modal dialog component that displays an AI-generated summary of a library
 * document.
 *
 * Rendered inside an Angular Material dialog, it shows the document name and
 * either a loading spinner (while the summary is being generated), the
 * resulting summary text, or an error message — driven by the injected
 * {@link SummarizeDialogData}. The component itself holds no business logic;
 * the summary generation is performed by the caller that opens the dialog and
 * supplies the data.
 */
@Component({
  selector: 'cbc-summarize-dialog',
  templateUrl: './summarize-dialog.component.html',
  styleUrl: './summarize-dialog.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [MatDialogModule, TranslocoModule, SpinnerComponent],
})
export class SummarizeDialogComponent {
  /** Reference to the Material dialog instance, used to close the dialog. */
  dialogRef = inject<MatDialogRef<SummarizeDialogComponent>>(MatDialogRef);
  /**
   * Summary state injected via `MAT_DIALOG_DATA`, bound by the template to
   * render the document name, loading spinner, summary text or error.
   */
  data = inject<SummarizeDialogData>(MAT_DIALOG_DATA);
}
