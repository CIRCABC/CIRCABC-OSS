import { NgClass, NgStyle } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  input,
  model,
  output,
} from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';

/**
 * Reusable modal-style preview/dialog shell (`cbc-preview`).
 *
 * Renders a modal container with a title bar, a projected content area and a
 * footer holding optional "OK" and "Cancel" actions. While an action is being
 * processed it can display a busy state (with a spinner via
 * {@link SpinnerComponent}) and an "executing" label. Button and title labels
 * are resolved through Transloco for i18n.
 *
 * The component exposes its visibility and busy flags as two-way bindable
 * models so parent components can both open/close the dialog and reflect its
 * state, while emitting {@link PreviewComponent.ok | ok} and
 * {@link PreviewComponent.cancelPreview | cancelPreview} events when the user
 * activates the footer buttons.
 */
@Component({
  selector: 'cbc-preview',
  templateUrl: './preview.component.html',
  styleUrl: './preview.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [NgStyle, NgClass, SpinnerComponent, TranslocoModule],
})
export class PreviewComponent {
  /** Title text shown in the dialog header. Defaults to an empty string. */
  readonly title = input('');
  /** Transloco key for the confirm/OK button label. Defaults to `'label.ok'`. */
  readonly okLabel = input('label.ok');
  /** Transloco key for the cancel button label. Defaults to `'label.cancel'`. */
  readonly cancelLabel = input('label.cancel');
  /** Label shown while an action is executing (busy state). Defaults to an empty string. */
  readonly executingLabel = input('');
  /** Whether the confirm/OK button is displayed. Defaults to `true`. */
  readonly showOkButton = input(true);
  /** Whether the close/cancel button is displayed. Defaults to `true`. */
  readonly displayCloseButton = input(true);
  /** CSS class applied to the modal content wrapper. Defaults to `'modal-content'`. */
  readonly contentClass = input('modal-content');

  /** Emits when the user confirms the dialog by activating the OK button. */
  readonly ok = output<void>();
  /** Emits when the user dismisses the dialog by activating the Cancel button. */
  readonly cancelPreview = output<void>();

  /** Two-way bindable flag controlling whether the dialog is shown. Defaults to `false`. */
  public visible = model<boolean>(false);
  /** Two-way bindable flag indicating an action is in progress (busy state). Defaults to `false`. */
  public executing = model<boolean>(false);

  /**
   * Handles activation of the cancel button by emitting
   * {@link PreviewComponent.cancelPreview | cancelPreview}.
   */
  onCancel() {
    this.cancelPreview.emit();
  }

  /**
   * Handles activation of the OK button by emitting
   * {@link PreviewComponent.ok | ok}.
   */
  onOk() {
    this.ok.emit();
  }
}
