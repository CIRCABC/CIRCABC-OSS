import { Component, Input } from '@angular/core';

@Component({
  selector: 'cbc-file-details',
  templateUrl: './file-details.component.html',
  styleUrls: ['./file-details.component.scss'],
  preserveWhitespaces: true,
})
export class FileDetailsComponent {
  @Input()
  public file!: File;
}
