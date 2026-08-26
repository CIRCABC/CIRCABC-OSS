import { Component, input } from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';
import { SizePipe } from 'app/shared/pipes/size.pipe';

@Component({
  selector: 'cbc-file-details',
  templateUrl: './file-details.component.html',
  styleUrl: './file-details.component.scss',
  preserveWhitespaces: true,
  imports: [SizePipe, TranslocoModule],
})
export class FileDetailsComponent {
  public readonly file = input.required<File>();
}
