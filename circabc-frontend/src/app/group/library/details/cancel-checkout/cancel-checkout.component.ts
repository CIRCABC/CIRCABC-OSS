import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'cbc-cancel-checkout',
  templateUrl: './cancel-checkout.component.html',
  styleUrls: ['./cancel-checkout.component.scss'],
  preserveWhitespaces: true,
})
export class CancelCheckoutComponent {
  @Input()
  public showModal = false;
  @Output()
  public readonly modalHide = new EventEmitter();
  @Output()
  public readonly checkoutCanceled = new EventEmitter();

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
