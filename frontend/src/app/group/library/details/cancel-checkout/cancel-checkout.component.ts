import {
  ChangeDetectionStrategy,
  Component,
  input,
  output,
} from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';
import { ModalComponent } from 'app/shared/modal/modal.component';

/**
 * Confirmation dialog for cancelling the checkout (lock) of a library document.
 *
 * Renders a modal (via {@link ModalComponent}) that asks the user to confirm
 * cancelling a document's checkout. When confirmed, it notifies the parent
 * component so the document details view can be refreshed, and then closes the
 * modal. Localised labels are provided through Transloco.
 *
 * @example
 * ```html
 * <cbc-cancel-checkout
 *   [showModal]="isCancelling"
 *   (modalHide)="isCancelling = false"
 *   (checkoutCanceled)="reloadDocument()">
 * </cbc-cancel-checkout>
 * ```
 */
@Component({
  selector: 'cbc-cancel-checkout',
  templateUrl: './cancel-checkout.component.html',
  styleUrl: './cancel-checkout.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ModalComponent, TranslocoModule],
})
export class CancelCheckoutComponent {
  /**
   * Controls the visibility of the confirmation modal.
   * When `true` the dialog is shown; defaults to `false` (hidden).
   */
  public readonly showModal = input(false);

  /**
   * Emitted when the modal should be closed (e.g. the user dismisses the
   * dialog). The parent is expected to hide the modal in response.
   */
  public readonly modalHide = output();

  /**
   * Emitted when the user confirms cancelling the document checkout, signalling
   * that the checkout has been cancelled so the parent can refresh its view.
   */
  public readonly checkoutCanceled = output();

  /**
   * Closes the confirmation dialog by emitting the {@link modalHide} event.
   *
   * @returns Nothing.
   */
  public closePopupWindow(): void {
    this.modalHide.emit();
  }

  /**
   * Confirms cancellation of the document checkout.
   *
   * Emits {@link checkoutCanceled} to notify the parent (used by the document
   * details view to redisplay itself) and then closes the modal via
   * {@link closePopupWindow}.
   *
   * @returns A promise that resolves once the cancellation has been signalled
   * and the dialog closed.
   */
  public async cancelCheckout() {
    // emit an event to signal that the checkout has been canceled
    // will be used by the document details to redisplay the view
    this.checkoutCanceled.emit();

    // close form/wizard
    this.closePopupWindow();
  }
}
