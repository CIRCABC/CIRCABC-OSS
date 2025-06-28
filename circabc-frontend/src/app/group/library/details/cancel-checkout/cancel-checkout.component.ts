import { Component, Input, output } from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';
import { ModalComponent } from 'app/shared/modal/modal.component';

@Component({
  selector: 'cbc-cancel-checkout',
  templateUrl: './cancel-checkout.component.html',
  styleUrl: './cancel-checkout.component.scss',
  preserveWhitespaces: true,
  imports: [ModalComponent, TranslocoModule],
})
export class CancelCheckoutComponent {
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  public showModal = false;
  public readonly modalHide = output();
  public readonly checkoutCanceled = output();

  public closePopupWindow(): void {
    this.showModal = false;
    this.modalHide.emit();
  }

  public async cancelCheckout() {
    // emit an event to signal that the checkout has been canceled
    // will be used by the document details to redisplay the view
    this.checkoutCanceled.emit();

    // close form/wizard
    this.closePopupWindow();
  }
}
