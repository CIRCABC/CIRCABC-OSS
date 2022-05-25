import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'cbc-modal-delete',
  templateUrl: './modal-delete.component.html',
  styleUrls: ['./modal-delete.component.scss'],
})
export class ModalDeleteComponent {
  @Input()
  public showModal = false;
  @Input()
  public title = '';
  @Input()
  public text = '';
  @Output()
  public readonly deletionCanceled = new EventEmitter();
  @Output()
  public readonly deletionConfirmed = new EventEmitter();

  public closePopupWindow(): void {
    this.showModal = false;
    this.deletionCanceled.emit();
  }

  public async delete() {
    this.showModal = false;
    this.deletionConfirmed.emit();
  }
}
