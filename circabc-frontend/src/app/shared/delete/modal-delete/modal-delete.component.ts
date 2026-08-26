import { Component, Input, output, input } from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';
import { ModalComponent } from 'app/shared/modal/modal.component';

@Component({
  selector: 'cbc-modal-delete',
  templateUrl: './modal-delete.component.html',
  styleUrl: './modal-delete.component.scss',
  imports: [ModalComponent, TranslocoModule],
})
export class ModalDeleteComponent {
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  public showModal = false;
  public readonly title = input('');
  public readonly text = input('');
  public readonly deletionCanceled = output();
  public readonly deletionConfirmed = output();

  public closePopupWindow(): void {
    this.showModal = false;
    this.deletionCanceled.emit();
  }

  public async delete() {
    this.showModal = false;
    this.deletionConfirmed.emit();
  }
}
