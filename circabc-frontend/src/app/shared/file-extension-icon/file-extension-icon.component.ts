import { Component, Input } from '@angular/core';

import { KnownExtensions } from 'app/shared/file-extension-icon/known-extensions';
import { environment } from 'environments/environment';

@Component({
  selector: 'cbc-file-extension-icon',
  templateUrl: './file-extension-icon.component.html',
  preserveWhitespaces: true,
})
export class FileExtensionIconComponent {
  @Input()
  filename: string | undefined;
  @Input()
  mimtype: string | undefined;

  getExtension(): string {
    if (this.filename) {
      const dotIndex = this.filename.lastIndexOf('.');
      if (dotIndex !== -1) {
        const ext = this.filename.substring(dotIndex + 1);
        if (KnownExtensions.list.indexOf(ext) !== -1) {
          return ext;
        }
      }
    }
    return 'blank';
  }

  getFilePath(): string {
    return `${
      environment.baseHref
    }img/file-extensions/24-grey/${this.getExtension()}-file-format.png`;
  }
}
