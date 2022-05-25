import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'cbc-modal',
  templateUrl: './modal.component.html',
  preserveWhitespaces: true,
})
export class ModalComponent {
  @Input()
  title = '';
  @Input()
  okLabel = 'label.ok';
  @Input()
  cancelLabel = 'label.cancel';
  @Input()
  executingLabel = '';
  @Input()
  showOkButton = true;
  @Input()
  displayCloseButton = true;
  @Input()
  contentClass = 'modal-content';

  @Output()
  readonly ok: EventEmitter<void> = new EventEmitter<void>();
  @Output()
  readonly cancel: EventEmitter<void> = new EventEmitter<void>();

  private visibleValue = false;
  @Output()
  readonly visibleChange = new EventEmitter();
  @Input()
  get visible() {
    return this.visibleValue;
  }
  set visible(value: boolean) {
    this.visibleValue = value;
    this.visibleChange.emit(this.visibleValue);
  }

  // eslint-disable-next-line @typescript-eslint/member-ordering
  private executingValue = false;
  // eslint-disable-next-line @typescript-eslint/member-ordering
  @Output()
  readonly executingChange = new EventEmitter();
  @Input()
  get executing() {
    return this.executingValue;
  }
  set executing(value: boolean) {
    this.executingValue = value;
    this.executingChange.emit(this.executingValue);
  }

  onCancel() {
    this.cancel.emit();
  }

  onOk() {
    this.ok.emit();
  }
}
