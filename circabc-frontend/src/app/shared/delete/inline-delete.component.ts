import { Component, Input, output, input } from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';

@Component({
  selector: 'cbc-inline-delete',
  templateUrl: './inline-delete.component.html',
  styleUrl: './inline-delete.component.scss',
  preserveWhitespaces: true,
  imports: [TranslocoModule],
})
export class InlineDeleteComponent {
  readonly deletionConfirmed = output();

  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  deleting = false;
  readonly deletingChange = output<boolean>();

  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  mustConfirm = false;
  readonly mustConfirmChange = output<boolean>();

  readonly useIcon = input(false);

  readonly useText = input(true);

  // TODO: Skipped for migration because:
  //  This input is used in a control flow expression (e.g. `@if` or `*ngIf`)
  //  and migrating would break narrowing currently.
  @Input()
  image = '';
  readonly imageHeightWidth = input(12);

  readonly deleteInline = input(true);

  readonly deleteLabel = input('label.delete');

  public deleteAskConfirmation() {
    this.mustConfirm = true;
    this.mustConfirmChange.emit(this.mustConfirm);
  }

  public confirm() {
    if (this.deleteInline()) {
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
