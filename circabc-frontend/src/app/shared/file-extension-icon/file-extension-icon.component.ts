import { Component, input } from '@angular/core';

import { knownExtensionsList } from 'app/shared/file-extension-icon/known-extensions';
import { environment } from 'environments/environment';

@Component({
  selector: 'cbc-file-extension-icon',
  templateUrl: './file-extension-icon.component.html',
  preserveWhitespaces: true,
})
export class FileExtensionIconComponent {
  readonly filename = input<string>();
  readonly mimtype = input<string>();

  getExtension(): string {
    const filename = this.filename();
    if (filename) {
      const dotIndex = filename.lastIndexOf('.');
      if (dotIndex !== -1) {
        const ext = filename.substring(dotIndex + 1);
        if (knownExtensionsList.indexOf(ext) !== -1) {
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
