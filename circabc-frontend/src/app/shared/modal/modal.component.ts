import { NgClass, NgStyle } from '@angular/common';
import { Component, output, input, model } from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';
import { DataCyDirective } from 'app/shared/directives/data-cy.directive';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';

@Component({
  selector: 'cbc-modal',
  templateUrl: './modal.component.html',
  preserveWhitespaces: true,
  imports: [
    NgStyle,
    NgClass,
    SpinnerComponent,
    DataCyDirective,
    TranslocoModule,
  ],
})
export class ModalComponent {
  readonly title = input('');
  readonly okLabel = input('label.ok');
  readonly cancelLabel = input('label.cancel');
  readonly executingLabel = input('');
  readonly showOkButton = input(true);
  readonly displayCloseButton = input(true);
  readonly contentClass = input('modal-content');

  readonly ok = output<void>();
  readonly cancelModal = output<void>();

  public visible = model.required<boolean>();

  public executing = model<boolean>(false);

  onCancel() {
    this.cancelModal.emit();
  }

  onOk() {
    this.ok.emit();
  }
}
