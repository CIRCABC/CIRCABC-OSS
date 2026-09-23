import { NgClass, NgStyle } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  input,
  model,
  output,
} from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';
import { DataCyDirective } from 'app/shared/directives/data-cy.directive';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';

/**
 * Reusable modal dialog component (`cbc-modal`).
 *
 * Renders a customisable modal window with a title, a content projection area,
 * an optional close button in the header, and a footer holding configurable
 * "OK" and "Cancel" actions. While an action is in progress it can display a
 * {@link SpinnerComponent} together with a busy label. Button and title text
 * are resolved through Transloco translation keys.
 *
 * The dialog's open state is driven by the two-way bindable {@link visible}
 * model, and callers can react to user actions via the {@link ok} and
 * {@link cancelModal} outputs.
 */
@Component({
  selector: 'cbc-modal',
  templateUrl: './modal.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    NgStyle,
    NgClass,
    SpinnerComponent,
    DataCyDirective,
    TranslocoModule,
  ],
})
export class ModalComponent {
  /** Translation key (or literal text) shown as the modal's header title. */
  readonly title = input('');
  /** Translation key for the confirm/OK button label. Defaults to `label.ok`. */
  readonly okLabel = input('label.ok');
  /** Translation key for the cancel button label. Defaults to `label.cancel`. */
  readonly cancelLabel = input('label.cancel');
  /** Translation key (or literal text) shown next to the spinner while an action is executing. */
  readonly executingLabel = input('');
  /** Whether the confirm/OK button is rendered in the footer. Defaults to `true`. */
  readonly showOkButton = input(true);
  /** Whether the close (dismiss) button is rendered in the header. Defaults to `true`. */
  readonly displayCloseButton = input(true);
  /** CSS class applied to the modal content wrapper. Defaults to `modal-content`. */
  readonly contentClass = input('modal-content');

  /** Emits when the user confirms the dialog by activating the OK button. */
  readonly ok = output<void>();
  /** Emits when the user dismisses the dialog via the cancel or close button. */
  readonly cancelModal = output<void>();

  /**
   * Two-way bindable flag controlling whether the modal is displayed.
   * Required: consumers must provide a binding for this model.
   */
  public visible = model.required<boolean>();

  /**
   * Two-way bindable flag indicating that an action is in progress.
   * When `true`, the modal shows the spinner and busy label. Defaults to `false`.
   */
  public executing = model<boolean>(false);

  /**
   * Handles a cancel/close interaction by emitting the {@link cancelModal} output.
   */
  onCancel() {
    this.cancelModal.emit();
  }

  /**
   * Handles a confirm interaction by emitting the {@link ok} output.
   */
  onOk() {
    this.ok.emit();
  }
}
