import { NgClass, NgStyle } from '@angular/common';
import { Component, output, input, model } from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';

@Component({
  selector: 'cbc-preview',
  templateUrl: './preview.component.html',
  styleUrl: './preview.component.scss',
  preserveWhitespaces: true,
  imports: [NgStyle, NgClass, SpinnerComponent, TranslocoModule],
})
export class PreviewComponent {
  readonly title = input('');
  readonly okLabel = input('label.ok');
  readonly cancelLabel = input('label.cancel');
  readonly executingLabel = input('');
  readonly showOkButton = input(true);
  readonly displayCloseButton = input(true);
  readonly contentClass = input('modal-content');

  readonly ok = output<void>();
  readonly cancelPreview = output<void>();

  public visible = model<boolean>(false);
  public executing = model<boolean>(false);

  onCancel() {
    this.cancelPreview.emit();
  }

  onOk() {
    this.ok.emit();
  }
}
