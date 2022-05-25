import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'cbc-inline-delete',
  templateUrl: './inline-delete.component.html',
  styleUrls: ['./inline-delete.component.scss'],
  preserveWhitespaces: true,
})
export class InlineDeleteComponent {
  @Output()
  readonly deletionConfirmed = new EventEmitter();

  @Input()
  deleting = false;
  @Output()
  readonly deletingChange = new EventEmitter();

  @Input()
  mustConfirm = false;
  @Output()
  readonly mustConfirmChange = new EventEmitter();

  @Input()
  useIcon = false;

  @Input()
  useText = true;

  @Input()
  image = '';
  @Input()
  imageHeightWidth = 12;

  @Input()
  deleteInline = true;

  @Input()
  deleteLabel = 'label.delete';

  public deleteAskConfirmation() {
    this.mustConfirm = true;
    this.mustConfirmChange.emit(this.mustConfirm);
  }

  public confirm() {
    if (this.deleteInline) {
      this.deleting = true;
    } else {
      this.mustConfirm = false;
    }
    this.deletionConfirmed.emit();
    this.deletingChange.emit(this.deleting);
  }

  public cancel() {
    this.mustConfirm = false;
    this.mustConfirmChange.emit(this.mustConfirm);
  }
}
